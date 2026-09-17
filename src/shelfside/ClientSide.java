package shelfside;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The customer (shopper) side of the app.
 *
 * The cart is a map from product id to how many the customer wants. As with the
 * retailer side, the real work is in small testable methods (addToCart,
 * cartTotalCents, checkout) and {@link #run} is just the menu around them.
 */
public class ClientSide {

    private final List<Item> inventory;
    private final Storage storage;
    private final Console console;

    /** product id -> quantity the customer wants to buy */
    private final Map<Integer, Integer> cart = new LinkedHashMap<Integer, Integer>();

    public ClientSide(List<Item> inventory, Storage storage, Console console) {
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

    /** Returns products whose name contains the term (ignoring case). */
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

    /** A read-only-ish copy of the cart, for display and tests. */
    public Map<Integer, Integer> getCart() {
        return cart;
    }

    /**
     * Adds a quantity of a product to the cart.
     *
     * @throws IllegalArgumentException if the product does not exist, the
     *         quantity is not positive, or the amount asked for (added to what
     *         is already in the cart) is more than the stock on hand.
     */
    public void addToCart(int id, int quantity) {
        Item item = findById(id);
        if (item == null) {
            throw new IllegalArgumentException("No product has id #" + id + ".");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
        int alreadyInCart = cart.containsKey(id) ? cart.get(id) : 0;
        if (alreadyInCart + quantity > item.getQuantity()) {
            throw new IllegalArgumentException(
                "Only " + item.getQuantity() + " of '" + item.getName() + "' in stock"
                + (alreadyInCart > 0 ? " (you already have " + alreadyInCart + " in your cart)." : "."));
        }
        cart.put(id, alreadyInCart + quantity);
    }

    /** Total price of everything in the cart, in cents. */
    public int cartTotalCents() {
        int total = 0;
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            Item item = findById(entry.getKey());
            if (item != null) {
                total += item.getPriceCents() * entry.getValue();
            }
        }
        return total;
    }

    /**
     * Completes the sale: reduces stock by the cart amounts, saves, and empties
     * the cart. Returns false if the cart is empty.
     *
     * @throws IllegalArgumentException if stock changed and is now too low.
     * @throws StorageException if saving fails (stock is left unchanged).
     */
    public boolean checkout() throws StorageException {
        if (cart.isEmpty()) {
            return false;
        }
        // Check everything still fits BEFORE changing any stock.
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            Item item = findById(entry.getKey());
            if (item == null) {
                throw new IllegalArgumentException("A product in your cart no longer exists.");
            }
            if (entry.getValue() > item.getQuantity()) {
                throw new IllegalArgumentException(
                    "Only " + item.getQuantity() + " of '" + item.getName() + "' left.");
            }
        }
        // Now it is safe to reduce stock.
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            findById(entry.getKey()).reduceStock(entry.getValue());
        }
        storage.save(inventory);
        cart.clear();
        return true;
    }

    // ------------------------------- menu ----------------------------------

    /** Shows the customer menu until the user chooses to go back. */
    public void run() {
        while (true) {
            console.blank();
            console.println("--- Customer Menu ---");
            console.println("1) View products");
            console.println("2) Search products");
            console.println("3) View product details");
            console.println("4) Add item to cart");
            console.println("5) View cart");
            console.println("6) Checkout");
            console.println("0) Back to main menu");
            int choice = console.readIntInRange("Choose an option: ", 0, 6);
            switch (choice) {
                case 1: printList(inventory); break;
                case 2: doSearch();           break;
                case 3: doDetails();          break;
                case 4: doAddToCart();        break;
                case 5: viewCart();           break;
                case 6: doCheckout();         break;
                case 0: return;
                default: break;
            }
        }
    }

    private void doSearch() {
        String term = console.readLine("Search for: ");
        printList(search(term));
    }

    private void doDetails() {
        int id = console.readIntInRange("Product id: ", 0, Integer.MAX_VALUE);
        Item item = findById(id);
        if (item == null) {
            console.println("No product has id #" + id + ".");
        } else {
            console.println(item.details());
        }
    }

    private void doAddToCart() {
        int id = console.readIntInRange("Product id: ", 0, Integer.MAX_VALUE);
        String quantityText = console.readLine("How many: ");
        try {
            int quantity = Integer.parseInt(quantityText.trim());
            addToCart(id, quantity);
            console.println("Added to cart.");
        } catch (NumberFormatException e) {
            console.println("Quantity must be a whole number: " + quantityText);
        } catch (IllegalArgumentException e) {
            console.println(e.getMessage());
        }
    }

    private void viewCart() {
        if (cart.isEmpty()) {
            console.println("Your cart is empty.");
            return;
        }
        console.println("--- Your Cart ---");
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            Item item = findById(entry.getKey());
            int lineCents = item.getPriceCents() * entry.getValue();
            console.println(String.format("%-20s x%d  = %s",
                item.getName(), entry.getValue(), Item.formatCents(lineCents)));
        }
        console.println("Total: " + Item.formatCents(cartTotalCents()));
    }

    private void doCheckout() {
        if (cart.isEmpty()) {
            console.println("Your cart is empty.");
            return;
        }
        viewCart();
        String confirm = console.readLine("Type 'yes' to complete checkout: ");
        if (!confirm.equalsIgnoreCase("yes")) {
            console.println("Checkout cancelled.");
            return;
        }
        try {
            int paid = cartTotalCents();
            checkout();
            console.println("Thank you! You paid " + Item.formatCents(paid) + ". Stock updated.");
        } catch (IllegalArgumentException e) {
            console.println("Checkout stopped: " + e.getMessage());
        } catch (StorageException e) {
            console.println("Save failed, sale not recorded: " + e.getMessage());
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
}
