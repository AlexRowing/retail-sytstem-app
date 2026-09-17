package shelfside;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

/** Tests for the Item data class and its money helpers. */
public class ItemTest {

    // ---- parsePriceToCents: normal cases ----

    @Test
    public void parsePrice_readsDollarsAndCents() {
        assertEquals(899, Item.parsePriceToCents("8.99"));
        assertEquals(500, Item.parsePriceToCents("5"));
        assertEquals(350, Item.parsePriceToCents("$3.5"));
        assertEquals(0, Item.parsePriceToCents("0"));
    }

    // ---- parsePriceToCents: bad input ----

    @Test(expected = IllegalArgumentException.class)
    public void parsePrice_rejectsLetters() {
        Item.parsePriceToCents("abc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parsePrice_rejectsNegative() {
        Item.parsePriceToCents("-3.00");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parsePrice_rejectsTooManyDecimals() {
        Item.parsePriceToCents("1.999");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parsePrice_rejectsBlank() {
        Item.parsePriceToCents("   ");
    }

    // ---- formatCents ----

    @Test
    public void formatCents_showsTwoDecimals() {
        assertEquals("$8.99", Item.formatCents(899));
        assertEquals("$5.00", Item.formatCents(500));
        assertEquals("$0.07", Item.formatCents(7));
    }

    // ---- constructor validation ----

    @Test
    public void constructor_acceptsValidItem() {
        Item item = new Item(1, "Mug", 899, 20, "ceramic");
        assertEquals("Mug", item.getName());
        assertEquals(899, item.getPriceCents());
        assertEquals(20, item.getQuantity());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_rejectsBlankName() {
        new Item(1, "   ", 899, 20, "x");
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_rejectsNegativePrice() {
        new Item(1, "Mug", -1, 20, "x");
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_rejectsNegativeQuantity() {
        new Item(1, "Mug", 899, -5, "x");
    }

    // ---- reduceStock ----

    @Test
    public void reduceStock_lowersQuantity() {
        Item item = new Item(1, "Mug", 899, 20, "x");
        item.reduceStock(5);
        assertEquals(15, item.getQuantity());
    }

    @Test
    public void reduceStock_rejectsMoreThanInStock() {
        Item item = new Item(1, "Mug", 899, 3, "x");
        try {
            item.reduceStock(4);
            fail("expected an exception when removing more than in stock");
        } catch (IllegalArgumentException expected) {
            assertEquals(3, item.getQuantity()); // stock unchanged
        }
    }
}
