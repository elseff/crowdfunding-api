package ru.elseff.crowdfoundingapi.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.elseff.crowdfoundingapi.dao.entity.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

}
