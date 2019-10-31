package livia;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.ArrayMap;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.korhner.asciimg.image.AsciiImgCache;
import io.korhner.asciimg.image.character_fit_strategy.ColorSquareErrorFitStrategy;
import io.korhner.asciimg.image.converter.AsciiToStringConverter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Model {

    public static class Listing {

        public Set<Subreddit> subreddits = new HashSet<>();
        public List<Message> messages = new ArrayList<>();
        public String after = null;

        public static Listing fromJson(GenericJson json) {
            Preconditions.checkArgument(json.containsKey("kind") && json.get("kind").equals("Listing"));

            Listing listing = new Listing();

            ArrayMap data = (ArrayMap) json.get("data");
            ArrayList<ArrayMap> children = (ArrayList<ArrayMap>) data.get("children");
            parseChildren(listing, children);
            listing.after = stringOrEmpty(data, "after");

            return listing;
        }

        private static void parseChildren(Listing listing, ArrayList<ArrayMap> children) {
            children.forEach(child -> {
                String kind = stringOrEmpty(child, "kind");
              ArrayMap data = (ArrayMap) child.get("data");
                    switch (kind){
                      case "t5":
                        listing.subreddits.add(Subreddit.fromJson(data));
                        break;
                      case "t4":
                        listing.messages.add(Message.fromJson(data));
                        break;
                    }
            });
        }

    }

    public static class Subreddit {

        private static final AsciiImgCache cache = AsciiImgCache.create(
                new Font("Courier", Font.BOLD, 6));

        private static final AsciiToStringConverter stringConverter =
                new AsciiToStringConverter(cache, new ColorSquareErrorFitStrategy());

        public String displayName;
        public String description;
        public String icon;

        public static Subreddit fromJson(ArrayMap json) {
            Subreddit subreddit = new Subreddit();

            subreddit.displayName = stringOrEmpty(json, "display_name");
            subreddit.description = stringOrEmpty(json, "description");
            BufferedImage image = imageOrNull(json, "icon_img");
            if (image != null) {
                BufferedImage largeImage = resizeImage(image, 256, 256);
                subreddit.icon = stringConverter.convertImage(largeImage).toString();
            }
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

    public static class Message {
      public String title;

      public static Message fromJson(ArrayMap json) {
        Message message = new Message();

        message.title = stringOrEmpty(json, "title");
        return message;
      }
    }

    static String stringOrEmpty(ArrayMap json, String key) {
        if (!json.containsKey(key)) {
            return "";
        }
        Object value = json.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return "";
    }

    static BufferedImage imageOrNull(ArrayMap json, String key) {
        if (!json.containsKey(key)) {
            return null;
        }
        Object value = json.get(key);
        if (value instanceof String) {
            String url = (String) value;
            if (Strings.isNullOrEmpty(url)) {
                return null;
            }
            try {
                return ImageIO.read(new URL(url));
            } catch (IOException _ignored) {
                return null;
            }
        }
        return null;
    }

    static BufferedImage resizeImage(BufferedImage img, int height, int width) {
        Image tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }
}
