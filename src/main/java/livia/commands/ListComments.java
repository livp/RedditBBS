package livia.commands;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.util.ArrayMap;
import livia.Model.*;
import livia.singletons.Network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static livia.Banners.BIG_BADA_BUM;
import static livia.Banners.OH_FUCK;

public class ListComments extends Command {

    private final Message message;
    private final List<Comment> comments = new ArrayList<>();

    public static ListComments create(Message message) {
        ListComments listComments = new ListComments(message);
        listComments.runQuery();
        // TODO: display comments.
        return listComments;
    }

    private ListComments(Message message) {
        this.message = message;
    }

    @Override
    public Command parseNextLine() {
        return null;
    }

    private void runQuery() {
        GenericUrl url = new GenericUrl(
                String.format("https://www.reddit.com/r/%s/comments/%s.json",
                        message.subreditName, message.id));
        try {
            HttpRequest request = Network.request(url);
            HttpResponse httpResponse = request.execute();
            List<ArrayMap> parsedResponse = httpResponse.parseAs(ArrayList.class);
            if (parsedResponse.size() > 2) {
                BIG_BADA_BUM(String.format("I can't parse comments. %s", url));
                return;
            }
            ArrayMap outerCommentsMap = (ArrayMap)parsedResponse.get(1).get("data");
            ArrayList commentsMap = (ArrayList)outerCommentsMap.get("children");
            for (int i = 0; i < commentsMap.size(); i++) {
                ArrayMap outerJson = (ArrayMap)commentsMap.get(i);
                ArrayMap commentMap = (ArrayMap)outerJson.get("data");
                comments.add(Comment.fromJson(commentMap));
            }
        } catch (IOException e) {
            e.printStackTrace();
            OH_FUCK(String.format("[GET %s] ------>>>>> ", url.toString(), e.getMessage()));
        }
    }
}