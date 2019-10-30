package livia;

import static livia.Banners.errorMessage;

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
import com.google.common.base.Strings;
import java.io.IOException;
import livia.Model.Listing;
import livia.Model.Subreddit;
import org.jline.reader.ParsedLine;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

/**
 * State machine.
 */
public abstract class Command {

  private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
  private static final JsonFactory JSON_FACTORY = new JacksonFactory();
  private static final HttpRequestFactory requestFactory
      = HTTP_TRANSPORT.createRequestFactory(
      (HttpRequest request) -> {
        request.setParser(new JsonObjectParser(JSON_FACTORY));
      });

  abstract String prompt();
  abstract Command parse(ParsedLine parsedLine);

  protected Terminal terminal;

  public static Root root(Terminal terminal) {
    return Root.create(terminal);
  }

  public static Command listSubreddits(Terminal terminal, String filter) {
    Command command = ListSubreddits.create(terminal, filter);
    return command;
  }

  private Command(Terminal terminal) {
    this.terminal = terminal;
  }


  public static class Root extends Command {

    public static Root create(Terminal terminal) {
      return new Root(terminal);
    }

    private Root(Terminal terminal) {
      super(terminal);
    }

    @Override
    String prompt() {
      String prompt = new AttributedStringBuilder()
          .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE))
          .append("> ")
          .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE))
          .toAnsi();
      return prompt;
    }

    @Override
    Command parse(ParsedLine parsedLine) {
      String command = parsedLine.words().get(0);

      boolean end = false;
      switch (command.toUpperCase()) {
        case "END":
          end = true;
          break;
        case "LIST":
          if (parsedLine.words().size() != 2) {
            errorMessage(terminal, "list <subreddit>");
            break;
          }
          String filter = parsedLine.words().get(1);
          return Command.listSubreddits(terminal, filter);
        default:
          Banners.errorMessage(terminal, "Valid commands: LIST, END");
      }
      terminal.flush();

      if (end) {
        return null;
      }

      return this;
    }
  }

  public static class ListSubreddits extends Command {

    private String filter;
    private Listing listing;
    private Terminal terminal;

    public static Command create(Terminal terminal, String filter) {
      ListSubreddits command = new ListSubreddits(terminal);
      command.terminal = terminal;
      command.filter = filter;
      command.runQuery();
      if (command.listing == null) {
        return Command.root(terminal);
      }
      return command;
    }

    @Override
    String prompt() {
      AttributedStringBuilder builder = new AttributedStringBuilder()
          .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE));
      if (listing != null && !Strings.isNullOrEmpty(listing.after)) {
        builder.append("there's more. continue? > ");
      } else {
        builder.append("no more subs > ");
      }
      String prompt = builder.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE))
          .toAnsi();
      return prompt;
    }

    @Override
    Command parse(ParsedLine parsedLine) {
      String command = parsedLine.words().get(0);

      boolean end = false;
      switch (command.toUpperCase()) {
        case "BACK":
          return new Command.Root(terminal);
        case "CONTINUE":
        case "Y":
        case "YES":
          runQuery();
          if (listing == null) {
            return Command.root(terminal);
          }
        case "NO":
        case "N":
          return Command.root(terminal);
        case "END":
          end = true;
          break;
        default:
          Banners.errorMessage(terminal, "Valid commands: BACK, CONTINUE, GO, END");
      }
      terminal.flush();

      if (end) {
        return null;
      }

      return this;
    }

    private ListSubreddits(Terminal terminal) {
      super(terminal);
    }

    private void runQuery() {
      try {
        GenericUrl url = new GenericUrl("https://www.reddit.com/search.json");
        url.put("q" , filter);
        url.put("type" , "sr");
        if (listing != null && !Strings.isNullOrEmpty(listing.after)) {
          url.put("after", listing.after);
        }

        HttpRequest request = requestFactory.buildGetRequest(url);
        HttpResponse httpResponse = request.execute();
        GenericJson jsonResponse = httpResponse.parseAs(GenericJson.class);
        listing = Listing.fromJson(jsonResponse);

        for (Subreddit subreddit : listing.subreddits) {
          AttributedString fancyTitle = new AttributedStringBuilder()
              .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE))
              .append(">>> ")
              .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE))
              .append(subreddit.displayName)
              .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE))
              .append(" <<< ")
              .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN))
              .append(subreddit.shortDescription(terminal.getWidth() - 25))
              .toAttributedString();
          terminal.writer().println(fancyTitle.toAnsi());
          terminal.flush();
        }

        if (Strings.isNullOrEmpty(listing.after)) {
          listing = null;
        }

      } catch (IOException e) {
        Banners.errorMessage(terminal,"Network error");
      }
    }
  }
}
