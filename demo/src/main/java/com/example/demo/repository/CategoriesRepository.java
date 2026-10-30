package com.example.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.example.demo.entities.Category;
import java.util.List;
@Repository
public interface CategoriesRepository extends JpaRepository<Category, Long> {
    List<Category> findAll();
    Category save(Category category);
    void deleteById(Long id);
}
