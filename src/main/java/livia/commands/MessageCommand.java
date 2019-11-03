package livia.commands;

import com.google.common.base.Strings;
import livia.Model.*;
import livia.singletons.TheTerminal;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import static livia.Banners.BUM;
import static livia.singletons.TheTerminal.flush;

public class MessageCommand extends Command {

    private final Message message;
    private final Command parent;
    private boolean firstTime = true;

    private MessageCommand(Message message, Command parent) {
        this.message = message;
        this.parent = parent;
    }

    public static Command create(Message message, Command parent) {
        if (!message.urls.isEmpty()) {
            String image = message.asciiImage();
            String fancyImage = new AttributedStringBuilder()
                    .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE).bold())
                    .append(image)
                    .toAnsi();
            TheTerminal.println(fancyImage);
        }
        // TODO: Content
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
            default:
                BUM("Valid commands: BACK, END");
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

        // TODO the parent
        String title = message.title;
        if (title.length() > 10) {
            title = title.substring(0, 10);
        }
        if (!message.images.isEmpty()) {
            builder.append(message.images.get(0));
        }
        builder.append(String.format("> %s > ", title));
        String prompt = builder.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE))
                .toAnsi();
        return prompt;
    }

}
