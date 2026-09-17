package shelfside;

import shelfside.exceptions.NoResultsException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class RetailSide {
    private Scanner scanner;
    private Storage storage;
    private HashMap<String, Item> inventory;

    public RetailSide(Scanner scanner, Storage storage) {
        this.scanner = scanner;
        this.storage = storage;
        this.inventory = storage.loadInventory();
    }

    public HashMap<String, Item> getInventory() {
        return inventory;
    }

    public void addCategory() {
        System.out.print("Enter new category name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Category name cannot be empty.");
            return;
        }
        if (Categories.exists(name)) {
            System.out.println("Category \"" + name + "\" already exists.");
            return;
        }
        Categories.addCategory(name);
        storage.saveCategories(Categories.getAll());
        System.out.println("Category \"" + name + "\" added.");
    }

    public void addProduct() {
        System.out.print("Enter item name: ");
        String name = scanner.nextLine().trim();

        if (inventory.containsKey(name)) {
            System.out.print("Item already exists. Enter quantity to add to existing stock: ");
            int addQty = readNonNegativeInt();
            Item existing = inventory.get(name);
            existing.updateStock(existing.getStock() + addQty);
            storage.saveInventory(inventory);
            System.out.println("Merged " + addQty + " units into existing item \"" + name + "\".");
            return;
        }

        System.out.print("Enter category: ");
        String category = scanner.nextLine().trim();
        if (!Categories.exists(category)) {
            System.out.println("Category \"" + category + "\" does not exist. Register it first (Add Category).");
            return;
        }

        System.out.print("Enter price: ");
        double price = readNonNegativeDouble();

        System.out.print("Enter stock quantity: ");
        int stock = readNonNegativeInt();

        try {
            Item item = new Item(name, stock, price, category);
            inventory.put(name, item);
            storage.saveInventory(inventory);
            System.out.println("Added item: " + item);
        } catch (IllegalArgumentException e) {
            System.out.println("Could not add item: " + e.getMessage());
        }
    }

    public void removeProduct() {
        System.out.print("Enter item name to remove: ");
        String name = scanner.nextLine().trim();
        if (inventory.remove(name) != null) {
            storage.saveInventory(inventory);
            System.out.println("Removed \"" + name + "\" from inventory.");
        } else {
            System.out.println("Item \"" + name + "\" not found.");
        }
    }

    public void viewStock() {
        if (inventory.isEmpty()) {
            System.out.println("Inventory is empty.");
            return;
        }
        System.out.println("---- Current Inventory ----");
        for (Item item : inventory.values()) {
            System.out.println(item);
        }
    }

    public void searchProduct() {
        System.out.print("Enter item name or category to search: ");
        String query = scanner.nextLine().trim().toLowerCase();
        try {
            List<Item> results = search(query);
            System.out.println("---- Search Results ----");
            for (Item item : results) {
                System.out.println(item);
            }
        } catch (NoResultsException e) {
            System.out.println(e.getMessage());
        }
    }

    private List<Item> search(String query) throws NoResultsException {
        List<Item> results = new ArrayList<>();
        for (Item item : inventory.values()) {
            if (item.getItemName().toLowerCase().contains(query)
                    || item.getCategory().toLowerCase().contains(query)) {
                results.add(item);
            }
        }
        if (results.isEmpty()) {
            throw new NoResultsException("No results found for \"" + query + "\".");
        }
        return results;
    }

    public void updateInventory() {
        System.out.print("Enter item name to update: ");
        String name = scanner.nextLine().trim();
        Item item = inventory.get(name);
        if (item == null) {
            System.out.println("Item \"" + name + "\" not found.");
            return;
        }

        System.out.println("1. Update price");
        System.out.println("2. Update stock");
        System.out.println("3. Update category");
        System.out.print("Choose an option: ");
        int choice = readNonNegativeInt();

        switch (choice) {
            case 1 -> {
                System.out.print("Enter new price: ");
                item.updatePrice(readNonNegativeDouble());
            }
            case 2 -> {
                System.out.print("Enter new stock: ");
                item.updateStock(readNonNegativeInt());
            }
            case 3 -> {
                System.out.print("Enter new category: ");
                String newCategory = scanner.nextLine().trim();
                try {
                    item.updateCategory(newCategory);
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                    return;
                }
            }
            default -> {
                System.out.println("Invalid option.");
                return;
            }
        }

        storage.saveInventory(inventory);
        System.out.println("Updated: " + item);
    }

    private int readNonNegativeInt() {
        while (true) {
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value < 0) {
                    System.out.print("Value cannot be negative. Try again: ");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid whole number: ");
            }
        }
    }

    private double readNonNegativeDouble() {
        while (true) {
            String line = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(line);
                if (value < 0) {
                    System.out.print("Value cannot be negative. Try again: ");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }
}
