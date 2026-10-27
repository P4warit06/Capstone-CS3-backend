package main.java.com.example.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
    public interface restrepo extends JpaRepository<courses, Long> {
    List<courses> findByCategoryId(Long categoryId);
}
