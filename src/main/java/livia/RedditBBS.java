package livia;


import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ArrayMap;
import com.google.api.client.util.Key;
import com.google.common.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
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
  private static final String BAD_COMMAND = new AttributedStringBuilder()
      .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
      .append("Unrecognized command. Valid commands: list, end")
      .toAnsi();
  private static final String NETWORK_ERROR = new AttributedStringBuilder()
      .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
      .append("Network error. Try again?")
      .toAnsi();

  private final HttpRequestFactory requestFactory
      = HTTP_TRANSPORT.createRequestFactory(
      (HttpRequest request) -> {
        request.setParser(new JsonObjectParser(JSON_FACTORY));
      });

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
      parseCommand(reader.readLine(prompt));
    }
  }

  void parseCommand(String line) {
    ParsedLine parsedLine = reader.getParser().parse(line, 0);
    String command = parsedLine.words().get(0);

    boolean end = false;
    switch (command.toUpperCase()) {
      case "END":
        end = true;
        break;
      case "LIST":
        listSubreddits(parsedLine.words().get(1));
        break;
      default:
        terminal.writer().println(BAD_COMMAND);
    }
    terminal.flush();

    if (end) {
      System.exit(0);
    }
  }

  void listSubreddits(String filter) {
    try {
      GenericUrl url = new GenericUrl(String.format("https://www.reddit.com/search.json?q=%s&type=sr", filter));
      HttpRequest request = requestFactory.buildGetRequest(url);
      HttpResponse httpResponse = request.execute();
      RedditResponse response = httpResponse.parseAs(RedditResponse.class);
      ArrayList<ArrayMap> children = (ArrayList<ArrayMap>) response.data.get("children");

      children.forEach(child -> {
        ArrayMap data = (ArrayMap)child.get("data");
        if (data.containsKey("display_name_prefixed")) {
        String subreddit = (String)data.get("display_name_prefixed");
          terminal.writer().println(String.format("%s", subreddit));
        }
      });
    } catch (IOException e) {
      terminal.writer().println(NETWORK_ERROR);
      terminal.flush();
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
