import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

// Генерирует 8 иконок, окрашенных под каждую тему приложения.
// Палитры совпадают с THEMES в MainActivity: bg, text, accent, grey, good, warn, bad.
public class IconGen {

    static final int[][] THEMES = {
            {0xFF101418, 0xFFE8EAED, 0xFF4FC3F7, 0xFF9AA0A6, 0xFF66BB6A, 0xFFFFD54F, 0xFFEF5350},
            {0xFF000000, 0xFFE8EAED, 0xFF00E676, 0xFF757575, 0xFF00E676, 0xFFFFD54F, 0xFFEF5350},
            {0xFFF5F5F5, 0xFF212121, 0xFF1565C0, 0xFF757575, 0xFF2E7D32, 0xFFF9A825, 0xFFC62828},
            {0xFF0D1117, 0xFF3FB950, 0xFF3FB950, 0xFF8B949E, 0xFF3FB950, 0xFFD29922, 0xFFF85149},
            {0xFFFAF7F2, 0xFF4A4440, 0xFF9C7A54, 0xFF9E958D, 0xFF5B9A6E, 0xFFC9A227, 0xFFC46B6B},
            {0xFF22201E, 0xFFE8E2DB, 0xFFC9A97E, 0xFF9A928A, 0xFF7CB98F, 0xFFD4B04A, 0xFFD08080},
            {0xFF232029, 0xFFE6E1F0, 0xFFB8A1D9, 0xFF9A93A8, 0xFF8FC7A5, 0xFFD9C06B, 0xFFD98A9A},
            {0xFF1B2422, 0xFFDDEAE5, 0xFF8FD6BD, 0xFF8FA39C, 0xFF8FD6BD, 0xFFD6C56F, 0xFFE08A8A}
    };

    static int[] unpack(int c) {
        return new int[]{(c >> 16) & 0xFF, (c >> 8) & 0xFF, c & 0xFF};
    }

    static BufferedImage icon(int s, int bgC, int accentC, int goodC) {
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // фон — цвет темы
        Color bg = new Color(bgC, true);
        // акцент темы
        Color accent = new Color(accentC, true);
        Color good = new Color(goodC, true);

        float pad = s * 0.03f;
        float r = s * 0.24f;
        RoundRectangle2D.Float bgRect =
                new RoundRectangle2D.Float(pad, pad, s - 2 * pad, s - 2 * pad, r, r);
        g.setColor(bg);
        g.fill(bgRect);

        // окантовка акцентным цветом
        g.setColor(accent);
        g.setStroke(new BasicStroke(s * 0.022f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(bgRect);

        float cx = s * 0.46f, cy = s * 0.46f;
        float R = s * 0.28f;

        // глобус акцентным цветом
        g.setColor(accent);
        g.setStroke(new BasicStroke(s * 0.032f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Ellipse2D.Float(cx - R, cy - R, R * 2, R * 2));

        g.setStroke(new BasicStroke(s * 0.024f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Ellipse2D.Float(cx - R * 0.45f, cy - R, R * 0.9f, R * 2));
        g.draw(new java.awt.geom.Line2D.Float(cx - R, cy - R * 0.35f, cx + R, cy - R * 0.35f));
        g.draw(new java.awt.geom.Line2D.Float(cx - R, cy + R * 0.35f, cx + R, cy + R * 0.35f));

        // бейдж
        float br = s * 0.135f;
        float bx = s * 0.70f, by = s * 0.70f;
        int[] dark = {0x12, 0x1A, 0x18};
        g.setColor(new Color(dark[0], dark[1], dark[2]));
        g.fill(new Ellipse2D.Float(bx - br, by - br, br * 2, br * 2));
        g.setColor(good);
        g.fill(new Ellipse2D.Float(bx - br * 0.72f, by - br * 0.72f, br * 1.44f, br * 1.44f));

        // стрелка вверх
        GeneralPath arrow = new GeneralPath();
        float w = br * 0.45f;
        arrow.moveTo(bx, by - br * 0.34f);
        arrow.lineTo(bx + w, by - br * 0.02f);
        arrow.lineTo(bx + w * 0.45f, by - br * 0.02f);
        arrow.lineTo(bx + w * 0.45f, by + br * 0.34f);
        arrow.lineTo(bx - w * 0.45f, by + br * 0.34f);
        arrow.lineTo(bx - w * 0.45f, by - br * 0.02f);
        arrow.lineTo(bx - w, by - br * 0.02f);
        arrow.closePath();
        g.setColor(new Color(dark[0], dark[1], dark[2]));
        g.fill(arrow);

        g.dispose();
        return img;
    }

    public static void main(String[] args) throws Exception {
        String out = args.length > 0 ? args[0] : "res/mipmap";
        int s = 192;
        for (int i = 0; i < THEMES.length; i++) {
            int[] t = THEMES[i];
            BufferedImage img = icon(s, t[0], t[2], t[4]);
            File f = new File(out, "ic_launcher_" + i + ".png");
            f.getParentFile().mkdirs();
            ImageIO.write(img, "png", f);
            System.out.println("icon theme " + i + " -> " + f.getPath());
        }
    }
}
