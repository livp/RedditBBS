package livia;

import livia.commands.Command;
import livia.singletons.TheTerminal;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.Parser;
import org.jline.reader.impl.DefaultParser;

public class RedditBBS {

  private LineReader reader = null;
  private Parser parser = new DefaultParser();
  private Command currentCommand = null;

  public RedditBBS() {
      currentCommand = Command.root();
      reader = LineReaderBuilder.builder()
          .terminal(TheTerminal.get())
          .parser(parser)
          .build();
  }

  void run() {
    Banners.displayWelcomeBanner();

    while (currentCommand != null) {
      currentCommand = currentCommand.parseNextLine();
    }
  }

  public static void main(String[] _ignored) {
    new RedditBBS().run();
  }

}
