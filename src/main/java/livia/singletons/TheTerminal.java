package livia.singletons;

import livia.Banners;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

import java.io.IOException;
import java.io.PrintWriter;

import static livia.Banners.ErrorType.OH_FUCK;

/**
 * because i'm lazy
 * obviously not reentrant
 */
public abstract class TheTerminal {

    private static Terminal theOnlyOne = null;

    public static void println(String s) {
        get().writer().println(s);
        get().flush();
        return;
    }

    public static Terminal get() {
        try {
            if (theOnlyOne == null) {
                theOnlyOne = TerminalBuilder.builder().system(true).build();
                theOnlyOne.puts(InfoCmp.Capability.clear_screen);
                theOnlyOne.flush();
            }
        } catch (IOException e) {
            Banners.errorMessage(theOnlyOne, OH_FUCK, e.getMessage());
        }
        return theOnlyOne;
    }

    public static PrintWriter writer() {
        return get().writer();
    }

    public static void flush() {
        get().flush();
    }

    public static int width() {
        int width = get().getWidth();
        if (width < 0) {
            return 0;
        }
        return width;
    }

    public static int height() {
        int width = get().getHeight();
        if (width < 0) {
            return 0;
        }
        return width;
    }
}
