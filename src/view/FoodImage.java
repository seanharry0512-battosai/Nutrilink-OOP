package view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;

public class FoodImage extends JComponent {
    private final int size;
    private Image image;

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

        File file = new File("resources/images/" + imageName);
        if (file.isFile()) {
            image = new ImageIcon(file.getPath()).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        }
    }

    private static String imageNameFor(String value) {
        if (value == null) return null;
        String name = value.toLowerCase().replaceAll("[^a-z0-9]", "");
        if (name.contains("chicken")) return "chicken.jpg";
        if (name.contains("bread") || name.contains("bakery")) return "bread.png";
        if (name.contains("vegetable")) return "vegetables.jpg";
        if (name.contains("fruit")) return "fruits.jpg";
        if (name.contains("rice") || name.contains("grain")) return "rice.jpg";
        if (name.contains("cooked") || name.contains("meal") || name.contains("food")) return "cooked_meals.jpg";
        return null;
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        float arc = size * 0.22f;
        if (image != null) {
            g2.setClip(new RoundRectangle2D.Float(0, 0, size, size, arc, arc));
            g2.drawImage(image, 0, 0, this);
        }
        g2.dispose();
    }
}