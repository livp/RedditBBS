package livia;

import com.google.api.client.util.ArrayMap;
import com.google.common.base.Strings;
import io.korhner.asciimg.image.AsciiImgCache;
import io.korhner.asciimg.image.character_fit_strategy.ColorSquareErrorFitStrategy;
import io.korhner.asciimg.image.character_fit_strategy.StructuralSimilarityFitStrategy;
import io.korhner.asciimg.image.converter.AsciiToStringConverter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class ASCIImage {

    private static final AsciiImgCache cache = AsciiImgCache.create(
            new Font("Courier", Font.BOLD, 6));
    private static final AsciiToStringConverter stringConverter =
//            new AsciiToStringConverter(cache, new ColorSquareErrorFitStrategy());
            new AsciiToStringConverter(cache, new StructuralSimilarityFitStrategy());

    private String stringImage = null;
    private BufferedImage image = null;

    public static ASCIImage create(String url) {
        try {
            ASCIImage asciImage = new ASCIImage();
            asciImage.image = ImageIO.read(new URL(url));
            return  asciImage;
        } catch (IOException _ignored) {
        }
        return null;
    }

    public static ASCIImage create(ArrayMap json, String key) {
        if (!json.containsKey(key)) {
            return null;
        }
        Object value = json.get(key);
        if (value instanceof String) {
            String url = (String) value;
            if (Strings.isNullOrEmpty(url)) {
                return null;
            }
            // Cleanup amp;
            url = url.replace("amp;", "");
            return create(url);
        }
        return null;
    }

    public int width() {
        if (image != null) {
            return image.getWidth();
        }
        return 0;
    }

    public int height() {
        if (image != null) {
            return image.getHeight();
        }
        return 0;
    }

    public String resizeImage(int height, int width) {
        if (image != null &&
                Strings.isNullOrEmpty(stringImage)) {
            Image tmp = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = resized.createGraphics();
            g2d.drawImage(tmp, 0, 0, null);
            g2d.dispose();
            stringImage = stringConverter.convertImage(resized).toString();
        }

        return stringImage;
    }
}
