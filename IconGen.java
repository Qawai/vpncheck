import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.File;

// Генерирует ОДНУ PNG-иконку (192x192) -> res/mipmap/ic_launcher.png
public class IconGen {

    public static void main(String[] args) throws Exception {
        int s = 192;
        String out = args.length > 0 ? args[0] : "res/mipmap/ic_launcher.png";

        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        LinearGradientPaint grad = new LinearGradientPaint(
                0, 0, s, s, new float[]{0f, 1f},
                new Color[]{new Color(0x29, 0x79, 0xFF), new Color(0x00, 0xC9, 0xA7)});
        g.setPaint(grad);
        g.fillRect(0, 0, s, s);

        float cx = s * 0.47f, cy = s * 0.47f;

        // глобус
        float R = s * 0.30f;
        g.setColor(new Color(0xFF, 0xFF, 0xFF, 235));
        g.setStroke(new BasicStroke(s * 0.035f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Ellipse2D.Float(cx - R, cy - R, R * 2, R * 2));

        g.setStroke(new BasicStroke(s * 0.028f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Ellipse2D.Float(cx - R * 0.45f, cy - R, R * 0.9f, R * 2));
        g.draw(new java.awt.geom.Line2D.Float(cx - R, cy - R * 0.35f, cx + R, cy - R * 0.35f));
        g.draw(new java.awt.geom.Line2D.Float(cx - R, cy + R * 0.35f, cx + R, cy + R * 0.35f));

        // бейдж
        float br = s * 0.14f;
        float bx = s * 0.70f, by = s * 0.70f;
        g.setColor(new Color(0x12, 0x1A, 0x18));
        g.fill(new Ellipse2D.Float(bx - br, by - br, br * 2, br * 2));
        g.setColor(new Color(0x2E, 0xE0, 0x7A));
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
        g.setColor(new Color(0x0B, 0x1A, 0x12));
        g.fill(arrow);

        g.dispose();

        File f = new File(out);
        f.getParentFile().mkdirs();
        ImageIO.write(img, "png", f);
        System.out.println("icon -> " + out);
    }
}
