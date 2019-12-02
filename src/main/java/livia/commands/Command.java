package livia.commands;

//import com.google.common.flogger.FluentLogger;

import livia.Model.*;
import livia.commands.ListMessagesCommand.PostSort;
/**
 * State machine.
 */
public abstract class Command {

    public abstract Command parseNextLine();

    public static RootCommand root() {
        return RootCommand.create();
    }

    public static Command listSubreddits(String filter) {
        return ListSubredditsCommand.create(filter);
    }

    public static Command subreddit(Subreddit subreddit, Command parent) {
        return SubredditCommand.create(subreddit, parent);
    }

    public static Command multi(Multi multi, Command parent) {
        return ListMessagesCommand.create(multi, PostSort.HOT, parent);
    }

    public static Command listPosts(Subreddit subreddit, ListMessagesCommand.PostSort postSort, Command parent) {
        return ListMessagesCommand.create(subreddit, postSort, parent);
    }

    public static Command listPosts(Multi multi, ListMessagesCommand.PostSort postSort, Command parent) {
        return ListMessagesCommand.create(multi, postSort, parent);
    }

    public static Command message(Message message, Command parent) {
        return MessageCommand.create(message, parent);
    }

    public static Command comments(Comment comment, Command parent) {
        return CommentCommand.create(comment, parent);
    }
}
