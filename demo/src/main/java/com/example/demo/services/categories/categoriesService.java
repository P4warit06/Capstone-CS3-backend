package com.example.demo.services.categories;

import org.springframework.stereotype.Service;

@Service
public class categoriesService {
    @Autowired
    private categoriesRepository categoriesRepository;

    public List<categories> getAllCategories() {
        return categoriesRepository.findAll();
    }
    
}
