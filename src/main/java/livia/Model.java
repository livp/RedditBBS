package livia;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.util.ArrayMap;
import com.google.common.base.Preconditions;
import livia.singletons.Network;
import livia.singletons.TheTerminal;

import java.io.IOException;
import java.util.*;

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
            listing.after = stringOrEmpty(data, "after");

            parseChildren(listing, children);

            return listing;
        }

        private static void parseChildren(Listing listing, ArrayList<ArrayMap> children) {
            children.forEach(child -> {
                String kind = stringOrEmpty(child, "kind");
                ArrayMap data = (ArrayMap) child.get("data");
                switch (kind) {
                    case "t5":
                        listing.subreddits.add(Subreddit.fromListing(data));
                        break;
                    case "t3":
                        listing.messages.add(Message.fromJson(data));
                        break;
                }
            });
        }

    }

    public static class Subreddit {
        public String displayName;
        public String description;
        public String icon;

        public static Subreddit fetch(String name) throws IOException {
            GenericUrl url = new GenericUrl(String.format("https://www.reddit.com/r/%s/about.json", name));
            HttpRequest request = Network.request(url);
            HttpResponse httpResponse = request.execute();
            GenericJson jsonResponse = httpResponse.parseAs(GenericJson.class);
            ArrayMap map = (ArrayMap) jsonResponse.get("data");
            return fromListing(map);
        }

        public static Subreddit fromListing(ArrayMap json) {
            Subreddit subreddit = new Subreddit();

            subreddit.displayName = stringOrEmpty(json, "display_name");
            subreddit.description = stringOrEmpty(json, "description");
            ASCIImage image = ASCIImage.create(json, "icon_img");
            if (image != null) {
                subreddit.icon = image.resizeImage(200, 200);
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
        public List<String> urls = new ArrayList<>();
        public List<String> images = new ArrayList<>();

        public static Message fromJson(ArrayMap json) {
            Message message = new Message();
            message.title = stringOrEmpty(json, "title");

            // This needs a fluid construct.
            if (json.containsKey("preview")) {
                ArrayMap preview = (ArrayMap) json.get("preview");
                if (preview.containsKey("images")) {
                    ArrayList<ArrayMap> previewImages = (ArrayList<ArrayMap>) preview.get("images");
                    for (ArrayMap image : previewImages) {
                        if (image.containsKey("source")) {
                            ArrayMap source = (ArrayMap) image.get("source");
                            if (source.containsKey("url")) {
                                String url = (String) source.get("url");
                                url = url.replace("amp;", "");
                                message.urls.add(url);
                            }
                        }
                    }
                }
            }
        return message;
        }

        public String asciiImage() {
            String url = urls.get(urls.size() - 1); // The last image is the smallest
            ASCIImage image = ASCIImage.create(url);
            double factor = 2;
            double width = TheTerminal.width() * factor;
            if (width == 0) {
                width = 250.0;
            }
            double ratio = width /  (double)image.width();
            double height = ratio * (double)image.height();
            return image.resizeImage((int)Math.round(width), (int)Math.round(height));
        }
    }

    public static String stringOrEmpty(ArrayMap json, String key) {
        if (!json.containsKey(key)) {
            return "";
        }
        Object value = json.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return "";
    }
}
