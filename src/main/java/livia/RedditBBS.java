package livia;

import java.io.IOException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.Parser;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp.Capability;

public class RedditBBS {

  private Terminal terminal = null;
  private LineReader reader = null;
  private Parser parser = new DefaultParser();
  private Command currentCommand = null;

  public RedditBBS() {
    try {
      terminal = TerminalBuilder.builder().system(true).build();
      terminal.puts(Capability.clear_screen);
      terminal.flush();

      currentCommand = Command.root(terminal);

      reader = LineReaderBuilder.builder()
          .terminal(terminal)
          .parser(parser)
          .build();

    } catch (IOException e) {
      System.err.println("Unable to open the terminal. Goodbye.");
      System.exit(-1);
    }
  }

  void run() {
    Banners.displayWelcomeBanner(terminal);

    while (currentCommand != null) {
      String line = reader.readLine(currentCommand.prompt());
      ParsedLine parsedLine = reader.getParser().parse(line, 0);
      currentCommand = currentCommand.parse(parsedLine);
    }
  }

  public static void main(String[] _ignored) {
    new RedditBBS().run();
  }

}
