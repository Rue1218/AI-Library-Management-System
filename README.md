# 图书馆管理系统

一个功能完善的图书馆管理系统，采用前后端分离架构，支持图书借阅、预约、AI智能助手等功能。

## 技术栈

### 前端

- Vue 3 + Composition API
- Vite
- Element Plus
- Pinia
- Vue Router
- Axios
- ECharts
- marked + highlight.js

### 后端

- Spring Boot 2.7
- MyBatis-Plus
- MySQL
- JWT
- Spring AOP

## 功能模块

### 用户端

- 图书浏览、搜索、详情查看
- 图书借阅、预约、续借
- 用户中心（个人信息、头像修改）
- 图书评论、点赞
- AI智能助手（支持接入第三方大模型API）
- 数字资源浏览

### 管理端

- 图书管理（增删改查、分类管理、封面上传）
- 用户管理（启用/禁用）
- 借阅管理（审批、逾期处理）
- 预约管理
- 数据统计（借阅趋势、热门图书）
- 系统设置

## 项目结构

```
├── library-management/           # 前端项目
│   ├── src/
│   │   ├── api/                  # API接口
│   │   ├── views/                # 页面组件
│   │   │   ├── admin/            # 管理端
│   │   │   └── user/             # 用户端
│   │   ├── router/               # 路由
│   │   ├── stores/               # 状态管理
│   │   └── utils/                # 工具函数
│   └── vite.config.js
│
├── library-management-backend/   # 后端项目
│   └── src/main/
│       └── java/com/library/library/
│           ├── controller/       # 控制器
│           ├── service/          # 业务逻辑
│           ├── mapper/           # 数据访问
│           ├── entity/           # 实体类
│           ├── config/           # 配置
│           └── interceptor/      # 拦截器
│
├── images/                       # 静态图片资源
├── library_db.sql               # 数据库脚本
└── PROJECT_RESUME.md            # 简历描述
```

## 快速开始

### 环境要求

- Node.js 16+
- JDK 17+
- MySQL 8.0+
- Maven 3.8+

### 1. 导入数据库

```sql
CREATE DATABASE library_db CHARACTER SET utf8mb4;
USE library_db;
SOURCE library_db.sql;
```

### 2. 配置后端

修改 `library-management-backend/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/library_db
    username: root
    password: 你的数据库密码
```

### 3. 启动后端

```bash
cd library-management-backend
mvn spring-boot:run
```

后端启动成功访问：`http://localhost:8080`

### 4. 启动前端

```bash
cd library-management
npm install
npm run dev
```

前端启动成功访问：`http://localhost:5173`

### 5. 登录账号

| 角色     | 用户名  | 密码     |
| -------- | ------- | -------- |
| 管理员   | admin   | admin123 |
| 普通用户 | user001 | 123456   |

## API接口

后端提供以下主要接口：

- `/api/auth/*` - 登录注册
- `/api/books/*` - 图书管理
- `/api/borrow/*` - 借阅管理
- `/api/reservation/*` - 预约管理
- `/api/category/*` - 分类管理
- `/api/user/*` - 用户管理
- `/api/statistics/*` - 数据统计
- `/api/upload/*` - 文件上传

## 扩展功能

### AI智能助手

系统集成了AI助手功能，支持接入第三方大模型API（如DeepSeek、字节方舟等）。

配置步骤：

1. 在AI助手页面点击设置
2. 填入API地址、API Key、模型ID
3. 点击检测按钮验证配置

## 注意事项

1. 图片上传默认存储路径：自己设置路径
2. JWT Token有效期：24小时
3. 借阅逾期会产生逾期费用

## 许可证

MIT License
