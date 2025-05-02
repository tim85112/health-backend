package com.healthmanagement.dao.social;

import com.healthmanagement.model.member.User;
import com.healthmanagement.model.social.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumDAO extends JpaRepository<Post, Integer> {
    
    // 依照分類查文章
    List<Post> findByCategory(String category);
    
    // 依照使用者查文章
    List<Post> findByUserId(Integer userId);
    
    List<Post> findByUser(User user);
    
    @Query("SELECT FORMAT(p.createdAt, 'yyyy-MM') AS month, COUNT(p) " +
    	       "FROM Post p GROUP BY FORMAT(p.createdAt, 'yyyy-MM') ORDER BY month")
	List<Object[]> countPostByMonth();
    	
	@Query("SELECT p.user.name, COUNT(p) FROM Post p GROUP BY p.user.name ORDER BY COUNT(p) DESC")
	List<Object[]> countPostsByUser();	
}
