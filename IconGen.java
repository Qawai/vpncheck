import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

public class IconGen {

    static BufferedImage base(int s) {
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        float r = s * 0.22f;
        RoundRectangle2D.Float bg = new RoundRectangle2D.Float(0, 0, s, s, r, r);
        g.setClip(bg);
        LinearGradientPaint grad = new LinearGradientPaint(
                0, 0, s, s,
                new float[]{0f, 1f},
                new Color[]{new Color(0x29, 0x79, 0xFF), new Color(0x00, 0xC9, 0xA7)});
        g.setPaint(grad);
        g.fillRect(0, 0, s, s);

        // мягкое свечение
        g.setPaint(new Color(0x00, 0x00, 0x00, 0));
        g.setClip(null);

        float cx = s * 0.5f, cy = s * 0.5f;

        // глобус
        float R = s * 0.30f;
        g.setColor(new Color(0xFF, 0xFF, 0xFF, 235));
        g.setStroke(new BasicStroke(s * 0.035f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Ellipse2D.Float(cx - R, cy - R, R * 2, R * 2));

        // меридиан
        g.setStroke(new BasicStroke(s * 0.028f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Ellipse2D.Float(cx - R * 0.45f, cy - R, R * 0.9f, R * 2));

        // параллели
        g.setStroke(new BasicStroke(s * 0.028f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new java.awt.geom.Line2D.Float(cx - R, cy - R * 0.35f, cx + R, cy - R * 0.35f));
        g.draw(new java.awt.geom.Line2D.Float(cx - R, cy + R * 0.35f, cx + R, cy + R * 0.35f));

        // зелёный бейдж со стрелкой-пингом (с учётом направления)
        float br = s * 0.135f;
        float bx = s * 0.68f, by = s * 0.68f;
        g.setColor(new Color(0x12, 0x1A, 0x18, 0));
        g.fill(new Ellipse2D.Float(bx - br, by - br, br * 2, br * 2));
        g.setColor(new Color(0x12, 0x1A, 0x18));
        g.fill(new Ellipse2D.Float(bx - br * 0.72f, by - br * 0.72f, br * 1.44f, br * 1.44f));
        g.setColor(new Color(0x2E, 0xE0, 0x7A));
        g.fill(new Ellipse2D.Float(bx - br * 0.52f, by - br * 0.52f, br * 1.04f, br * 1.04f));

        // стрелка вверх (сигнал)
        GeneralPath arrow = new GeneralPath();
        float w = br * 0.42f;
        arrow.moveTo(bx, by - br * 0.30f);
        arrow.lineTo(bx + w, by - br * 0.02f);
        arrow.lineTo(bx + w * 0.45f, by - br * 0.02f);
        arrow.lineTo(bx + w * 0.45f, by + br * 0.30f);
        arrow.lineTo(bx - w * 0.45f, by + br * 0.30f);
        arrow.lineTo(bx - w * 0.45f, by - br * 0.02f);
        arrow.lineTo(bx - w, by - br * 0.02f);
        arrow.closePath();
        g.setColor(new Color(0x0B, 0x1A, 0x12));
        g.fill(arrow);

        g.dispose();
        return img;
    }

    static void save(String path, int s) throws Exception {
        File f = new File(path);
        f.getParentFile().mkdirs();
        ImageIO.write(base(s), "png", f);
    }

    public static void main(String[] args) throws Exception {
        String out = args.length > 0 ? args[0] : "res";
        save(out + "/mipmap-mdpi/ic_launcher.png", 48);
        save(out + "/mipmap-hdpi/ic_launcher.png", 72);
        save(out + "/mipmap-xhdpi/ic_launcher.png", 96);
        save(out + "/mipmap-xxhdpi/ic_launcher.png", 144);
        save(out + "/mipmap-xxxhdpi/ic_launcher.png", 192);
        System.out.println("icons generated in " + out);
    }
}
