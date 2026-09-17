package shelfside;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for loading and saving. Every test uses a temporary file, so the real
 * demo inventory is never touched.
 */
public class StorageTest {

    private Path tempFile;

    @Before
    public void makeTempFile() throws IOException {
        tempFile = Files.createTempFile("shelfside-test", ".json");
    }

    @After
    public void deleteTempFile() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @Test
    public void load_missingFile_givesEmptyList() throws Exception {
        Files.deleteIfExists(tempFile); // make sure it does not exist
        Storage storage = new Storage(tempFile);
        List<Item> items = storage.load();
        assertTrue(items.isEmpty());
    }

    @Test
    public void saveThenLoad_returnsSameData() throws Exception {
        List<Item> items = new ArrayList<Item>();
        items.add(new Item(1, "Coffee Mug", 899, 20, "12oz ceramic"));
        items.add(new Item(2, "Notebook", 350, 5, "80 pages"));

        Storage storage = new Storage(tempFile);
        storage.save(items);
        List<Item> loaded = storage.load();

        assertEquals(2, loaded.size());
        assertEquals("Coffee Mug", loaded.get(0).getName());
        assertEquals(899, loaded.get(0).getPriceCents());
        assertEquals(20, loaded.get(0).getQuantity());
        assertEquals("Notebook", loaded.get(1).getName());
        assertEquals(5, loaded.get(1).getQuantity());
    }

    @Test
    public void save_handlesNamesWithQuotesAndCommas() throws Exception {
        List<Item> items = new ArrayList<Item>();
        items.add(new Item(1, "12\" Ruler, metal", 199, 10, "line 1\nline 2"));

        Storage storage = new Storage(tempFile);
        storage.save(items);
        List<Item> loaded = storage.load();

        assertEquals("12\" Ruler, metal", loaded.get(0).getName());
        assertEquals("line 1\nline 2", loaded.get(0).getDescription());
    }

    @Test
    public void load_malformedJson_throwsClearError() throws Exception {
        Files.write(tempFile, "this is not json".getBytes(StandardCharsets.UTF_8));
        Storage storage = new Storage(tempFile);
        try {
            storage.load();
            fail("expected a StorageException for malformed data");
        } catch (StorageException expected) {
            assertTrue(expected.getMessage().contains("not valid"));
        }
    }

    @Test
    public void load_missingRequiredField_throwsClearError() throws Exception {
        // "name" is missing from this product.
        Files.write(tempFile,
            "[{\"id\":1,\"priceCents\":100,\"quantity\":2}]".getBytes(StandardCharsets.UTF_8));
        Storage storage = new Storage(tempFile);
        try {
            storage.load();
            fail("expected a StorageException for a missing field");
        } catch (StorageException expected) {
            assertTrue(expected.getMessage().contains("name"));
        }
    }
}
