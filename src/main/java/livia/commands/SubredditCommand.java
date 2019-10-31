package livia.commands;

import com.google.common.base.Strings;
import livia.Banners;
import livia.Model.Subreddit;
import livia.singletons.TheTerminal;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.DefaultParser;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import static livia.Banners.BUM;
import static livia.Banners.centerBanner;
import static livia.singletons.TheTerminal.flush;

public class SubredditCommand extends Command {

    private boolean firstTime = true;
    private Subreddit subreddit = null;

    public static Command create(Subreddit subreddit) {
        SubredditCommand command = new SubredditCommand();
        command.subreddit = subreddit;
        return command;
    }

    @Override
    public Command parseNextLine() {
        if (firstTime) {
            subredditBanner();
        }
        firstTime = false;
        LineReader reader = LineReaderBuilder.builder()
                .terminal(TheTerminal.get())
                .parser(new DefaultParser())
                .build();
        String line = reader.readLine(prompt());
        ParsedLine parsedLine = reader.getParser().parse(line, 0);
        String command = parsedLine.words().get(0);

        boolean end = false;

        switch (command.toUpperCase()) {
            case "BACK":
                return Command.root();
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
                .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.MAGENTA));
        builder.append(String.format("%s > ", subreddit.displayName));
        String prompt = builder.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE))
                .toAnsi();
        return prompt;
    }

    private void subredditBanner() {
        if (!Strings.isNullOrEmpty(subreddit.icon)) {
            TheTerminal.writer().print(centerBanner(subreddit.icon));
        }
    }
}