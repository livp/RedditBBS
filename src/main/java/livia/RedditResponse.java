package livia;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

public class RedditResponse extends GenericJson {

  @Key
  public String kind;

  @Key
  public GenericJson data;

  @Override
  public String toString() {
    return String.format("Kind: %s\n%s", kind, data);
  }

}
