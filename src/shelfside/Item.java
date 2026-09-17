package shelfside;

/**
 * One product in the store.
 *
 * Money is stored as a whole number of cents (an int). We do this everywhere so
 * that we never do math on dollars-as-doubles, which can give answers like
 * $0.30000000000000004. A price of $8.99 is stored as 899.
 */
public class Item {

    private final int id;
    private String name;
    private int priceCents;
    private int quantity;
    private String description;

    /**
     * Creates an item and checks that its values make sense.
     *
     * @throws IllegalArgumentException if the name is blank, the price is
     *         negative, or the quantity is negative.
     */
    public Item(int id, String name, int priceCents, int quantity, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be blank.");
        }
        if (priceCents < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        this.id = id;
        this.name = name.trim();
        this.priceCents = priceCents;
        this.quantity = quantity;
        this.description = (description == null) ? "" : description.trim();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be blank.");
        }
        this.name = name.trim();
    }

    public int getPriceCents() {
        return priceCents;
    }

    public void setPriceCents(int priceCents) {
        if (priceCents < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
        this.priceCents = priceCents;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        this.quantity = quantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = (description == null) ? "" : description.trim();
    }

    /** Removes {@code amount} units from stock, refusing to go below zero. */
    public void reduceStock(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount to remove cannot be negative.");
        }
        if (amount > quantity) {
            throw new IllegalArgumentException(
                "Not enough stock: only " + quantity + " of '" + name + "' available.");
        }
        quantity -= amount;
    }

    /** A one-line summary used in list views. */
    public String shortLine() {
        return String.format("#%d  %-20s %8s  qty %d",
            id, name, formatCents(priceCents), quantity);
    }

    /** A multi-line view used for the "product details" screen. */
    public String details() {
        StringBuilder sb = new StringBuilder();
        sb.append("Product #").append(id).append('\n');
        sb.append("  Name:     ").append(name).append('\n');
        sb.append("  Price:    ").append(formatCents(priceCents)).append('\n');
        sb.append("  In stock: ").append(quantity).append('\n');
        sb.append("  About:    ").append(description.isEmpty() ? "(no description)" : description);
        return sb.toString();
    }

    // ----- money helpers (kept here so price handling lives in one place) -----

    /** Turns cents into a display string, e.g. 899 -> "$8.99". */
    public static String formatCents(int cents) {
        return String.format("$%d.%02d", cents / 100, Math.abs(cents % 100));
    }

    /**
     * Turns a typed dollar amount into cents, e.g. "8.99" -> 899, "5" -> 500.
     * A leading "$" is allowed. Rejects blanks, letters, negatives, and more
     * than two decimal places.
     *
     * @throws IllegalArgumentException with a clear message if the text is not a
     *         valid, non-negative price.
     */
    public static int parsePriceToCents(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Price cannot be blank.");
        }
        String s = raw.trim();
        if (s.startsWith("$")) {
            s = s.substring(1).trim();
        }
        String[] parts = s.split("\\.", -1);
        if (parts.length > 2) {
            throw new IllegalArgumentException("Price has too many decimal points: " + raw);
        }
        String whole = parts[0].isEmpty() ? "0" : parts[0];
        String frac = (parts.length == 2) ? parts[1] : "";
        if (frac.length() > 2) {
            throw new IllegalArgumentException("Price can have at most two decimal places: " + raw);
        }
        while (frac.length() < 2) {
            frac = frac + "0";
        }
        if (!whole.matches("\\d+") || !frac.matches("\\d+")) {
            throw new IllegalArgumentException("Price is not a valid number: " + raw);
        }
        long cents = Long.parseLong(whole) * 100 + Long.parseLong(frac);
        if (cents > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Price is too large.");
        }
        return (int) cents;
    }
}
