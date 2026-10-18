package view;

import model.Donation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.geom.Path2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Inventory page: total food card, search, and a table of food that is still in stock. */
public class InventoryView extends JPanel {
    private static final String[] COLUMNS = {"Name", "Quantity", "Unit", "Expiry", "Status"};
    private static final double[] WEIGHTS = {0.30, 0.15, 0.15, 0.22, 0.18};
    private static final DateTimeFormatter EXPIRY_FORMAT = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    private static final String READY = "Ready";
    private static final String URGENT = "Urgent";

    private final JLabel totalLabel = Theme.label("0 Items", 28, Font.BOLD, Color.BLACK);
    private final Theme.HintField search = new Theme.HintField("Search inventory...");
    private final JPanel rows = new JPanel();
    private List<Donation> all = new ArrayList<>();

    public InventoryView() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(22, 28, 22, 28));

        // ---- total food card ----
        JPanel text = new JPanel(new GridLayout(2, 1, 0, 0));
        text.setOpaque(false);
        text.add(Theme.label("Total Food Available", 14, Font.PLAIN, Color.BLACK));
        text.add(totalLabel);
        JPanel card = new JPanel(new BorderLayout(18, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new Theme.RoundedBorder(Theme.BORDER, 8), new EmptyBorder(6, 14, 6, 14)));
        JLabel boxIcon = new JLabel(new BoxIcon());
        card.add(boxIcon, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);

        // ---- search ----
        search.setPreferredSize(new Dimension(200, 46));
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        // ---- column header ----
        JPanel header = new JPanel(new ColumnLayout(WEIGHTS)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.LIGHT_GRAY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(10, 16, 10, 16));
        for (int i = 0; i < COLUMNS.length; i++) {
            JLabel l = Theme.label(COLUMNS[i], 17, Font.BOLD, Color.BLACK);
            l.setHorizontalAlignment(i == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);
            header.add(l);
        }

        JPanel north = new JPanel(new GridBagLayout());
        north.setOpaque(false);
        north.add(Theme.label("Inventory", 38, Font.BOLD, Color.BLACK), Theme.row(0, 0));
        north.add(card, Theme.row(1, 8));
        north.add(search, Theme.row(2, 14));
        north.add(header, Theme.row(3, 14));
        add(north, BorderLayout.NORTH);

        // ---- list ----
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setBackground(Color.WHITE);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(rows, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    // ---------------- API for DashboardView ----------------
    public void setItems(List<Donation> items) {
        all = new ArrayList<>(items);
        double total = 0;
        for (Donation d : all) total += d.getRemainingQuantity();
        totalLabel.setText(number(total) + (total == 1 ? " Item" : " Items"));
        applyFilter();
    }

    // ---------------- internals ----------------
    private void applyFilter() {
        String q = search.getText().trim().toLowerCase();
        List<Donation> list = new ArrayList<>();
        for (Donation d : all) {
            if (q.isEmpty() || d.getFoodName().toLowerCase().contains(q)) list.add(d);
        }
        list.sort(Comparator.comparing(Donation::getDeadline).thenComparingInt(Donation::getId));

        rows.removeAll();
        if (list.isEmpty()) {
            JLabel empty = Theme.label("No food in stock.", 16, Font.PLAIN, Theme.MUTED);
            empty.setBorder(new EmptyBorder(24, 16, 0, 0));
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            rows.add(empty);
        }
        for (Donation d : list) rows.add(buildRow(d));
        rows.revalidate();
        rows.repaint();
    }

    private JPanel buildRow(Donation d) {
        LocalDate today = LocalDate.now();
        boolean expired = d.getDeadline().isBefore(today);
        boolean urgent = expired || !d.getDeadline().isAfter(today.plusDays(2));

        JPanel row = new JPanel(new ColumnLayout(WEIGHTS));
        row.setBackground(Color.WHITE);
        row.setBorder(new EmptyBorder(6, 16, 6, 16));
        row.setPreferredSize(new Dimension(100, 70));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel name = new JPanel();
        name.setLayout(new BoxLayout(name, BoxLayout.X_AXIS));
        name.setOpaque(false);
        name.add(new FoodImage(d.getFoodName(), 58));
        name.add(Box.createHorizontalStrut(14));
        name.add(Theme.label(d.getFoodName(), 18, Font.BOLD, Color.BLACK));

        String expiryText = d.getDeadline().format(EXPIRY_FORMAT);
        JLabel expiry = expired
                ? Theme.label("<html><s>" + expiryText + "</s></html>", 16, Font.PLAIN, new Color(0xD32F2F))
                : Theme.label(expiryText, 16, Font.PLAIN, Color.BLACK);

        row.add(name);
        row.add(center(number(d.getRemainingQuantity())));
        row.add(center(d.getUnit()));
        row.add(expiry);
        expiry.setHorizontalAlignment(SwingConstants.CENTER);
        row.add(new FilledPill(urgent ? URGENT : READY));
        return row;
    }

    private static JLabel center(String text) {
        JLabel l = Theme.label(text, 16, Font.PLAIN, Color.BLACK);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    private static String number(double q) {
        if (q == Math.rint(q)) return String.valueOf((long) q);
        return String.format("%.1f", q);
    }

    /** Light-filled pill: green "Ready" or red "Urgent". */
    private static class FilledPill extends JLabel {
        private final boolean urgent;

        FilledPill(String text) {
            super(text, SwingConstants.CENTER);
            this.urgent = text.equals(URGENT);
            setOpaque(false);
            setFont(Theme.font(Font.BOLD, 14));
            setForeground(urgent ? new Color(0xD32F2F) : new Color(0x1E8449));
            setPreferredSize(new Dimension(102, 50));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(urgent ? new Color(0xFFCDD2) : new Color(0xB5F0C3));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
            g2.setColor(urgent ? new Color(0xE59A9A) : new Color(0x7BCB8E));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Green open box drawn with Java2D (no image needed). */
    private static class BoxIcon implements Icon {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            fill(g2, new Color(0x56B07F), 10, 28, 4, 14, 30, 8, 36, 18);     // left flap
            fill(g2, new Color(0x56B07F), 62, 28, 68, 14, 42, 8, 36, 18);    // right flap
            fill(g2, new Color(0x24603D), 10, 28, 36, 18, 62, 28, 36, 38);   // opening
            fill(g2, new Color(0x2F7D4F), 10, 28, 36, 38, 36, 66, 10, 56);   // left face
            fill(g2, new Color(0x3E9B66), 36, 38, 62, 28, 62, 56, 36, 66);   // right face
            g2.dispose();
        }

        private static void fill(Graphics2D g2, Color c, double... p) {
            Path2D path = new Path2D.Double();
            path.moveTo(p[0], p[1]);
            for (int i = 2; i < p.length; i += 2) path.lineTo(p[i], p[i + 1]);
            path.closePath();
            g2.setColor(c);
            g2.fill(path);
        }

        @Override public int getIconWidth() { return 72; }
        @Override public int getIconHeight() { return 70; }
    }

    /** Splits the width by fixed percentages so the header and every row line up. */
    private static class ColumnLayout implements LayoutManager {
        private final double[] weights;

        ColumnLayout(double[] weights) { this.weights = weights; }

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
            int total = parent.getWidth() - in.left - in.right;
            int h = parent.getHeight() - in.top - in.bottom;
            int x = in.left;
            for (int i = 0; i < parent.getComponentCount() && i < weights.length; i++) {
                Component c = parent.getComponent(i);
                int cw = (int) (total * weights[i]);
                if (c instanceof FilledPill) {                                   // centered at its own size
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
}