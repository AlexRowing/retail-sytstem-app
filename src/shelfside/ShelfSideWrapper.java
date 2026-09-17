package shelfside;

import java.util.Scanner;

/**
 * Program's main menu interface loop. Routes the user into either the
 * ClientSide shopping loop or the RetailSide login/management flow.
 */
public class ShelfSideWrapper {
    private static final String RETAILER_USERNAME = "admin";
    private static final String RETAILER_PASSWORD = "admin123";

    private Scanner scanner;
    private RetailSide retailSide;
    private ClientSide clientSide;
    private LoginProcessor loginProcessor;

    public ShelfSideWrapper() {
        this.scanner = new Scanner(System.in);

        Storage storage = new Storage("inventory.json", "categories.json");
        Categories.loadAll(storage.loadCategories());
        if (Categories.getAll().isEmpty()) {
            Categories.addCategory("General");
            storage.saveCategories(Categories.getAll());
        }

        this.retailSide = new RetailSide(scanner, storage);
        this.clientSide = new ClientSide(scanner, storage, retailSide.getInventory());
        this.loginProcessor = new LoginProcessor(RETAILER_USERNAME, RETAILER_PASSWORD);
    }

    public void startApp() {
        boolean running = true;
        while (running) {
            System.out.println("\n===== Shelf Side =====");
            System.out.println("1. Retailer Login");
            System.out.println("2. Shop as Customer");
            System.out.println("3. Exit");
            System.out.print("Select an option: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            if (choice == 3) {
                running = false;
                System.out.println("Goodbye!");
                continue;
            }
            routeUser(choice);
        }
        scanner.close();
    }

    public void routeUser(int choice) {
        switch (choice) {
            case 1 -> retailerLogin();
            case 2 -> customerMenu();
            default -> System.out.println("Select from option 1, 2, or 3.");
        }
    }

    private void retailerLogin() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        if (!loginProcessor.authenticate(username, password)) {
            System.out.println("Invalid credentials.");
            return;
        }

        boolean inRetailMenu = true;
        while (inRetailMenu) {
            System.out.println("\n---- Retailer Menu ----");
            System.out.println("1. Add Category");
            System.out.println("2. Add Product");
            System.out.println("3. Remove Product");
            System.out.println("4. View Stock");
            System.out.println("5. Search Product");
            System.out.println("6. Update Inventory");
            System.out.println("7. Log Out");
            System.out.print("Select an option: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                continue;
            }

            switch (choice) {
                case 1 -> retailSide.addCategory();
                case 2 -> retailSide.addProduct();
                case 3 -> retailSide.removeProduct();
                case 4 -> retailSide.viewStock();
                case 5 -> retailSide.searchProduct();
                case 6 -> retailSide.updateInventory();
                case 7 -> inRetailMenu = false;
                default -> System.out.println("Select a valid option (1-7).");
            }
        }
    }

    private void customerMenu() {
        boolean inCustomerMenu = true;
        while (inCustomerMenu) {
            System.out.println("\n---- Shopping Menu ----");
            System.out.println("1. View Products");
            System.out.println("2. Search Products");
            System.out.println("3. View Product Details");
            System.out.println("4. Add to Cart");
            System.out.println("5. View Cart");
            System.out.println("6. Clear Cart");
            System.out.println("7. Checkout");
            System.out.println("8. Back to Main Menu");
            System.out.print("Select an option: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                continue;
            }

            switch (choice) {
                case 1 -> clientSide.viewProducts();
                case 2 -> clientSide.searchProducts();
                case 3 -> clientSide.viewProductDetails();
                case 4 -> clientSide.addToCart();
                case 5 -> clientSide.viewCart();
                case 6 -> clientSide.clearCart();
                case 7 -> clientSide.checkout();
                case 8 -> inCustomerMenu = false;
                default -> System.out.println("Select a valid option (1-8).");
            }
        }
    }
}
