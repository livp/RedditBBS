package livia;

import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

public class Banners {

  public static final String BIG_ALIEN =
              "                  ,d\"=≥,.,qOp,\n"
            + "                ,7'  ''²$(  )\n"
            + "                ,7'      '?q$7'\n"
            + "            ..,$$,.\n"
            + "  ,.  .,,--***²\"\"²***--,,.  .,\n"
            + "²   ,p²''              ''²q,   ²\n"
            + ":  ,7'                      '7,  :\n"
            + "' $      ,db,      ,db,      $ '\n"
            + "  '$      ²$$²      ²$$²      $' \n"
            + "  '$                          $' \n"
            + "  '$.     .,        ,.     .$'\n"
            + "    'b,     '²«»«»«»²'     ,d'\n"
            + "    '²?bn,,          ,,nd?²'\n"
            + "      ,7$ ''²²²²²²²²'' $7,\n"
            + "    ,² ²$              $² ²,\n"
            + "    $  :$              $:  $\n"
            + "    $   $              $   $\n"
            + "    'b  q:            :p  d'\n"
            + "      '²«?$.          .$?»²'\n"
            + "        'b            d'\n"
            + "      ,²²'?,.      .,?'²²,\n"
            + "      ²==--≥²²==--==²²≤--==²\n";

  public static final String WELCOME_LINE = "Welcome to Reddit BBS";

  /**
   * Gets the nice little alien shown in the terminal.
   */
  static void displayWelcomeBanner(Terminal terminal) {
    AttributedString welcome = new AttributedStringBuilder()
        .style(AttributedStyle.DEFAULT.bold())
        .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
        .append(centerBanner(terminal, BIG_ALIEN))
        .style(AttributedStyle.DEFAULT.boldOff())
        .append(centerBanner(terminal, WELCOME_LINE))
        .toAttributedString();
    terminal.writer().println(welcome.toAnsi());
    terminal.flush();
  }

  /**
   * Computes the effective width of a multi-line string.
   */
  static int bannerWidth(String banner) {
    int bannerWidth = 0;
    for (String s : banner.split("\n")) {
      if (s.length() > bannerWidth) {
        bannerWidth = s.length();
      }
    }
    return bannerWidth;
  }

  /**
   * Pads a banner with spaces to center it in the terminal.
   */
  static String centerBanner(Terminal terminal, String banner) {
    int pad = Math.floorDiv(terminal.getWidth() - bannerWidth(banner), 2);
    if (pad <= 0) {
      return banner;
    }
    String padded = "";
    for (String s : banner.split("\n")) {
      padded += String.format("%" + pad + "c", ' ') + s + "\n";
    }
    return padded;
  }

  static void errorMessage(Terminal terminal, String message) {
    String fancyError = new AttributedStringBuilder()
        .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
        .append(String.format("!!! ERROR !!!! %s", message))
        .toAnsi();
    terminal.writer().println(fancyError);
    terminal.flush();
  }


  // Prevent instantiation.
  private Banners() {}


}
