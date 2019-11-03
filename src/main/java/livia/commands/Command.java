package livia.commands;

//import com.google.common.flogger.FluentLogger;

import livia.Model.*;

/**
 * State machine.
 */
public abstract class Command {

    public abstract Command parseNextLine();

    public static Root root() {
        return Root.create();
    }

    public static Command listSubreddits(String filter) {
        return ListSubreddits.create(filter);
    }

    public static Command subreddit(Subreddit subreddit, Command parent) {
        return SubredditCommand.create(subreddit, parent);
    }

    public static Command listPosts(Subreddit subreddit, ListMessages.PostSort postSort, Command parent) {
        return ListMessages.create(subreddit, postSort, parent);
    }

    public static Command message(Message message, Command parent) {
        return MessageCommand.create(message, parent);
    }
}
