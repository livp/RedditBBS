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
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class RedditBBS {

  static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
  static JsonFactory JSON_FACTORY = new JacksonFactory();

  private Terminal terminal = null;

  public RedditBBS() {
    try {
      terminal = TerminalBuilder.builder().system(true).build();

    } catch(IOException e) {
      System.err.println("Unable to open the terminal. Goodbye.");
      System.exit(-1);
    }
  }

  /**
   * Uninstall settings, kill threads.
   */
  void goodbye() {
    AnsiConsole.systemUninstall();
  }

  void displayWelcomeBanner() {

    terminal.writer().println(Banners.BIG_ALIEN);
    // AnsiConsole.out().print(
    //     ansi().eraseScreen().cursor(0,0));
    // AnsiConsole.out().println(ansi().fgBright(RED).a(Banners.BIG_ALIEN));
    // AnsiConsole.out().println(ansi().fgBright(WHITE).bold().a(Banners.WELCOME));
  }

  public static void main(String[] _ignored) {
    RedditBBS bbs = new RedditBBS();
    bbs.displayWelcomeBanner();
    bbs.goodbye();
  }

}
