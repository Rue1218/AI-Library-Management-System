package com.library.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.library.entity.BookComment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookCommentMapper extends BaseMapper<BookComment> {
}
