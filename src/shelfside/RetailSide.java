package shelfside;

import java.util.ArrayList;
import java.util.List;

/**
 * The retailer (store manager) side of the app.
 *
 * The real work is done by small methods (addItem, removeItem, updateQuantity,
 * updatePrice, search) that are easy to test. The {@link #run} method is just
 * the menu that reads input and calls those methods. Every change is saved to
 * the file right away so it survives restarting the app.
 */
public class RetailSide {

    private final List<Item> inventory;
    private final Storage storage;
    private final Console console;

    public RetailSide(List<Item> inventory, Storage storage, Console console) {
        this.inventory = inventory;
        this.storage = storage;
        this.console = console;
    }

    // ------------------------- logic (easy to test) -------------------------

    /** Finds a product by id, or returns null if there is no such product. */
    public Item findById(int id) {
        for (Item item : inventory) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }

    /** The id to give the next new product: one more than the current highest. */
    public int nextId() {
        int max = 0;
        for (Item item : inventory) {
            if (item.getId() > max) {
                max = item.getId();
            }
        }
        return max + 1;
    }

    /** Returns items whose name contains the term (ignoring case). */
    public List<Item> search(String term) {
        String needle = (term == null) ? "" : term.trim().toLowerCase();
        List<Item> hits = new ArrayList<Item>();
        for (Item item : inventory) {
            if (item.getName().toLowerCase().contains(needle)) {
                hits.add(item);
            }
        }
        return hits;
    }

    /**
     * Adds a new product. The {@link Item} constructor validates the values, so
     * this throws IllegalArgumentException for a blank name, negative price, or
     * negative quantity.
     */
    public Item addItem(String name, int priceCents, int quantity, String description) throws StorageException {
        Item item = new Item(nextId(), name, priceCents, quantity, description);
        inventory.add(item);
        storage.save(inventory);
        return item;
    }

    /** Removes the product with the given id. Returns false if it was not found. */
    public boolean removeItem(int id) throws StorageException {
        Item item = findById(id);
        if (item == null) {
            return false;
        }
        inventory.remove(item);
        storage.save(inventory);
        return true;
    }

    /** Sets a new stock quantity. Returns false if the product was not found. */
    public boolean updateQuantity(int id, int newQuantity) throws StorageException {
        Item item = findById(id);
        if (item == null) {
            return false;
        }
        item.setQuantity(newQuantity); // throws if negative
        storage.save(inventory);
        return true;
    }

    /** Sets a new price in cents. Returns false if the product was not found. */
    public boolean updatePrice(int id, int newPriceCents) throws StorageException {
        Item item = findById(id);
        if (item == null) {
            return false;
        }
        item.setPriceCents(newPriceCents); // throws if negative
        storage.save(inventory);
        return true;
    }

    // ------------------------------- menu ----------------------------------

    /** Shows the retailer menu until the user chooses to go back. */
    public void run() {
        while (true) {
            console.blank();
            console.println("--- Retailer Menu ---");
            console.println("1) View inventory");
            console.println("2) Search inventory");
            console.println("3) Add product");
            console.println("4) Remove product");
            console.println("5) Update stock quantity");
            console.println("6) Update price");
            console.println("0) Back to main menu");
            int choice = console.readIntInRange("Choose an option: ", 0, 6);
            switch (choice) {
                case 1: viewInventory();  break;
                case 2: doSearch();       break;
                case 3: doAdd();          break;
                case 4: doRemove();       break;
                case 5: doUpdateQuantity(); break;
                case 6: doUpdatePrice();  break;
                case 0: return;
                default: break;
            }
        }
    }

    private void viewInventory() {
        printList(inventory);
    }

    private void doSearch() {
        String term = console.readLine("Search for: ");
        printList(search(term));
    }

    private void doAdd() {
        String name = console.readLine("Name: ");
        String priceText = console.readLine("Price (like 8.99): ");
        String quantityText = console.readLine("Quantity: ");
        String description = console.readLine("Description (optional): ");
        try {
            int priceCents = Item.parsePriceToCents(priceText);
            int quantity = parseWholeNumber(quantityText, "Quantity");
            Item added = addItem(name, priceCents, quantity, description);
            console.println("Added " + added.shortLine());
        } catch (IllegalArgumentException e) {
            console.println("Could not add product: " + e.getMessage());
        } catch (StorageException e) {
            console.println("Saved failed: " + e.getMessage());
        }
    }

    private void doRemove() {
        int id = console.readIntInRange("Product id to remove: ", 0, Integer.MAX_VALUE);
        try {
            if (removeItem(id)) {
                console.println("Removed product #" + id + ".");
            } else {
                console.println("No product has id #" + id + ".");
            }
        } catch (StorageException e) {
            console.println("Save failed: " + e.getMessage());
        }
    }

    private void doUpdateQuantity() {
        int id = console.readIntInRange("Product id: ", 0, Integer.MAX_VALUE);
        if (findById(id) == null) {
            console.println("No product has id #" + id + ".");
            return;
        }
        String text = console.readLine("New stock quantity: ");
        try {
            int quantity = parseWholeNumber(text, "Quantity");
            updateQuantity(id, quantity);
            console.println("Updated. " + findById(id).shortLine());
        } catch (IllegalArgumentException e) {
            console.println("Could not update: " + e.getMessage());
        } catch (StorageException e) {
            console.println("Save failed: " + e.getMessage());
        }
    }

    private void doUpdatePrice() {
        int id = console.readIntInRange("Product id: ", 0, Integer.MAX_VALUE);
        if (findById(id) == null) {
            console.println("No product has id #" + id + ".");
            return;
        }
        String text = console.readLine("New price (like 8.99): ");
        try {
            int priceCents = Item.parsePriceToCents(text);
            updatePrice(id, priceCents);
            console.println("Updated. " + findById(id).shortLine());
        } catch (IllegalArgumentException e) {
            console.println("Could not update: " + e.getMessage());
        } catch (StorageException e) {
            console.println("Save failed: " + e.getMessage());
        }
    }

    private void printList(List<Item> items) {
        if (items.isEmpty()) {
            console.println("(no products)");
            return;
        }
        for (Item item : items) {
            console.println(item.shortLine());
        }
    }

    /** Parses a non-negative whole number, throwing a clear message if invalid. */
    private static int parseWholeNumber(String text, String label) {
        try {
            int value = Integer.parseInt(text.trim());
            if (value < 0) {
                throw new IllegalArgumentException(label + " cannot be negative.");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " must be a whole number: " + text);
        }
    }
}
