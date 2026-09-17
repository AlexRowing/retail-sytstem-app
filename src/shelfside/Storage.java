package shelfside;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads and writes the local JSON inventory/category files.
 * This is a small hand-written reader/writer (no external JSON library) since
 * it only ever needs to round-trip the fixed schema this class itself produces.
 */
public class Storage {
    private String filePath;
    private String categoriesFilePath;
    private HashMap<String, Item> inventoryMap;

    private static final Pattern ITEM_PATTERN = Pattern.compile(
            "\"([^\"]+)\"\\s*:\\s*\\{\\s*\"stock\"\\s*:\\s*(-?\\d+)\\s*,\\s*\"price\"\\s*:\\s*(-?[\\d.]+)\\s*,\\s*\"category\"\\s*:\\s*\"([^\"]*)\"\\s*}");
    private static final Pattern CATEGORY_PATTERN = Pattern.compile("\"([^\"]*)\"");

    public Storage(String filePath, String categoriesFilePath) {
        this.filePath = filePath;
        this.categoriesFilePath = categoriesFilePath;
        this.inventoryMap = new HashMap<>();
    }

    public HashMap<String, Item> loadInventory() {
        inventoryMap = new HashMap<>();
        try {
            String content = Files.readString(Path.of(filePath));
            Matcher matcher = ITEM_PATTERN.matcher(content);
            while (matcher.find()) {
                String name = matcher.group(1);
                int stock = Integer.parseInt(matcher.group(2));
                double price = Double.parseDouble(matcher.group(3));
                String category = matcher.group(4);
                if (!Categories.exists(category)) {
                    Categories.addCategory(category);
                }
                inventoryMap.put(name, new Item(name, stock, price, category));
            }
        } catch (IOException e) {
            try {
                Files.writeString(Path.of(filePath), "{}");
            } catch (IOException ignored) {
                // leave inventoryMap empty; program keeps running
            }
        }
        return inventoryMap;
    }

    public void saveInventory(HashMap<String, Item> activeRepo) {
        if (activeRepo == null) {
            throw new IllegalArgumentException("Inventory data cannot be null");
        }

        StringBuilder json = new StringBuilder("{\n");
        int i = 0;
        for (Item item : activeRepo.values()) {
            json.append("  \"").append(item.getItemName()).append("\": ").append(item.toJson());
            if (++i < activeRepo.size()) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("}");

        try {
            Files.writeString(Path.of(filePath), json.toString());
        } catch (IOException e) {
            System.out.println("Failed to save inventory: " + e.getMessage());
        }

        this.inventoryMap = activeRepo;
    }

    public Set<String> loadCategories() {
        try {
            String content = Files.readString(Path.of(categoriesFilePath));
            Set<String> loaded = new LinkedHashSet<>();
            Matcher matcher = CATEGORY_PATTERN.matcher(content);
            while (matcher.find()) {
                loaded.add(matcher.group(1));
            }
            return loaded;
        } catch (IOException e) {
            try {
                Files.writeString(Path.of(categoriesFilePath), "[]");
            } catch (IOException ignored) {
                // leave categories empty; program keeps running
            }
            return new LinkedHashSet<>();
        }
    }

    public void saveCategories(Set<String> categories) {
        StringBuilder json = new StringBuilder("[");
        int i = 0;
        for (String category : categories) {
            json.append("\"").append(category).append("\"");
            if (++i < categories.size()) {
                json.append(", ");
            }
        }
        json.append("]");

        try {
            Files.writeString(Path.of(categoriesFilePath), json.toString());
        } catch (IOException e) {
            System.out.println("Failed to save categories: " + e.getMessage());
        }
    }
}
