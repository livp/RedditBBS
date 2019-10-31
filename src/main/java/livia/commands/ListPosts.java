package livia.commands;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.common.base.Strings;
import livia.Model;
import livia.Model.Listing;
import livia.Model.Subreddit;
import livia.singletons.Network;
import livia.singletons.TheTerminal;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.DefaultParser;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.io.IOException;

import static livia.Banners.BUM;
import static livia.Banners.OH_FUCK;
import static livia.singletons.TheTerminal.flush;
import static livia.singletons.TheTerminal.writer;

public class ListPosts extends Command {

    private final Subreddit subreddit;
    private final PostSort postSort;
    private final Command parent;
    private Listing listing = null;

    public enum PostSort {
        HOT,
        NEW,
        TOP,
        DAVERYBEST
    }

    public static Command create(Subreddit subreddit, PostSort postSort, Command parent) {
        ListPosts command = new ListPosts(subreddit, postSort, parent);
        command.runQuery();
        return command;
    }

    private ListPosts(Subreddit subreddit, PostSort postSort, Command parent) {
        super();
        this.subreddit = subreddit;
        this.postSort = postSort;
        this.parent = parent;
    }

    @Override
    public Command parseNextLine() {
        LineReaderBuilder builder = LineReaderBuilder.builder()
                .terminal(TheTerminal.get())
                .parser(new DefaultParser());
//        if (listing != null) {
//            builder.completer(new StringsCompleter(subreddits.keySet()));
//        }
        LineReader reader = builder.build();
        String line = reader.readLine(prompt());
        ParsedLine parsedLine = reader.getParser().parse(line, 0);
        String command = parsedLine.words().get(0);

        boolean end = false;

//        if (subreddits.containsKey(command)) {
//            return Command.subreddit(subreddits.get(command));
//        }

        switch (command.toUpperCase()) {
            case "BACK":
                return parent;
            case "CONTINUE":
            case "Y":
            case "YES":
                runQuery();
            case "NO":
            case "N":
                return this;
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
            builder.append(String.format("%s > %s > %s > ", subreddit.displayName, postSort.toString(), "THERE IS MORE. CONTINUE?"));
        } else {
            builder.append(String.format("%s > %s > %s > ", subreddit.displayName, postSort.toString(), "END OF LIST"));
        }
        String prompt = builder.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE))
                .toAnsi();
        return prompt;
    }

    private void runQuery() {
        GenericUrl url = new GenericUrl(
                String.format("https://www.reddit.com/r/%s/%s.json",
                        subreddit.displayName, postSort.toString().toLowerCase()));
        if (listing != null && !Strings.isNullOrEmpty(listing.after)) {
            url.put("after", listing.after);
        }

        try {
            HttpRequest request = Network.request(url);
            HttpResponse httpResponse = request.execute();
            GenericJson jsonResponse = httpResponse.parseAs(GenericJson.class);
            listing = Model.Listing.fromJson(jsonResponse);

            for (Model.Message message : listing.messages) {
                AttributedString fancyTitle = new AttributedStringBuilder()
                        .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE))
                        .append("*** ")
                        .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE))
                        .append(message.title)
                        .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE))
                        .append(" *** ")
                        .toAttributedString();
                writer().println(fancyTitle.toAnsi());
                flush();
//            }
//
//            listing.subreddits.forEach(
//                    subreddit -> subreddits.put(subreddit.displayName, subreddit)
//            );
//
                if (Strings.isNullOrEmpty(listing.after)) {
                    listing = null;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            OH_FUCK(String.format("[GET %s] ------>>>>> ", url.toString(), e.getMessage()));
        }
    }

}
