package livia.commands;

import livia.Model.*;

public class CommentCommand extends Command {

    private final Comment comment;
    private final Command parent;

    private CommentCommand(Comment comment, Command parent) {
        this.comment = comment;
        this.parent = parent;
    }

    public static CommentCommand create(Comment comment, Command parent) {
        CommentCommand command = new CommentCommand(comment, parent);
        return command;
    }


    @Override
    public Command parseNextLine() {
        return null;
    }
}
