package shelfside;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reads and writes the inventory as a JSON file.
 *
 * The file is a JSON array of products, for example:
 * <pre>
 * [
 *   {"id":1,"name":"Coffee Mug","priceCents":899,"quantity":20,"description":"12oz ceramic"}
 * ]
 * </pre>
 *
 * We write and read this format by hand (no outside JSON library) so the whole
 * program is self-contained and easy to explain. If the file is missing we start
 * with an empty inventory; if the file is present but broken we throw a
 * {@link StorageException} with a clear message instead of crashing.
 */
public class Storage {

    private final Path file;

    public Storage(String path) {
        this(Paths.get(path));
    }

    public Storage(Path file) {
        this.file = file;
    }

    /** The file this Storage reads from and writes to. */
    public Path getFile() {
        return file;
    }

    /**
     * Loads the inventory. Returns an empty list if the file does not exist.
     *
     * @throws StorageException if the file exists but cannot be read or is not
     *         valid inventory data.
     */
    public List<Item> load() throws StorageException {
        if (!Files.exists(file)) {
            return new ArrayList<Item>();
        }
        String text;
        try {
            text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new StorageException("Could not read inventory file '" + file + "': " + e.getMessage());
        }
        try {
            Object root = new JsonReader(text).parseWholeDocument();
            return toItems(root);
        } catch (RuntimeException e) {
            throw new StorageException("Inventory file '" + file + "' is not valid: " + e.getMessage());
        }
    }

    /**
     * Saves the inventory, creating the folder if needed.
     *
     * @throws StorageException if the file cannot be written.
     */
    public void save(List<Item> items) throws StorageException {
        String json = toJson(items);
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            Files.write(file, json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new StorageException("Could not save inventory to '" + file + "': " + e.getMessage());
        }
    }

    // --------------------------------------------------------------------
    // Turning parsed JSON into Item objects
    // --------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static List<Item> toItems(Object root) {
        if (!(root instanceof List)) {
            throw new IllegalArgumentException("the top level should be a list of products");
        }
        List<Item> items = new ArrayList<Item>();
        List<Object> rows = (List<Object>) root;
        for (int i = 0; i < rows.size(); i++) {
            if (!(rows.get(i) instanceof Map)) {
                throw new IllegalArgumentException("product #" + (i + 1) + " is not an object");
            }
            Map<String, Object> row = (Map<String, Object>) rows.get(i);
            int id = asInt(row, "id");
            String name = asString(row, "name");
            int priceCents = asInt(row, "priceCents");
            int quantity = asInt(row, "quantity");
            String description = row.containsKey("description") ? asString(row, "description") : "";
            // The Item constructor does the final range checks (blank name, negatives).
            items.add(new Item(id, name, priceCents, quantity, description));
        }
        return items;
    }

    private static int asInt(Map<String, Object> row, String key) {
        Object v = row.get(key);
        if (!(v instanceof Long)) {
            throw new IllegalArgumentException("field '" + key + "' is missing or not a whole number");
        }
        return (int) (long) (Long) v;
    }

    private static String asString(Map<String, Object> row, String key) {
        Object v = row.get(key);
        if (!(v instanceof String)) {
            throw new IllegalArgumentException("field '" + key + "' is missing or not text");
        }
        return (String) v;
    }

    // --------------------------------------------------------------------
    // Writing Item objects out as JSON text
    // --------------------------------------------------------------------

    private static String toJson(List<Item> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < items.size(); i++) {
            Item it = items.get(i);
            sb.append("  {");
            sb.append("\"id\":").append(it.getId()).append(',');
            sb.append("\"name\":").append(quote(it.getName())).append(',');
            sb.append("\"priceCents\":").append(it.getPriceCents()).append(',');
            sb.append("\"quantity\":").append(it.getQuantity()).append(',');
            sb.append("\"description\":").append(quote(it.getDescription()));
            sb.append('}');
            if (i < items.size() - 1) {
                sb.append(',');
            }
            sb.append('\n');
        }
        sb.append("]\n");
        return sb.toString();
    }

    /** Wraps text in quotes and escapes the characters JSON requires. */
    private static String quote(String s) {
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:   sb.append(c);
            }
        }
        sb.append('"');
        return sb.toString();
    }

    // --------------------------------------------------------------------
    // A tiny JSON reader: just enough to read the format we write above.
    // It walks the text one character at a time. Anything unexpected throws
    // an IllegalArgumentException, which load() turns into a StorageException.
    // --------------------------------------------------------------------

    private static final class JsonReader {
        private final String text;
        private int pos;

        JsonReader(String text) {
            this.text = text;
        }

        Object parseWholeDocument() {
            skipSpaces();
            Object value = readValue();
            skipSpaces();
            if (pos < text.length()) {
                throw new IllegalArgumentException("extra text after position " + pos);
            }
            return value;
        }

        private Object readValue() {
            skipSpaces();
            if (pos >= text.length()) {
                throw new IllegalArgumentException("unexpected end of file");
            }
            char c = text.charAt(pos);
            if (c == '{') {
                return readObject();
            }
            if (c == '[') {
                return readArray();
            }
            if (c == '"') {
                return readString();
            }
            if (c == '-' || (c >= '0' && c <= '9')) {
                return readNumber();
            }
            if (text.startsWith("true", pos)) {
                pos += 4;
                return Boolean.TRUE;
            }
            if (text.startsWith("false", pos)) {
                pos += 5;
                return Boolean.FALSE;
            }
            if (text.startsWith("null", pos)) {
                pos += 4;
                return null;
            }
            throw new IllegalArgumentException("unexpected character '" + c + "' at position " + pos);
        }

        private Map<String, Object> readObject() {
            java.util.LinkedHashMap<String, Object> map = new java.util.LinkedHashMap<String, Object>();
            expect('{');
            skipSpaces();
            if (peek() == '}') {
                pos++;
                return map;
            }
            while (true) {
                skipSpaces();
                String key = readString();
                skipSpaces();
                expect(':');
                Object value = readValue();
                map.put(key, value);
                skipSpaces();
                char c = next();
                if (c == '}') {
                    return map;
                }
                if (c != ',') {
                    throw new IllegalArgumentException("expected ',' or '}' at position " + (pos - 1));
                }
            }
        }

        private List<Object> readArray() {
            List<Object> list = new ArrayList<Object>();
            expect('[');
            skipSpaces();
            if (peek() == ']') {
                pos++;
                return list;
            }
            while (true) {
                list.add(readValue());
                skipSpaces();
                char c = next();
                if (c == ']') {
                    return list;
                }
                if (c != ',') {
                    throw new IllegalArgumentException("expected ',' or ']' at position " + (pos - 1));
                }
            }
        }

        private String readString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (true) {
                if (pos >= text.length()) {
                    throw new IllegalArgumentException("text is missing a closing quote");
                }
                char c = text.charAt(pos++);
                if (c == '"') {
                    return sb.toString();
                }
                if (c == '\\') {
                    char e = next();
                    switch (e) {
                        case '"':  sb.append('"');  break;
                        case '\\': sb.append('\\'); break;
                        case '/':  sb.append('/');  break;
                        case 'n':  sb.append('\n'); break;
                        case 'r':  sb.append('\r'); break;
                        case 't':  sb.append('\t'); break;
                        default:
                            throw new IllegalArgumentException("bad escape '\\" + e + "' at position " + (pos - 1));
                    }
                } else {
                    sb.append(c);
                }
            }
        }

        /** Reads a whole number. We do not need decimals, so we reject them. */
        private Long readNumber() {
            int start = pos;
            if (peek() == '-') {
                pos++;
            }
            while (pos < text.length() && text.charAt(pos) >= '0' && text.charAt(pos) <= '9') {
                pos++;
            }
            if (pos < text.length() && (text.charAt(pos) == '.' || text.charAt(pos) == 'e' || text.charAt(pos) == 'E')) {
                throw new IllegalArgumentException("numbers must be whole (no decimals) at position " + start);
            }
            String digits = text.substring(start, pos);
            if (digits.isEmpty() || digits.equals("-")) {
                throw new IllegalArgumentException("expected a number at position " + start);
            }
            try {
                return Long.valueOf(Long.parseLong(digits));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("number is too big at position " + start);
            }
        }

        private void skipSpaces() {
            while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
                pos++;
            }
        }

        private char peek() {
            if (pos >= text.length()) {
                throw new IllegalArgumentException("unexpected end of file");
            }
            return text.charAt(pos);
        }

        private char next() {
            char c = peek();
            pos++;
            return c;
        }

        private void expect(char c) {
            char got = next();
            if (got != c) {
                throw new IllegalArgumentException("expected '" + c + "' at position " + (pos - 1));
            }
        }
    }
}
