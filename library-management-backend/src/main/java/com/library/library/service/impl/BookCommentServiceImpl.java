package com.library.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.library.dto.CommentDTO;
import com.library.library.entity.Book;
import com.library.library.entity.BookComment;
import com.library.library.entity.CommentLike;
import com.library.library.entity.User;
import com.library.library.mapper.BookCommentMapper;
import com.library.library.mapper.CommentLikeMapper;
import com.library.library.service.BookCommentService;
import com.library.library.service.UserService;
import com.library.library.vo.BookRatingVO;
import com.library.library.vo.CommentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookCommentServiceImpl extends ServiceImpl<BookCommentMapper, BookComment> implements BookCommentService {

    @Autowired
    private CommentLikeMapper commentLikeMapper;

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public void addComment(Long userId, Long bookId, Integer rating, String content, Long parentId) {
        BookComment comment = new BookComment();
        comment.setBookId(bookId);
        comment.setUserId(userId);
        comment.setRating(rating);
        comment.setContent(content);
        comment.setParentId(parentId);
        comment.setLikeCount(0);
        comment.setReplyCount(0);
        
        if (parentId != null) {
            BookComment parentComment = getById(parentId);
            if (parentComment != null) {
                parentComment.setReplyCount(parentComment.getReplyCount() + 1);
                updateById(parentComment);
            }
        }
        
        save(comment);
    }

    @Override
    public List<CommentVO> getCommentsByBookId(Long bookId, Long currentUserId) {
        LambdaQueryWrapper<BookComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookComment::getBookId, bookId)
               .orderByDesc(BookComment::getCreateTime);
        List<BookComment> comments = list(wrapper);
        
        Set<Long> userIds = comments.stream().map(BookComment::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap;
        if (!userIds.isEmpty()) {
            LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.in(User::getId, userIds);
            List<User> users = userService.list(userWrapper);
            userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        } else {
            userMap = new HashMap<>();
        }
        
        Set<Long> likedCommentIds;
        if (currentUserId != null) {
            LambdaQueryWrapper<CommentLike> likeWrapper = new LambdaQueryWrapper<>();
            likeWrapper.eq(CommentLike::getUserId, currentUserId);
            List<CommentLike> likes = commentLikeMapper.selectList(likeWrapper);
            likedCommentIds = likes.stream().map(CommentLike::getCommentId).collect(Collectors.toSet());
        } else {
            likedCommentIds = new HashSet<>();
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        return comments.stream().map(c -> {
            CommentVO vo = new CommentVO();
            vo.setId(c.getId());
            vo.setBookId(c.getBookId());
            vo.setUserId(c.getUserId());
            vo.setRating(c.getRating());
            vo.setContent(c.getContent());
            vo.setLikeCount(c.getLikeCount());
            vo.setReplyCount(c.getReplyCount());
            vo.setParentId(c.getParentId());
            vo.setIsLiked(likedCommentIds.contains(c.getId()));
            vo.setCreateTime(c.getCreateTime().format(formatter));
            
            User user = userMap.get(c.getUserId());
            if (user != null) {
                vo.setUsername(user.getRealName() != null ? user.getRealName() : user.getUsername());
                vo.setUserAvatar(user.getAvatar());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public BookRatingVO getBookRating(Long bookId) {
        LambdaQueryWrapper<BookComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookComment::getBookId, bookId);
        List<BookComment> comments = list(wrapper);
        
        BookRatingVO vo = new BookRatingVO();
        
        if (comments.isEmpty()) {
            vo.setAverageRating(0.0);
            vo.setTotalComments(0);
            vo.setRating5(0);
            vo.setRating4(0);
            vo.setRating3(0);
            vo.setRating2(0);
            vo.setRating1(0);
            return vo;
        }
        
        int total = comments.size();
        double sum = comments.stream().mapToInt(BookComment::getRating).sum();
        vo.setAverageRating(Math.round(sum / total * 10) / 10.0);
        vo.setTotalComments(total);
        
        Map<Integer, Long> ratingCount = comments.stream()
            .collect(Collectors.groupingBy(BookComment::getRating, Collectors.counting()));
        
        vo.setRating5(ratingCount.getOrDefault(5, 0L).intValue());
        vo.setRating4(ratingCount.getOrDefault(4, 0L).intValue());
        vo.setRating3(ratingCount.getOrDefault(3, 0L).intValue());
        vo.setRating2(ratingCount.getOrDefault(2, 0L).intValue());
        vo.setRating1(ratingCount.getOrDefault(1, 0L).intValue());
        
        return vo;
    }

    @Override
    @Transactional
    public void likeComment(Long userId, Long commentId) {
        LambdaQueryWrapper<CommentLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommentLike::getCommentId, commentId)
               .eq(CommentLike::getUserId, userId);
        CommentLike existing = commentLikeMapper.selectOne(wrapper);
        
        if (existing == null) {
            CommentLike like = new CommentLike();
            like.setCommentId(commentId);
            like.setUserId(userId);
            try {
                commentLikeMapper.insert(like);
                
                BookComment comment = getById(commentId);
                if (comment != null) {
                    comment.setLikeCount(comment.getLikeCount() + 1);
                    updateById(comment);
                }
            } catch (Exception e) {
                // 忽略重复键异常（已点赞）
            }
        }
    }

    @Override
    @Transactional
    public void unlikeComment(Long userId, Long commentId) {
        LambdaQueryWrapper<CommentLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommentLike::getCommentId, commentId)
               .eq(CommentLike::getUserId, userId);
        CommentLike existing = commentLikeMapper.selectOne(wrapper);
        
        if (existing != null) {
            commentLikeMapper.deleteById(existing.getId());
            
            BookComment comment = getById(commentId);
            if (comment != null && comment.getLikeCount() > 0) {
                comment.setLikeCount(comment.getLikeCount() - 1);
                updateById(comment);
            }
        }
    }
}
