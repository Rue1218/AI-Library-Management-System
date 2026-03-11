import axios from 'axios'

const DEFAULT_TIMEOUT = 45000

const normalizeChatEndpoint = (baseUrl = '') => {
  const trimmed = baseUrl.trim().replace(/\/+$/, '')
  if (!trimmed) return ''

  if (trimmed.endsWith('/chat/completions')) {
    return trimmed
  }

  if (trimmed.endsWith('/v1')) {
    return `${trimmed}/chat/completions`
  }

  return `${trimmed}/v1/chat/completions`
}

const buildHeaders = (apiKey = '') => {
  const key = apiKey.trim()
  return {
    'Content-Type': 'application/json',
    ...(key ? { Authorization: `Bearer ${key}` } : {})
  }
}

const createBody = (modelId, messages, temperature = 0.7, maxTokens = 0) => {
  const body = { model: modelId, messages, temperature }
  if (maxTokens && maxTokens > 0) body.max_tokens = maxTokens
  return body
}

export const testAiModel = async ({ apiKey, baseUrl, modelId }) => {
  const endpoint = normalizeChatEndpoint(baseUrl)
  const body = createBody(modelId, [
    {
      role: 'system',
      content: '你是一个中文AI助手。请只使用中文回答。'
    },
    {
      role: 'user',
      content: '你好，请回复“连接成功”。'
    }
  ])

  const { data } = await axios.post(endpoint, body, {
    timeout: DEFAULT_TIMEOUT,
    headers: buildHeaders(apiKey)
  })

  const content = data?.choices?.[0]?.message?.content || ''
  return {
    ok: !!content,
    response: content,
    raw: data,
    endpoint
  }
}

/**
 * 流式对话 —— SSE / ReadableStream
 * @param {object} options
 * @param {string} options.apiKey
 * @param {string} options.baseUrl
 * @param {string} options.modelId
 * @param {Array}  options.messages
 * @param {AbortSignal} [options.signal]
 * @param {(chunk:{type:'thinking'|'content', text:string})=>void} options.onChunk
 * @param {()=>void} [options.onDone]
 */
export const streamAiChat = async ({ apiKey, baseUrl, modelId, messages, signal, temperature = 0.7, maxTokens = 0, onChunk, onDone }) => {
  const endpoint = normalizeChatEndpoint(baseUrl)
  const bodyObj = {
    model: modelId,
    messages,
    temperature: temperature ?? 0.7,
    stream: true,
    stream_options: { include_usage: true }
  }
  if (maxTokens && maxTokens > 0) bodyObj.max_tokens = maxTokens
  const body = JSON.stringify(bodyObj)

  const response = await fetch(endpoint, {
    method: 'POST',
    headers: buildHeaders(apiKey),
    body,
    signal
  })

  if (!response.ok) {
    let errMsg = `HTTP ${response.status}`
    try {
      const errData = await response.json()
      errMsg = errData?.error?.message || errMsg
    } catch {}
    throw new Error(errMsg)
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''
  let usageData = null

  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })
    const lines = buffer.split('\n')
    buffer = lines.pop() ?? ''

    for (const line of lines) {
      const trimmed = line.trim()
      if (!trimmed || trimmed.startsWith(':')) continue
      if (trimmed === 'data: [DONE]') {
        onDone?.({ usage: usageData })
        return
      }
      if (trimmed.startsWith('data: ')) {
        try {
          const json = JSON.parse(trimmed.slice(6))
          if (json?.usage) usageData = json.usage
          const delta = json?.choices?.[0]?.delta ?? {}
          // DeepSeek R1 / Qwen 推理模型支持 reasoning_content（思考链）
          if (delta.reasoning_content) {
            onChunk?.({ type: 'thinking', text: delta.reasoning_content })
          }
          if (delta.content) {
            onChunk?.({ type: 'content', text: delta.content })
          }
        } catch {}
      }
    }
  }

  onDone?.({ usage: usageData })
}

export { normalizeChatEndpoint }
