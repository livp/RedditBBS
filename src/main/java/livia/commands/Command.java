package livia.commands;

//import com.google.common.flogger.FluentLogger;

import livia.Model.Subreddit;

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

  public static Command subreddit(Subreddit subreddit) {
    return SubredditCommand.create(subreddit);
  }
}
