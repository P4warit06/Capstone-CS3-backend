package com.example.demo.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
public class CategorieController {
    @Autowired
    private categoriesService categoriesService;

    @GetMapping("/categories")
    public List<categories> getAllCategories() {
        return categoriesService.getAllCategories();
    }
}
