package com.example.demo.controllers;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.demo.services.categories.CategoriesService;
import com.example.demo.entity.Category;
import com.example.demo.dtos.CategoryDto;
import java.util.List;


@RestController
@RequestMapping("/v1/categories")
public class CategoryController {
    @Autowired
    private CategoriesService categoriesService;

    // SCRUM-218: รายการหมวดทั้งหมด (ไม่แบ่งหน้า) ให้ FE ใช้สร้างตัวเลือก filter
    @GetMapping
    public List<CategoryDto> getAllCategories() {
        return categoriesService.getAllCategoryDtos();
    }
    @GetMapping("/{id}")
    public Category getCategoryById(@PathVariable Long id) {
        return categoriesService.getCategoryById(id);
    }
    @PostMapping
    public Category saveCategory(@RequestBody Category category) {
        return categoriesService.saveCategory(category);
    }
    @DeleteMapping("/{id}")
    public void deleteCategory(@PathVariable Long id) {
        categoriesService.deleteCategory(id);
    }
}
