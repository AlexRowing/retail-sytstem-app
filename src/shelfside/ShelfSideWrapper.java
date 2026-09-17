package shelfside;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * The starting point of Shelf Side.
 *
 * It loads the inventory once, then shows the main menu: go to the retailer
 * side (after a demo login), go to the customer side, or exit. The retailer and
 * customer sides share the same in-memory list, so changes one makes are visible
 * to the other, and both save through the same {@link Storage}.
 */
public class ShelfSideWrapper {

    /** Default file used when none is given on the command line. */
    public static final String DEFAULT_FILE = "inventory.json";

    private final Storage storage;
    private final Console console;
    private final LoginProcessor login = new LoginProcessor();
    private List<Item> inventory;

    public ShelfSideWrapper(Storage storage, Console console) {
        this.storage = storage;
        this.console = console;
    }

    public static void main(String[] args) {
        String path = (args.length > 0) ? args[0] : DEFAULT_FILE;
        Storage storage = new Storage(path);
        Console console = new Console(new Scanner(System.in), System.out);
        new ShelfSideWrapper(storage, console).start();
    }

    /** Loads inventory (starting empty on any problem) and runs the main menu. */
    public void start() {
        console.println("==============================");
        console.println("        SHELF SIDE");
        console.println("  CS 2114 class demo project");
        console.println("==============================");
        console.println("Inventory file: " + storage.getFile());

        try {
            inventory = storage.load();
            console.println("Loaded " + inventory.size() + " product(s).");
        } catch (StorageException e) {
            // Clear message instead of a crash; start empty so the demo still runs.
            console.println("Warning: " + e.getMessage());
            console.println("Starting with an empty inventory.");
            inventory = new ArrayList<Item>();
        }

        mainMenu();
        console.println("Goodbye!");
    }

    private void mainMenu() {
        while (true) {
            console.blank();
            console.println("--- Main Menu ---");
            console.println("1) Retailer (store manager)");
            console.println("2) Customer (shopper)");
            console.println("0) Exit");
            int choice = console.readIntInRange("Choose an option: ", 0, 2);
            switch (choice) {
                case 1:
                    if (retailerLogin()) {
                        new RetailSide(inventory, storage, console).run();
                    }
                    break;
                case 2:
                    new ClientSide(inventory, storage, console).run();
                    break;
                case 0:
                    return;
                default:
                    break;
            }
        }
    }

    /**
     * Asks for the demo login, allowing a few tries. Returns true on success.
     * The credentials are printed here on purpose because this is a demo.
     */
    private boolean retailerLogin() {
        console.blank();
        console.println("Retailer login (DEMO credentials — not real security)");
        console.println("  username: " + LoginProcessor.DEMO_USERNAME);
        console.println("  password: " + LoginProcessor.DEMO_PASSWORD);
        for (int attempt = 1; attempt <= LoginProcessor.MAX_ATTEMPTS; attempt++) {
            String user = console.readLine("Username: ");
            String pass = console.readLine("Password: ");
            if (login.check(user, pass)) {
                console.println("Login OK.");
                return true;
            }
            console.println("Wrong username or password. "
                + (LoginProcessor.MAX_ATTEMPTS - attempt) + " try(s) left.");
        }
        console.println("Too many failed attempts. Returning to main menu.");
        return false;
    }
}
