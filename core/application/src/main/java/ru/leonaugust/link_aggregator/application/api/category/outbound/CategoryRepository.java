package ru.leonaugust.link_aggregator.application.api.category.outbound;

import java.util.List;
import ru.leonaugust.link_aggregator.domain.category.Category;

public interface CategoryRepository {

  List<Category> getCategories();

}
