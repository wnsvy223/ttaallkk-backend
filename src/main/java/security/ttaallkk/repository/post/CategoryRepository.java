package security.ttaallkk.repository.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import security.ttaallkk.domain.post.Category;

public interface CategoryRepository extends JpaRepository<Category, Long>, QuerydslPredicateExecutor<Category>{
    
}
