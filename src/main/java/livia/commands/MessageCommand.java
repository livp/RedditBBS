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
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static livia.Banners.*;
import static livia.singletons.TheTerminal.flush;

public class MessageCommand extends Command {

    private static final int MAX_TITLE_LENGTH = 70;

    private final Message message;
    private final Command parent;

    private MessageCommand(Message message, Command parent) {
        this.message = message;
        this.parent = parent;
    }

    public static Command create(Message message, Command parent) {
        if (!Strings.isNullOrEmpty(message.title)) {
            String boldTitle = new AttributedStringBuilder()
                    .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE).bold())
                    .append(message.title)
                    .toAnsi();
            TheTerminal.println(boldTitle);
        }
        if (!message.images.isEmpty()) {
            String image = message.asciiImage();
            String fancyImage = new AttributedStringBuilder()
                    .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE))
                    .append(image)
                    .toAnsi();
            TheTerminal.println(fancyImage);
        }
        if (!Strings.isNullOrEmpty(message.body)) {
            TheTerminal.println(message.body);
        }
        if (!Strings.isNullOrEmpty(message.url)) {
            String url = new AttributedStringBuilder()
                    .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE))
                    .append("URL[")
                    .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE).bold())
                    .append(message.url)
                    .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE))
                    .append("]")
                    .toAnsi();
            TheTerminal.println(url);
        }
        return new MessageCommand(message, parent);
    }

    @Override
    public Command parseNextLine() {
        LineReaderBuilder builder = LineReaderBuilder.builder()
                .terminal(TheTerminal.get())
                .parser(new DefaultParser());
        LineReader reader = builder.build();
        String line = reader.readLine(prompt());
        ParsedLine parsedLine = reader.getParser().parse(line, 0);
        String command = parsedLine.words().get(0);

        boolean end = false;
        switch (command.toUpperCase()) {
            case "BACK":
                return parent;
            case "END":
                end = true;
                break;
            case "GO":
                try {
                    Process browser = new ProcessBuilder(
                            "x-terminal-emulator",
                            "-e",
                            "/usr/bin/lynx",
                            message.url).start();
                } catch (IOException e) {
                    BUM("I can't start lynx. No URL for you.");
                }
                break;
            case "THUMBNAIL":
            case "THUMB":
            case "IMAGE":
            case "IMG":
                if (!message.images.isEmpty()) {
                    String image = message.asciiImage();
                    String fancyImage = new AttributedStringBuilder()
                            .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE))
                            .append(image)
                            .toAnsi();
                    TheTerminal.println(fancyImage);
                } else {
                    String noImage = new AttributedStringBuilder()
                            .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE))
                            .append("No thumbnail available, sorry.")
                            .toAnsi();
                    TheTerminal.println(noImage);
                }
            case "COMMENTS":
                return ListComments.create(message);
            default:
                BUM("Valid commands: BACK, END, GO, THUMB[NAIL], IMG, COMMENTS");
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
        String title = message.title;
        if (title.length() > MAX_TITLE_LENGTH) {
            title = title.substring(0, MAX_TITLE_LENGTH);
        }
        builder.append(String.format("> %s > ", title));
        String prompt = builder.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE))
                .toAnsi();
        return prompt;
    }

    private void comments() {
    }

}
