package shelfside;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * A small helper for reading keyboard input and printing text.
 *
 * All three menu classes share this so they do not each repeat the same
 * read-a-line / read-a-number code. It takes its input and output as parameters,
 * which also lets tests feed it fake input if needed.
 */
public class Console {

    private final Scanner in;
    private final PrintStream out;

    public Console(Scanner in, PrintStream out) {
        this.in = in;
        this.out = out;
    }

    public void print(String text) {
        out.print(text);
    }

    public void println(String text) {
        out.println(text);
    }

    public void blank() {
        out.println();
    }

    /**
     * Shows a prompt and returns the typed line, trimmed. Returns an empty
     * string if there is no more input (for example, the user closed input).
     */
    public String readLine(String prompt) {
        out.print(prompt);
        if (!in.hasNextLine()) {
            return "";
        }
        return in.nextLine().trim();
    }

    /**
     * Asks for a whole number between min and max (inclusive). Re-asks on bad
     * input. If input runs out, returns min so the program can end cleanly.
     */
    public int readIntInRange(String prompt, int min, int max) {
        while (true) {
            String line = readLine(prompt);
            if (line.isEmpty() && !in.hasNextLine()) {
                return min;
            }
            try {
                int value = Integer.parseInt(line);
                if (value < min || value > max) {
                    println("  Please enter a number from " + min + " to " + max + ".");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                println("  '" + line + "' is not a whole number. Try again.");
            }
        }
    }
}
