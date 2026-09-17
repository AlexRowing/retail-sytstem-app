package shelfside;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The "categories bag" referenced in the project scope. Categories must be
 * registered here before an Item can be created or assigned to them.
 */
public final class Categories {
    private static final Set<String> categories = new LinkedHashSet<>();

    private Categories() {}

    public static boolean addCategory(String category) {
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }
        return categories.add(category);
    }

    public static boolean exists(String category) {
        return category != null && categories.contains(category);
    }

    public static Set<String> getAll() {
        return Collections.unmodifiableSet(categories);
    }

    public static void loadAll(Set<String> loaded) {
        categories.clear();
        if (loaded != null) {
            categories.addAll(loaded);
        }
    }
}
