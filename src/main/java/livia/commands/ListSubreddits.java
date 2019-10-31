package livia.commands;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.common.base.Strings;
import livia.Model.Listing;
import livia.Model.Subreddit;
import livia.singletons.Network;
import livia.singletons.TheTerminal;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;
import static livia.Banners.BUM;
import static livia.Banners.OH_FUCK;
import static livia.singletons.TheTerminal.flush;
import static livia.singletons.TheTerminal.writer;

public class ListSubreddits extends Command {

    private String filter;
    private Listing listing;
    private Map<String, Subreddit> subreddits = new HashMap<>();

    public static Command create(String filter) {
        ListSubreddits command = new ListSubreddits();
        command.filter = filter;
        command.runQuery();
        if (command.listing == null) {
            return Command.root();
        }
        return command;
    }

    @Override
    public Command parseNextLine() {
        LineReaderBuilder builder = LineReaderBuilder.builder()
                .terminal(TheTerminal.get())
                .parser(new DefaultParser());
        if (listing != null) {
            builder.completer(new StringsCompleter(subreddits.keySet()));
        }
        LineReader reader = builder.build();
        String line = reader.readLine(prompt());
        ParsedLine parsedLine = reader.getParser().parse(line, 0);
        String command = parsedLine.words().get(0);

        boolean end = false;

        if (subreddits.containsKey(command)) {
            return Command.subreddit(subreddits.get(command));
        }

        switch (command.toUpperCase()) {
            case "BACK":
                return Command.root();
            case "CONTINUE":
            case "Y":
            case "YES":
                runQuery();
                if (listing == null) {
                    return Command.root();
                }
            case "NO":
            case "N":
                return Command.root();
            case "END":
                end = true;
                break;
            default:
                BUM("Valid commands: BACK, CONTINUE, GO, END");
        }
        flush();

        if (end) {
            return null;
        }

        return this;
    }

    private String prompt() {
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

    private void runQuery() {
        GenericUrl url = new GenericUrl("https://www.reddit.com/search.json");
        url.put("q", filter);
        url.put("type", "sr");
        if (listing != null && !Strings.isNullOrEmpty(listing.after)) {
            url.put("after", listing.after);
        }

        try {
            HttpRequest request = Network.request(url);
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
                        .append(subreddit.shortDescription(TheTerminal.width() - 25))
                        .toAttributedString();
                writer().println(fancyTitle.toAnsi());
                flush();
            }

            listing.subreddits.forEach(
                    subreddit -> subreddits.put(subreddit.displayName, subreddit)
            );

            if (Strings.isNullOrEmpty(listing.after)) {
                listing = null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            OH_FUCK(String.format("[GET %s] ------>>>>> ", url.toString(), e.getMessage()));
        }
    }
}
