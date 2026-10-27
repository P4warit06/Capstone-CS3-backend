

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import main.java.com.example.demo.services.categories.CategoriesService;
import main.java.com.example.demo.models.categories;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
@RestController
@RequestMapping("/api/v1/categories")
public class CategorieController {
    @Autowired
    private CategoriesService categoriesService;

    @GetMapping("/categories")
    public List<categories> getAllCategories() {
        return categoriesService.getAllCategories();
    }
    @GetMapping("/categories/{id}")
    public categories getCategoryById(@PathVariable Long id) {
        return categoriesService.getCategoryById(id);
    }
    @PostMapping("/categories")
    public categories saveCategory(@RequestBody categories category) {
        return categoriesService.saveCategory(category);
    }
    @DeleteMapping("/categories/{id}")
    public void deleteCategory(@PathVariable Long id) {
        categoriesService.deleteCategory(id);
    }
}
