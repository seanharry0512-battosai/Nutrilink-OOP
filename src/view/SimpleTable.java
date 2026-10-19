package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/** Plain table drawn with labels: gray header bar + rows. The first column is left-aligned, the rest centered. */
final class SimpleTable extends JPanel {
    private final double[] weights;
    private final JPanel rows = new JPanel();

    SimpleTable(String[] columns, double[] weights) {
        this.weights = weights;
        setLayout(new BorderLayout(0, 4));
        setOpaque(false);

        JPanel header = new JPanel(new ColumnLayout(weights)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.PANEL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(9, 16, 9, 16));
        for (int i = 0; i < columns.length; i++) {
            JLabel l = Theme.label(columns[i], 14, Font.BOLD, Color.BLACK);
            l.setHorizontalAlignment(i == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);
            header.add(l);
        }

        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setOpaque(false);
        add(header, BorderLayout.NORTH);
        add(rows, BorderLayout.CENTER);
    }

    void setRows(List<String[]> data, String emptyText) {
        rows.removeAll();
        if (data.isEmpty()) {
            JLabel empty = Theme.label(emptyText, 14, Font.PLAIN, Theme.MUTED);
            empty.setBorder(new EmptyBorder(14, 16, 6, 0));
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            rows.add(empty);
        }
        for (String[] cells : data) {
            JPanel row = new JPanel(new ColumnLayout(weights));
            row.setOpaque(false);
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xEEEEEE)), new EmptyBorder(0, 16, 0, 16)));
            row.setPreferredSize(new Dimension(100, 42));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            for (int i = 0; i < cells.length; i++) {
                JLabel l = Theme.label(cells[i], 14, i == 0 ? Font.BOLD : Font.PLAIN, Color.BLACK);
                l.setHorizontalAlignment(i == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);
                row.add(l);
            }
            rows.add(row);
        }
        rows.revalidate();
        rows.repaint();
    }
}
