package livia;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.ArrayMap;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;

public class Model {

  public static class Listing {

    List<Subreddit> subreddits = new ArrayList<>();
    String after = null;

    public static Listing fromJson(GenericJson json) {
      Preconditions.checkArgument(json.containsKey("kind") && json.get("kind").equals("Listing"));

      Listing listing = new Listing();

      ArrayMap data = (ArrayMap)json.get("data");
      ArrayList<ArrayMap> children = (ArrayList<ArrayMap>)data.get("children");
      parseChildren(listing, children);
      listing.after = jsonValueOrEmpty(data, "after");

      return listing;
    }

    private static void parseChildren(Listing listing, ArrayList<ArrayMap> children) {
      children.forEach(child -> {
        if (child.containsKey("kind")) {
          if (child.get("kind").equals("t5")) {
            ArrayMap data = (ArrayMap) child.get("data");
            listing.subreddits.add(Subreddit.fromJson(data));
          }
        }
      });
    }

  }

  public static class Subreddit {

    public String displayName;
    public String description;

    public static Subreddit fromJson(ArrayMap json) {
      Subreddit subreddit = new Subreddit();

      subreddit.displayName = jsonValueOrEmpty(json, "display_name");
      subreddit.description = jsonValueOrEmpty(json, "description");

      return subreddit;
    }

    public String shortDescription(int length) {
      String lines[] = description.split("\n");
      if (lines.length < 1) {
        return "";
      }

      String line = lines[0];
      if (length < 0) {
        return line;
      }

      if (line.length() <= length) {
        return line;
      }
      return line.substring(0, length - 1);
    }

  }

  static String jsonValueOrEmpty(ArrayMap json, String key) {
    if (!json.containsKey(key)) {
      return "";
    }
    Object value = json.get(key);
    if (value instanceof String) {
      return (String)value;
    }
    return "";
  }
}
