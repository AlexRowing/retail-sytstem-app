package shelfside;

import shelfside.exceptions.EmptyCartException;
import shelfside.exceptions.NoResultsException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ClientSide {
    private static final double TAX_RATE = 0.08;
    private static final Map<String, Double> PROMO_CODES = Map.of(
            "SAVE10", 0.10,
            "SAVE20", 0.20);

    private Scanner scanner;
    private Storage storage;
    private HashMap<String, Item> inventory;
    private LinkedHashMap<String, Integer> cart;

    public ClientSide(Scanner scanner, Storage storage, HashMap<String, Item> inventory) {
        this.scanner = scanner;
        this.storage = storage;
        this.inventory = inventory;
        this.cart = new LinkedHashMap<>();
    }

    public void viewProducts() {
        if (inventory.isEmpty()) {
            System.out.println("No products available.");
            return;
        }
        System.out.println("---- Product Catalog ----");
        for (Item item : inventory.values()) {
            System.out.println(item);
        }
    }

    public void searchProducts() {
        System.out.print("Search by name or category: ");
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
            throw new NoResultsException("No products found matching \"" + query + "\".");
        }
        return results;
    }

    public void viewProductDetails() {
        System.out.print("Enter item name: ");
        String name = scanner.nextLine().trim();
        Item item = inventory.get(name);
        if (item == null) {
            System.out.println("Item \"" + name + "\" not found.");
            return;
        }
        System.out.println(item);
    }

    public void addToCart() {
        System.out.print("Enter item name to add to cart: ");
        String name = scanner.nextLine().trim();
        Item item = inventory.get(name);
        if (item == null) {
            System.out.println("Item \"" + name + "\" not found.");
            return;
        }

        System.out.print("Enter quantity: ");
        int quantity = readPositiveInt();

        int alreadyInCart = cart.getOrDefault(name, 0);
        int available = item.getStock() - alreadyInCart;
        if (quantity > available) {
            System.out.println("Only " + available + " unit(s) of \"" + name
                    + "\" available. Adding " + Math.max(available, 0) + " instead.");
            quantity = available;
        }
        if (quantity <= 0) {
            System.out.println("Cannot add \"" + name + "\": no stock available.");
            return;
        }

        cart.merge(name, quantity, Integer::sum);
        System.out.println("Added " + quantity + " x \"" + name + "\" to cart.");
    }

    public void viewCart() {
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }
        System.out.println("---- Your Cart ----");
        double subtotal = 0;
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            Item item = inventory.get(entry.getKey());
            double lineTotal = item.getPrice() * entry.getValue();
            subtotal += lineTotal;
            System.out.printf("%s x%d - $%.2f%n", entry.getKey(), entry.getValue(), lineTotal);
        }
        System.out.printf("Subtotal: $%.2f%n", subtotal);
    }

    public void clearCart() {
        cart.clear();
        System.out.println("Cart cleared.");
    }

    public void checkout() {
        try {
            if (cart.isEmpty()) {
                throw new EmptyCartException("Your cart is empty. Add items before checking out.");
            }

            double subtotal = 0;
            for (Map.Entry<String, Integer> entry : cart.entrySet()) {
                Item item = inventory.get(entry.getKey());
                subtotal += item.getPrice() * entry.getValue();
            }

            System.out.printf("Subtotal: $%.2f%n", subtotal);
            System.out.print("Enter promo code (or press Enter to skip): ");
            String promo = scanner.nextLine().trim().toUpperCase();
            double discountRate = PROMO_CODES.getOrDefault(promo, 0.0);
            if (!promo.isEmpty() && discountRate == 0.0) {
                System.out.println("Invalid promo code. No discount applied.");
            }

            double discount = subtotal * discountRate;
            double tax = (subtotal - discount) * TAX_RATE;
            double total = subtotal - discount + tax;

            for (Map.Entry<String, Integer> entry : cart.entrySet()) {
                Item item = inventory.get(entry.getKey());
                item.updateStock(item.getStock() - entry.getValue());
            }
            storage.saveInventory(inventory);

            printReceipt(subtotal, discount, tax, total);
            System.out.printf("Total charged: $%.2f%n", total);
            cart.clear();
        } catch (EmptyCartException e) {
            System.out.println(e.getMessage());
        }
    }

    private void printReceipt(double subtotal, double discount, double tax, double total) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("========== Shelf Side Receipt ==========\n");
        receipt.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        receipt.append("-----------------------------------------\n");
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            Item item = inventory.get(entry.getKey());
            receipt.append(String.format("%-20s x%-3d $%.2f%n",
                    entry.getKey(), entry.getValue(), item.getPrice() * entry.getValue()));
        }
        receipt.append("-----------------------------------------\n");
        receipt.append(String.format("Subtotal: $%.2f%n", subtotal));
        receipt.append(String.format("Discount: -$%.2f%n", discount));
        receipt.append(String.format("Tax:      $%.2f%n", tax));
        receipt.append(String.format("Total:    $%.2f%n", total));
        receipt.append("=========================================\n");

        System.out.print(receipt);

        String fileName = "receipt_" + System.currentTimeMillis() + ".txt";
        try {
            Files.writeString(Path.of(fileName), receipt.toString());
            System.out.println("Receipt saved to " + fileName);
        } catch (IOException e) {
            System.out.println("Could not save receipt file: " + e.getMessage());
        }
    }

    private int readPositiveInt() {
        while (true) {
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value <= 0) {
                    System.out.print("Enter a positive quantity: ");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid whole number: ");
            }
        }
    }
}
