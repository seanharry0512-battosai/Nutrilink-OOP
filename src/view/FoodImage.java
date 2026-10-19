package view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/** Square food picture with rounded corners. Pictures are loaded once and then reused. */
public class FoodImage extends JComponent {
    private static final Map<String, BufferedImage> CACHE = new HashMap<>();

    private final int size;
    private final BufferedImage image;
    private final String initial;

    public FoodImage(String foodName, int size) {
        this(foodName, null, size);
    }

    public FoodImage(String foodName, String donationType, int size) {
        this.size = size;
        Dimension d = new Dimension(size, size);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);

        String imageName = imageNameFor(foodName);
        if (imageName == null) imageName = imageNameFor(donationType);
        if (imageName == null) imageName = "cooked_meals.jpg";
        this.image = scaled(imageName, size);
        this.initial = (foodName == null || foodName.isBlank()) ? "?" : foodName.trim().substring(0, 1).toUpperCase();
    }

    private static String imageNameFor(String value) {
        if (value == null) return null;
        String name = value.toLowerCase().replaceAll("[^a-z0-9]", "");
        if (name.contains("chicken")) return "chicken.jpg";
        if (name.contains("bread") || name.contains("bakery") || name.contains("pastr")) return "bread.png";
        if (name.contains("vegetable")) return "vegetables.jpg";
        if (name.contains("fruit")) return "fruits.jpg";
        if (name.contains("rice") || name.contains("grain")) return "rice.jpg";
        if (name.contains("cooked") || name.contains("meal") || name.contains("food")) return "cooked_meals.jpg";
        return null;
    }

    /** Crops the picture to a square (so it is not stretched) and scales it once. */
    private static BufferedImage scaled(String name, int size) {
        String key = name + "@" + size;
        if (CACHE.containsKey(key)) return CACHE.get(key);
        BufferedImage src = Theme.loadImage(name);
        BufferedImage out = null;
        if (src != null) {
            int side = Math.min(src.getWidth(), src.getHeight());
            Image smooth = new ImageIcon(src.getSubimage((src.getWidth() - side) / 2, (src.getHeight() - side) / 2, side, side)
                    .getScaledInstance(size, size, Image.SCALE_SMOOTH)).getImage();
            out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = out.createGraphics();
            g.drawImage(smooth, 0, 0, null);
            g.dispose();
        }
        CACHE.put(key, out);
        return out;
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        float arc = size * 0.22f;
        if (image != null) {
            g2.setClip(new RoundRectangle2D.Float(0, 0, size, size, arc, arc));
            g2.drawImage(image, 0, 0, null);
        } else {                                                    // no picture file: show a letter instead
            g2.setColor(new Color(0xDFF3E6));
            g2.fill(new RoundRectangle2D.Float(0, 0, size, size, arc, arc));
            g2.setColor(Theme.GREEN);
            g2.setFont(Theme.font(Font.BOLD, size / 2));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(initial, (size - fm.stringWidth(initial)) / 2, (size - fm.getHeight()) / 2 + fm.getAscent());
        }
        g2.dispose();
    }
}
