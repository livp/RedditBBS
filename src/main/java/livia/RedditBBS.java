package livia;


import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Key;
import com.google.common.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import org.fusesource.jansi.AnsiConsole;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.Parser;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp.Capability;

public class RedditBBS {

  private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
  private static final JsonFactory JSON_FACTORY = new JacksonFactory();

  private Terminal terminal = null;
  private LineReader reader = null;
  private Parser parser = new DefaultParser();
  private String prompt = new AttributedStringBuilder()
      .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE))
      .append("> ")
      .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE))
      .toAnsi();

  public RedditBBS() {
    try {
      terminal = TerminalBuilder.builder().system(true).build();
      terminal.puts(Capability.clear_screen);
      terminal.flush();

      reader = LineReaderBuilder.builder()
          .terminal(terminal)
          .parser(parser)
          .build();

    } catch(IOException e) {
      System.err.println("Unable to open the terminal. Goodbye.");
      System.exit(-1);
    }
  }

  void run() {
    displayWelcomeBanner();

    while(true) {
      String line = reader.readLine(prompt);
      ParsedLine parsedLine = reader.getParser().parse(line, 0);

      terminal.writer().println("You said:\n");
      boolean end = false;
      for (String word : parsedLine.words()) {
        terminal.writer().println(word);
        if (word.toUpperCase().equals("END")) {
          end = true;
        }
      }
      terminal.flush();
      if (end) {
        System.exit(0);
      }
    }
  }

  private void displayWelcomeBanner() {

    Banners.displayWelcomeBanner(terminal);
    // AnsiConsole.out().print(
    //     ansi().eraseScreen().cursor(0,0));
    // AnsiConsole.out().println(ansi().fgBright(RED).a(Banners.BIG_ALIEN));
    // AnsiConsole.out().println(ansi().fgBright(WHITE).bold().a(Banners.WELCOME));
  }

  public static void main(String[] _ignored) {
    new RedditBBS().run();
  }

}
