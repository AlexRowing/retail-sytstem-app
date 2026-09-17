package shelfside;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

/** Tests for the customer operations: cart and checkout. Uses a temporary file. */
public class ClientSideTest {

    private Path tempFile;
    private List<Item> inventory;
    private Storage storage;
    private ClientSide client;

    @Before
    public void setUp() throws IOException {
        tempFile = Files.createTempFile("shelfside-client", ".json");
        Files.deleteIfExists(tempFile);
        inventory = new ArrayList<Item>();
        inventory.add(new Item(1, "Coffee Mug", 899, 5, "ceramic"));
        inventory.add(new Item(2, "Notebook", 350, 10, "80 pages"));
        storage = new Storage(tempFile);
        Console console = new Console(new Scanner(""), new PrintStream(new ByteArrayOutputStream()));
        client = new ClientSide(inventory, storage, console);
    }

    @After
    public void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @Test
    public void addToCart_addsAndComputesTotal() {
        client.addToCart(1, 2); // 2 mugs @ $8.99
        client.addToCart(2, 1); // 1 notebook @ $3.50
        assertEquals(2, (int) client.getCart().get(1));
        assertEquals(899 * 2 + 350, client.cartTotalCents());
    }

    @Test
    public void addToCart_rejectsMoreThanStock() {
        try {
            client.addToCart(1, 6); // only 5 in stock
            fail("expected an exception for exceeding stock");
        } catch (IllegalArgumentException expected) {
            assertTrue(client.getCart().isEmpty());
        }
    }

    @Test
    public void addToCart_rejectsWhenCartPlusRequestExceedsStock() {
        client.addToCart(1, 3);
        try {
            client.addToCart(1, 3); // 3 + 3 > 5
            fail("expected an exception when cart + request exceeds stock");
        } catch (IllegalArgumentException expected) {
            assertEquals(3, (int) client.getCart().get(1)); // unchanged
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void addToCart_rejectsMissingProduct() {
        client.addToCart(999, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addToCart_rejectsZeroQuantity() {
        client.addToCart(1, 0);
    }

    @Test
    public void checkout_reducesStockClearsCartAndSaves() throws Exception {
        client.addToCart(1, 2);
        assertTrue(client.checkout());

        assertEquals(3, client.findById(1).getQuantity()); // 5 - 2
        assertTrue(client.getCart().isEmpty());

        // The reduced stock was saved to disk.
        List<Item> reloaded = new Storage(tempFile).load();
        assertEquals(3, reloaded.get(0).getQuantity());
    }

    @Test
    public void checkout_emptyCart_returnsFalse() throws Exception {
        assertFalse(client.checkout());
    }
}
