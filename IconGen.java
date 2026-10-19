import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

// Иконка-спидометр: шкала с зелёной зоной и стрелкой (явная геометрия).
// Расцветка — из палитры каждой темы (bg, accent, good).
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

    // точка верхней дуги: theta=0 справа, 90 сверху, 180 слева
    static float[] pt(float cx, float cy, float r, double thetaDeg) {
        double a = Math.toRadians(thetaDeg);
        return new float[]{(float) (cx + r * Math.cos(a)), (float) (cy - r * Math.sin(a))};
    }

    static GeneralPath arcPath(float cx, float cy, float r, double from, double to) {
        GeneralPath p = new GeneralPath();
        for (double a = from; a <= to + 0.001; a += 3) {
            float[] q = pt(cx, cy, r, a);
            if (a == from) p.moveTo(q[0], q[1]);
            else p.lineTo(q[0], q[1]);
        }
        return p;
    }

    static BufferedImage icon(int s, int bgC, int accentC, int goodC) {
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        Color bg = new Color(bgC, true);
        Color accent = new Color(accentC, true);
        Color good = new Color(goodC, true);

        float pad = s * 0.03f;
        float r = s * 0.24f;
        RoundRectangle2D.Float rect =
                new RoundRectangle2D.Float(pad, pad, s - 2 * pad, s - 2 * pad, r, r);
        g.setColor(bg);
        g.fill(rect);
        g.setColor(accent);
        g.setStroke(new BasicStroke(s * 0.02f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(rect);

        float cx = s * 0.5f, cy = s * 0.62f;
        float R = s * 0.30f;

        // шкала (верхняя полуокружность)
        g.setColor(accent);
        g.setStroke(new BasicStroke(s * 0.05f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(arcPath(cx, cy, R, 0, 180));

        // зелёная зона (правый сектор)
        g.setColor(good);
        g.draw(arcPath(cx, cy, R, 0, 50));

        // засечки
        g.setStroke(new BasicStroke(s * 0.016f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (double t : new double[]{0, 45, 90, 135, 180}) {
            float[] a = pt(cx, cy, R + s * 0.04f, t);
            float[] b = pt(cx, cy, R + s * 0.08f, t);
            g.draw(new Line2D.Float(a[0], a[1], b[0], b[1]));
        }

        // стрелка в зелёную зону
        double na = Math.toRadians(28);
        float len = R * 0.82f;
        g.setColor(accent);
        g.setStroke(new BasicStroke(s * 0.032f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Float(cx, cy,
                (float) (cx + len * Math.cos(na)),
                (float) (cy - len * Math.sin(na))));

        // центр стрелки
        g.fill(new Ellipse2D.Float(cx - s * 0.045f, cy - s * 0.045f, s * 0.09f, s * 0.09f));

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
