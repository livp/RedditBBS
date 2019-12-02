package livia;

import livia.singletons.TheTerminal;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import static livia.Banners.ErrorType.*;
import static livia.singletons.TheTerminal.flush;
import static livia.singletons.TheTerminal.writer;
import static org.jline.utils.AttributedStyle.*;

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
  public static void displayWelcomeBanner() {
    AttributedString welcome = new AttributedStringBuilder()
        .style(AttributedStyle.DEFAULT.bold())
        .style(AttributedStyle.DEFAULT.foreground(RED))
        .append(centerBanner(BIG_ALIEN))
        .style(AttributedStyle.DEFAULT.boldOff())
        .append(centerBanner(WELCOME_LINE))
        .toAttributedString();
    writer().println(welcome.toAnsi());
    flush();
  }

  /**
   * Computes the effective width of a multi-line string.
   */
  public static int bannerWidth(String banner) {
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
  public static String centerBanner(String banner) {
    int pad = Math.floorDiv(TheTerminal.width() - bannerWidth(banner), 2);
    if (pad <= 0) {
      return banner;
    }
    String padded = "";
    for (String s : banner.split("\n")) {
      padded += String.format("%" + pad + "c", ' ') + s + "\n";
    }
    return padded;
  }

  public enum ErrorType {
    BUM(AttributedStyle.DEFAULT.foreground(YELLOW)),
    BIG_BADA_BUM(AttributedStyle.DEFAULT.foreground(BRIGHT | RED)),
    OH_FUCK(AttributedStyle.DEFAULT.bold().blink().foreground(BRIGHT | RED));

    private AttributedStyle style;

    ErrorType(AttributedStyle style) {
      this.style = style;
    }

    AttributedStyle getStyle() {
      return style;
    }
  }

  public static void errorMessage(ErrorType type, String message) {
    errorMessage(TheTerminal.get(), type, message);
  }

  public static void BUM(String message) {
    errorMessage(TheTerminal.get(), BUM, message);
  }
  public static void BIG_BADA_BUM(String message) {
    errorMessage(TheTerminal.get(), BIG_BADA_BUM, message);
  }
  public static void OH_FUCK(String message) {
    errorMessage(TheTerminal.get(), OH_FUCK, message);
  }

  public static void errorMessage(Terminal terminal, ErrorType type, String message) {
    String fancyError = new AttributedStringBuilder()
        .style(type.getStyle())
        .append(String.format("!!! ERROR !!!! %s", message))
        .toAnsi();
    terminal.writer().println(fancyError);
    terminal.flush();
    if (type.equals(ErrorType.OH_FUCK)) {
      System.err.println("All is lost.");
      System.exit(-1);
    }
  }

  // Prevent instantiation.
  private Banners() {}


}
