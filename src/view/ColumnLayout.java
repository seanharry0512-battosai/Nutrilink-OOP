package view;

import javax.swing.*;
import java.awt.*;

/**
 * Splits the width by fixed percentages so a header and all its rows line up.
 * Components marked with {@link #centered(JComponent)} keep their own size and sit in the middle of their column.
 */
final class ColumnLayout implements LayoutManager {
    private static final String CENTER_KEY = "columnLayout.center";

    private final double[] weights;
    private final int fixedWidth;      // extra fixed column on the right (0 = none)

    ColumnLayout(double[] weights) { this(weights, 0); }

    ColumnLayout(double[] weights, int fixedWidth) {
        this.weights = weights;
        this.fixedWidth = fixedWidth;
    }

    static <T extends JComponent> T centered(T c) {
        c.putClientProperty(CENTER_KEY, Boolean.TRUE);
        return c;
    }

    @Override public void addLayoutComponent(String name, Component comp) { }
    @Override public void removeLayoutComponent(Component comp) { }

    @Override public Dimension preferredLayoutSize(Container parent) {
        int h = 0;
        for (Component c : parent.getComponents()) h = Math.max(h, c.getPreferredSize().height);
        Insets in = parent.getInsets();
        return new Dimension(600, h + in.top + in.bottom);
    }

    @Override public Dimension minimumLayoutSize(Container parent) { return preferredLayoutSize(parent); }

    @Override public void layoutContainer(Container parent) {
        Insets in = parent.getInsets();
        int total = parent.getWidth() - in.left - in.right - fixedWidth;
        int h = parent.getHeight() - in.top - in.bottom;
        int x = in.left;
        for (int i = 0; i < parent.getComponentCount(); i++) {
            Component c = parent.getComponent(i);
            int cw = (i < weights.length) ? (int) (total * weights[i]) : fixedWidth;
            boolean center = c instanceof JComponent
                    && Boolean.TRUE.equals(((JComponent) c).getClientProperty(CENTER_KEY));
            if (center) {
                Dimension p = c.getPreferredSize();
                int w = Math.min(p.width, cw);
                c.setBounds(x + (cw - w) / 2, in.top + (h - p.height) / 2, w, p.height);
            } else {
                c.setBounds(x, in.top, cw, h);
            }
            x += cw;
        }
    }
}
