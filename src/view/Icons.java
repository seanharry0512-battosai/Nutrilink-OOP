package view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

/** Small icons drawn with Java2D (no image files needed). */
final class Icons {
    private Icons() { }

    /** Bell for the dashboard header. */
    static class BellIcon implements Icon {
        private final Color color;
        BellIcon(Color color) { this.color = color; }

        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new Arc2D.Double(x + 6, y + 3, 18, 20, 0, 180, Arc2D.OPEN));
            g2.drawLine(x + 6, y + 13, x + 4, y + 23);
            g2.drawLine(x + 24, y + 13, x + 26, y + 23);
            g2.drawLine(x + 3, y + 23, x + 27, y + 23);
            g2.drawArc(x + 12, y + 24, 6, 5, 180, 180);
            g2.dispose();
        }
        @Override public int getIconWidth() { return 30; }
        @Override public int getIconHeight() { return 32; }
    }

    /** White leaf for the NutriLink logo. */
    static class LeafIcon implements Icon {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            Path2D leaf = new Path2D.Double();
            leaf.moveTo(3, 26);
            leaf.curveTo(0, 12, 10, 2, 25, 2);
            leaf.curveTo(26, 16, 16, 27, 3, 26);
            g2.setColor(Color.WHITE);
            g2.fill(leaf);
            g2.setColor(Theme.GREEN);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(6, 23, 17, 12);
            g2.dispose();
        }
        @Override public int getIconWidth() { return 28; }
        @Override public int getIconHeight() { return 28; }
    }

    /** Outline icon for each sidebar item. */
    static class NavIcon implements Icon {
        private final String type;
        private final Color color;

        NavIcon(String type, Color color) {
            this.type = type;
            this.color = color;
        }

        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(color);
            g2.translate(x, y);
            switch (type) {
                case DashboardView.HOME -> {
                    Path2D roof = new Path2D.Double();
                    roof.moveTo(3, 13); roof.lineTo(14, 3); roof.lineTo(25, 13);
                    g2.draw(roof);
                    g2.drawRect(6, 13, 16, 12);
                    g2.drawRect(11, 18, 6, 7);
                }
                case DashboardView.DONATIONS -> {
                    g2.drawRect(4, 14, 20, 10);
                    g2.drawLine(2, 14, 26, 14);
                    Path2D heart = new Path2D.Double();
                    heart.moveTo(14, 11);
                    heart.curveTo(8, 8, 9, 2, 14, 5);
                    heart.curveTo(19, 2, 20, 8, 14, 11);
                    g2.draw(heart);
                }
                case DashboardView.INVENTORY -> {
                    g2.drawRect(3, 3, 22, 22);
                    g2.drawLine(3, 12, 25, 12);
                    g2.drawLine(3, 19, 25, 19);
                    g2.drawLine(14, 3, 14, 12);
                }
                case DashboardView.COLLECTION -> {
                    Path2D cart = new Path2D.Double();
                    cart.moveTo(2, 5); cart.lineTo(6, 5); cart.lineTo(9, 18); cart.lineTo(23, 18);
                    cart.lineTo(25, 8); cart.lineTo(7, 8);
                    g2.draw(cart);
                    g2.draw(new Ellipse2D.Double(9, 21, 4, 4));
                    g2.draw(new Ellipse2D.Double(19, 21, 4, 4));
                }
                case DashboardView.DISTRIBUTION -> {
                    g2.draw(new Arc2D.Double(7, 3, 14, 14, 0, 180, Arc2D.OPEN));
                    g2.drawLine(7, 10, 14, 25);
                    g2.drawLine(21, 10, 14, 25);
                    g2.draw(new Ellipse2D.Double(11.5, 7.5, 5, 5));
                }
                case DashboardView.BENEFICIARIES -> {
                    g2.draw(new Ellipse2D.Double(11, 3, 6, 6));
                    g2.draw(new Arc2D.Double(7, 12, 14, 14, 0, 180, Arc2D.OPEN));
                    g2.draw(new Ellipse2D.Double(2, 9, 5, 5));
                    g2.draw(new Arc2D.Double(0, 17, 10, 8, 0, 180, Arc2D.OPEN));
                    g2.draw(new Ellipse2D.Double(21, 9, 5, 5));
                    g2.draw(new Arc2D.Double(18, 17, 10, 8, 0, 180, Arc2D.OPEN));
                }
                case DashboardView.REPORTS -> {
                    g2.drawRoundRect(5, 2, 18, 24, 4, 4);
                    g2.drawLine(8, 19, 12, 14);
                    g2.drawLine(12, 14, 15, 18);
                    g2.drawLine(15, 18, 20, 11);
                }
                case DashboardView.NOTIFICATIONS -> {
                    g2.draw(new Arc2D.Double(7, 3, 14, 16, 0, 180, Arc2D.OPEN));
                    g2.drawLine(7, 11, 5, 20);
                    g2.drawLine(21, 11, 23, 20);
                    g2.drawLine(4, 20, 24, 20);
                    g2.drawArc(11, 21, 6, 5, 180, 180);
                }
                case DashboardView.SETTINGS -> {
                    g2.draw(new Ellipse2D.Double(9, 9, 10, 10));
                    for (int i = 0; i < 8; i++) {
                        double a = Math.PI / 4 * i;
                        g2.drawLine((int) Math.round(14 + Math.cos(a) * 7), (int) Math.round(14 + Math.sin(a) * 7),
                                (int) Math.round(14 + Math.cos(a) * 11), (int) Math.round(14 + Math.sin(a) * 11));
                    }
                }
                case "Logout" -> {
                    Path2D door = new Path2D.Double();
                    door.moveTo(12, 4); door.lineTo(3, 4); door.lineTo(3, 24); door.lineTo(12, 24);
                    g2.draw(door);
                    g2.drawLine(9, 14, 25, 14);
                    g2.drawLine(20, 9, 25, 14);
                    g2.drawLine(20, 19, 25, 14);
                }
                default -> { }
            }
            g2.dispose();
        }

        @Override public int getIconWidth() { return 28; }
        @Override public int getIconHeight() { return 28; }
    }
}
