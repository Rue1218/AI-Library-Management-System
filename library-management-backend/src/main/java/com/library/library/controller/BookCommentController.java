package com.library.library.controller;

import com.library.library.config.UserContextHolder;
import com.library.library.dto.CommentDTO;
import com.library.library.service.BookCommentService;
import com.library.library.vo.BookRatingVO;
import com.library.library.vo.CommentVO;
import com.library.library.vo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class BookCommentController {

    private static final Logger logger = LoggerFactory.getLogger(BookCommentController.class);

    @Autowired
    private BookCommentService bookCommentService;

    @PostMapping
    public Result<Void> addComment(@RequestBody CommentDTO dto) {
        try {
            Long userId = UserContextHolder.getUserId();
            
            bookCommentService.addComment(userId, dto.getBookId(), dto.getRating(), 
                                         dto.getContent(), dto.getParentId());
            return Result.success(null);
        } catch (Exception e) {
            logger.error("添加评论失败", e);
            return Result.error("添加评论失败: " + e.getMessage());
        }
    }

    @GetMapping("/book/{bookId}")
    public Result<List<CommentVO>> getComments(@PathVariable Long bookId) {
        try {
            Long userId = UserContextHolder.getUserId();
            List<CommentVO> comments = bookCommentService.getCommentsByBookId(bookId, userId);
            return Result.success(comments);
        } catch (Exception e) {
            logger.error("获取评论失败", e);
            return Result.error("获取评论失败: " + e.getMessage());
        }
    }

    @GetMapping("/book/{bookId}/rating")
    public Result<BookRatingVO> getBookRating(@PathVariable Long bookId) {
        try {
            BookRatingVO rating = bookCommentService.getBookRating(bookId);
            return Result.success(rating);
        } catch (Exception e) {
            logger.error("获取评分失败", e);
            return Result.error("获取评分失败: " + e.getMessage());
        }
    }

    @PostMapping("/{commentId}/like")
    public Result<Void> likeComment(@PathVariable Long commentId) {
        try {
            Long userId = UserContextHolder.getUserId();
            
            bookCommentService.likeComment(userId, commentId);
            return Result.success(null);
        } catch (Exception e) {
            logger.error("点赞失败", e);
            return Result.error("点赞失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{commentId}/like")
    public Result<Void> unlikeComment(@PathVariable Long commentId) {
        try {
            Long userId = UserContextHolder.getUserId();
            
            bookCommentService.unlikeComment(userId, commentId);
            return Result.success(null);
        } catch (Exception e) {
            logger.error("取消点赞失败", e);
            return Result.error("取消点赞失败: " + e.getMessage());
        }
    }
}
