import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import main.java.com.example.demo.models.categories;
import main.java.com.example.demo.repositories.CategoriesRepository;    
@Service
public class CategoriesService {
    @Autowired
    private CategoriesRepository categoriesRepository;

    public List<categories> getAllCategories() {
        return categoriesRepository.findAll();
    }
    
    public categories getCategoryById(Long id) {
        return categoriesRepository.findById(id).orElse(null);
    }
    
    public categories saveCategory(categories category) {
        return categoriesRepository.save(category);
    }
    
    public void deleteCategory(Long id) {
        categoriesRepository.deleteById(id);
    }
}
