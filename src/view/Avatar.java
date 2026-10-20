package view;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/** Round picture: shows a photo if one is set, otherwise the first letter of a name. */
public class Avatar extends JComponent {
    private final int size;
    private final Color background;
    private final Color foreground;
    private String initial = "?";
    private BufferedImage photo;   // already cropped to a circle, or null

    public Avatar(int size, Color background, Color foreground) {
        this.size = size;
        this.background = background;
        this.foreground = foreground;
        Dimension d = new Dimension(size, size);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
    }

    public void setInitial(String initial) {
        this.initial = initial;
        repaint();
    }

    /** Sets the photo (null goes back to the initial). Any shape works; it is center-cropped to a circle. */
    public void setImage(BufferedImage image) {
        photo = (image == null) ? null : toCircle(image);
        repaint();
    }

    /** Same as setImage, but from saved image bytes (PNG/JPG). Bad or empty bytes just show the initial. */
    public void setImageBytes(byte[] bytes) {
        BufferedImage image = null;
        if (bytes != null && bytes.length > 0) {
            try {
                image = ImageIO.read(new ByteArrayInputStream(bytes));
            } catch (IOException e) {
                image = null;
            }
        }
        setImage(image);
    }

    private BufferedImage toCircle(BufferedImage src) {
        int side = Math.min(src.getWidth(), src.getHeight());
        BufferedImage square = src.getSubimage((src.getWidth() - side) / 2, (src.getHeight() - side) / 2, side, side);
        Image scaled = square.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.fill(new Ellipse2D.Float(0, 0, size - 1, size - 1));
        g.setComposite(AlphaComposite.SrcIn);
        g.drawImage(scaled, 0, 0, null);
        g.dispose();
        return out;
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (photo != null) {
            g2.drawImage(photo, 0, 0, null);
            g2.dispose();
            return;
        }
        g2.setColor(background);
        g2.fillOval(0, 0, size - 1, size - 1);
        g2.setColor(foreground);
        g2.setFont(Theme.font(Font.BOLD, size / 2));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(initial, (size - fm.stringWidth(initial)) / 2, (size - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }
}
