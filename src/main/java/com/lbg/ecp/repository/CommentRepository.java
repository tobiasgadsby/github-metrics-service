package com.lbg.ecp.repository;

import com.lbg.ecp.entities.tables.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {}
