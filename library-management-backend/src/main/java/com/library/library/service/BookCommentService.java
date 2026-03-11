package com.library.library.service;

import com.library.library.vo.BookRatingVO;
import com.library.library.vo.CommentVO;

import java.util.List;

public interface BookCommentService {
    void addComment(Long userId, Long bookId, Integer rating, String content, Long parentId);
    List<CommentVO> getCommentsByBookId(Long bookId, Long currentUserId);
    BookRatingVO getBookRating(Long bookId);
    void likeComment(Long userId, Long commentId);
    void unlikeComment(Long userId, Long commentId);
}
