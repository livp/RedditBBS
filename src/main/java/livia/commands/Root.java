package livia.commands;

import livia.singletons.TheTerminal;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.DefaultParser;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import static livia.Banners.BUM;
import static livia.singletons.TheTerminal.flush;

public class Root extends Command {

    public static Root create() {
        return new Root();
    }

    @Override
    public Command parseNextLine() {
        LineReader reader = LineReaderBuilder.builder()
                .terminal(TheTerminal.get())
                .parser(new DefaultParser())
                .build();
        String line = reader.readLine(prompt());
        ParsedLine parsedLine = reader.getParser().parse(line, 0);
        String command = parsedLine.words().get(0);

        boolean end = false;
        switch (command.toUpperCase()) {
            case "END":
                end = true;
                break;
            case "LIST":
                if (parsedLine.words().size() != 2) {
                    BUM("list <filter>");
                    break;
                }
                String filter = parsedLine.words().get(1);
                return Command.listSubreddits(filter);
            default:
                BUM("Valid commands: LIST, END");
        }
        flush();

        if (end) {
            return null;
        }

        return this;
    }

    private String prompt() {
        String prompt = new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE))
                .append("> ")
                .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE))
                .toAnsi();
        return prompt;
    }
}
