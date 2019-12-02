package livia.commands;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.util.ArrayMap;
import com.google.common.base.Strings;
import livia.Model.*;
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
import java.util.Map;

import static livia.Banners.BUM;
import static livia.Banners.OH_FUCK;
import static livia.singletons.TheTerminal.flush;
import static livia.singletons.TheTerminal.writer;

public class ListMessagesCommand extends Command {

    private static final String IS_NUMERIC_REGEX = "-?\\d+(\\.\\d+)?";
    private final SubOrMulti subOrMulti;
    private final PostSort postSort;
    private final Command parent;
    private Listing listing = null;
    private Map<Integer, Message> messages = new ArrayMap<>();

    public enum PostSort {
        HOT,
        NEW,
        TOP,
        DAVERYBEST
    }

    public static Command create(SubOrMulti subOrMulti, PostSort postSort, Command parent) {
        ListMessagesCommand command = new ListMessagesCommand(subOrMulti, postSort, parent);
        command.runQuery();
        return command;
    }

    private ListMessagesCommand(SubOrMulti subOrMulti, PostSort postSort, Command parent) {
        super();
        this.subOrMulti = subOrMulti;
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

        if (command.matches(IS_NUMERIC_REGEX)) {
            // A post
            int number = Integer.valueOf(command);
            if (!messages.containsKey(number)) {
                BUM("BAD MESSAGE NUMBER");
                return this;
            }
            return Command.message(messages.get(number), this);
        }

        switch (command.toUpperCase()) {
            case "BACK":
                return parent;
            case "CONTINUE":
            case "Y":
            case "YES":
                runQuery();
            case "NO":
            case "N":
                return parent;
            case "END":
                end = true;
                break;
            default:
                BUM("Valid commands: BACK, CONTINUE, YES, NO, GO, END");
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
            builder.append(String.format("%s > %s > %s > ", subOrMulti.name(), postSort.toString(), "THERE IS MORE. CONTINUE?"));
        } else {
            builder.append(String.format("%s > %s > %s > ", subOrMulti.name(), postSort.toString(), "END OF LIST"));
        }
        String prompt = builder.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE))
                .toAnsi();
        return prompt;
    }

    private void runQuery() {
        GenericUrl url = new GenericUrl(
                String.format("https://www.reddit.com/r/%s/%s.json",
                        subOrMulti.name(), postSort.toString().toLowerCase()));
        if (listing != null && !Strings.isNullOrEmpty(listing.after)) {
            url.put("after", listing.after);
        }

        try {
            HttpRequest request = Network.request(url);
            HttpResponse httpResponse = request.execute();
            GenericJson jsonResponse = httpResponse.parseAs(GenericJson.class);
            listing = Listing.fromJson(jsonResponse);

            for (Message message : listing.messages) {
                int messageNumber = messages.size() /* 0 based, no need for +1 */;
                messages.put(messageNumber, message);
                AttributedString fancyTitle = new AttributedStringBuilder()
                        .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN))
                        .append(String.format("[%d]", messageNumber))
                        .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE))
                        .append("*** ")
                        .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE))
                        .append(message.title)
                        .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE))
                        .append(" *** ")
                        .toAttributedString();
                TheTerminal.println(fancyTitle.toAnsi());
                flush();
            }
            if (Strings.isNullOrEmpty(listing.after)) {
                listing = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            OH_FUCK(String.format("[GET %s] ------>>>>> ", url.toString(), e.getMessage()));
        }
    }

}
