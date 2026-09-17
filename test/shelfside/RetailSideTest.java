package shelfside;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Tests for the retailer operations. Uses a temporary storage file. */
public class RetailSideTest {

    private Path tempFile;
    private List<Item> inventory;
    private Storage storage;
    private RetailSide retail;

    @Before
    public void setUp() throws IOException {
        tempFile = Files.createTempFile("shelfside-retail", ".json");
        Files.deleteIfExists(tempFile); // start with no file
        inventory = new ArrayList<Item>();
        storage = new Storage(tempFile);
        Console console = new Console(new Scanner(""), new PrintStream(new ByteArrayOutputStream()));
        retail = new RetailSide(inventory, storage, console);
    }

    @After
    public void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @Test
    public void addItem_addsAndSaves() throws Exception {
        Item added = retail.addItem("Mug", 899, 20, "ceramic");
        assertEquals(1, added.getId());
        assertEquals(1, inventory.size());

        // It was written to disk: a fresh load sees it.
        List<Item> reloaded = new Storage(tempFile).load();
        assertEquals(1, reloaded.size());
        assertEquals("Mug", reloaded.get(0).getName());
    }

    @Test
    public void addItem_givesIncreasingIds() throws Exception {
        Item a = retail.addItem("Mug", 899, 20, "");
        Item b = retail.addItem("Pen", 150, 100, "");
        assertEquals(1, a.getId());
        assertEquals(2, b.getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addItem_rejectsNegativePrice() throws Exception {
        retail.addItem("Mug", -5, 20, "");
    }

    @Test
    public void updateQuantity_changesStockAndSaves() throws Exception {
        retail.addItem("Mug", 899, 20, "");
        int id = inventory.get(0).getId();
        assertTrue(retail.updateQuantity(id, 7));
        assertEquals(7, retail.findById(id).getQuantity());

        List<Item> reloaded = new Storage(tempFile).load();
        assertEquals(7, reloaded.get(0).getQuantity());
    }

    @Test
    public void updateQuantity_missingProduct_returnsFalse() throws Exception {
        assertFalse(retail.updateQuantity(999, 5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateQuantity_negative_throws() throws Exception {
        retail.addItem("Mug", 899, 20, "");
        int id = inventory.get(0).getId();
        retail.updateQuantity(id, -3);
    }

    @Test
    public void removeItem_removesExisting_andReportsMissing() throws Exception {
        retail.addItem("Mug", 899, 20, "");
        int id = inventory.get(0).getId();
        assertTrue(retail.removeItem(id));
        assertNull(retail.findById(id));
        assertFalse(retail.removeItem(id)); // already gone
    }

    @Test
    public void search_findsByNameAndReturnsEmptyWhenNoMatch() throws Exception {
        retail.addItem("Coffee Mug", 899, 20, "");
        retail.addItem("Notebook", 350, 5, "");
        assertEquals(1, retail.search("mug").size());   // case-insensitive
        assertTrue(retail.search("banana").isEmpty());  // bad/no match
    }
}
