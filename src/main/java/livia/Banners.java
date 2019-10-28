package livia;

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
            + "      ²==--≥²²==--==²²≤--==²";

  public static final String WELCOME = "Welcome to Reddit BBS.";

  // Prevent instances.
  private Banners() {}

  /**
   * Computes the effective width of a multi-line string.
   */
  static int bannerWidth(String banner) {
    int width = 0;
    for (String s : banner.split("\n")) {
      if (s.length() > width) { width = s.length(); }
    }
    return width;
  }


}
