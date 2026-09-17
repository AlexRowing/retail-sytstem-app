package shelfside;

public class Item {
    private String itemName;
    private int stock;
    private double price;
    private String category;

    public Item(String itemName, int stock, double price, String category) {
        if (itemName == null || itemName.isBlank()) {
            throw new IllegalArgumentException("Item name cannot be null or empty");
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Category cannot be null or empty");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (!Categories.exists(category)) {
            throw new IllegalArgumentException(
                    "Category \"" + category + "\" does not exist. Register it first.");
        }

        this.itemName = itemName;
        this.stock = stock;
        this.price = price;
        this.category = category;
    }

    public String getItemName() {
        return itemName;
    }

    public int getStock() {
        return stock;
    }

    public double getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public void updatePrice(double newPrice) {
        if (newPrice < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.price = newPrice;
    }

    public void updateCategory(String newCategory) {
        if (!Categories.exists(newCategory)) {
            throw new IllegalArgumentException(
                    "Category \"" + newCategory + "\" does not exist. Register it first.");
        }
        this.category = newCategory;
    }

    public void updateStock(int newStock) {
        if (newStock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        this.stock = newStock;
    }

    /** Returns this item's data (excluding its name, which is the map key) as a JSON object. */
    public String toJson() {
        return String.format("{\"stock\": %d, \"price\": %.2f, \"category\": \"%s\"}",
                stock, price, category);
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - $%.2f, %d in stock", itemName, category, price, stock);
    }
}
