package com.example.demo.services.categories;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.demo.entities.Category;
import com.example.demo.repositories.CategoriesRepository;
@Service
public class CategoriesService {
    @Autowired
    private CategoriesRepository categoriesRepository;

    public Page<Category> getAllCategories(Pageable pageable) {
        return categoriesRepository.findAll(pageable);
    }

    public Category getCategoryById(Long id) {
        return categoriesRepository.findById(id).orElse(null);
    }

    public Category saveCategory(Category category) {
        return categoriesRepository.save(category);
    }

    public void deleteCategory(Long id) {
        categoriesRepository.deleteById(id);
    }
}
