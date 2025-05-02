package com.healthmanagement.service.social;

import com.healthmanagement.dao.social.CommentDAO;
import com.healthmanagement.dao.social.ForumDAO;
import com.healthmanagement.dto.social.CommentRequest;
import com.healthmanagement.model.social.Comment;
import com.healthmanagement.model.social.Post;
import com.healthmanagement.service.member.UserService;
import com.healthmanagement.service.fitness.AchievementService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentDAO commentDAO;
    
    @Autowired
    private ForumDAO forumDAO;

    @Autowired
    private UserService userService;

    @Autowired
    private AchievementService achievementService;


    @Override
    public List<Comment> getCommentsByPostId(Integer postId) {
        return commentDAO.findByPost_Id(postId);
    }

    @Override
    public List<Comment> getCommentsByUserId(Integer userId) {
    	return commentDAO.findByUser_Id(userId); 
    }

    @Override
    public Comment getCommentById(Integer commentId) {
        return commentDAO.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
    }
    
    @Autowired
    private UserActivityService userActivityService;
    
    @Override
    public Comment createComment(Integer postId, String email, CommentRequest request) {
        Comment comment = new Comment();
        comment.setText(request.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        comment.setPost(forumDAO.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found")));
        comment.setUser(userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found")));

        Comment saved = commentDAO.save(comment);

     // 成就邏輯
     long commentCount = commentDAO.countByUser_Id(saved.getUser().getUserId());
     achievementService.checkAndAwardAchievements(saved.getUser().getUserId(), "SOCIAL_COMMENT_CREATED", (int) commentCount);

     // 動態紀錄邏輯
     userActivityService.logActivity(
             saved.getUser().getUserId(),// 使用者 ID
             "comment",                     // 動作類型
             saved.getId()                  // 留言 ID 
     );

     return saved;

    }
    @Override
    public Comment updateComment(Integer commentId, String email, CommentRequest request) {
        Comment comment = getCommentById(commentId);

        // 檢查是否為留言本人
        if (!comment.getUser().getEmail().equals(email)) {
            throw new AccessDeniedException("您無權修改此留言！");
        }

        comment.setText(request.getContent());
        comment.setUpdatedAt(LocalDateTime.now());

        return commentDAO.save(comment);
    }

    @Override
    public void deleteComment(Integer commentId, String email) {
        Comment comment = getCommentById(commentId);
        if (!comment.getUser().getEmail().equals(email)) {
            throw new AccessDeniedException("您無權刪除此留言！");
        }
        commentDAO.deleteById(commentId);
    }
    
    @Override
    public int countByPost(Post post) {
        return commentDAO.findByPost_Id(post.getId()).size();
    }
}
