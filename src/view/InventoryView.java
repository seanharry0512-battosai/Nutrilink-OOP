package view;

import model.Donation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.geom.Path2D;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Inventory page: stock summary card, search, and a table of food that is still in stock. */
public class InventoryView extends JPanel {
    private static final String[] COLUMNS = {"Name", "Quantity", "Unit", "Expiry", "Status"};
    private static final double[] WEIGHTS = {0.30, 0.15, 0.15, 0.22, 0.18};
    private static final DateTimeFormatter EXPIRY_FORMAT = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);
    private static final String READY = "Ready";
    private static final String URGENT = "Urgent";
    private static final String EXPIRED = "Expired";

    private final JLabel totalLabel = Theme.label("0 Items", 28, Font.BOLD, Color.BLACK);
    private final JLabel breakdownLabel = Theme.label(" ", 13, Font.PLAIN, Theme.MUTED);
    private final Theme.HintField search = new Theme.HintField("Search inventory...");
    private final JPanel rows = new JPanel();
    private List<Donation> all = new ArrayList<>();

    public InventoryView() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(22, 28, 22, 28));

        // ---- stock summary card (quantities are only added up per unit: kg with kg, packs with packs) ----
        JPanel text = new JPanel(new GridLayout(3, 1, 0, 0));
        text.setOpaque(false);
        text.add(Theme.label("Food in Stock", 14, Font.PLAIN, Color.BLACK));
        text.add(totalLabel);
        text.add(breakdownLabel);
        JPanel card = new JPanel(new BorderLayout(18, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new Theme.RoundedBorder(Theme.BORDER, 8), new EmptyBorder(6, 14, 6, 14)));
        card.add(new JLabel(new BoxIcon()), BorderLayout.WEST);
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
        add(Theme.listScroll(rows), BorderLayout.CENTER);
    }

    // ---------------- API for the controller ----------------
    public void setItems(List<Donation> items) {
        all = new ArrayList<>(items);
        Map<String, Double> perUnit = new LinkedHashMap<>();
        for (Donation d : all) perUnit.merge(d.getUnit(), d.getRemainingQuantity(), Double::sum);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Double> e : perUnit.entrySet()) {
            if (sb.length() > 0) sb.append("  \u00B7  ");
            sb.append(number(e.getValue())).append(' ').append(e.getKey());
        }
        totalLabel.setText(all.size() + (all.size() == 1 ? " Item" : " Items"));
        breakdownLabel.setText(sb.length() == 0 ? " " : sb.toString());
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
        boolean urgent = !expired && !d.getDeadline().isAfter(today.plusDays(2));

        JPanel row = new JPanel(new ColumnLayout(WEIGHTS));
        row.setBackground(Color.WHITE);
        row.setBorder(new EmptyBorder(6, 16, 6, 16));
        row.setPreferredSize(new Dimension(100, 70));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel name = new JPanel();
        name.setLayout(new BoxLayout(name, BoxLayout.X_AXIS));
        name.setOpaque(false);
        name.add(new FoodImage(d.getFoodName(), d.getDonationType(), 58));
        name.add(Box.createHorizontalStrut(14));
        name.add(Theme.label(d.getFoodName(), 18, Font.BOLD, Color.BLACK));

        String expiryText = d.getDeadline().format(EXPIRY_FORMAT);
        JLabel expiry = expired
                ? Theme.label("<html><s>" + expiryText + "</s></html>", 16, Font.PLAIN, Theme.STATUS_RED)
                : Theme.label(expiryText, 16, Font.PLAIN, Color.BLACK);
        expiry.setHorizontalAlignment(SwingConstants.CENTER);

        row.add(name);
        row.add(center(number(d.getRemainingQuantity())));
        row.add(center(d.getUnit()));
        row.add(expiry);
        row.add(ColumnLayout.centered(new FilledPill(expired ? EXPIRED : urgent ? URGENT : READY)));
        return row;
    }

    private static JLabel center(String text) {
        JLabel l = Theme.label(text, 16, Font.PLAIN, Color.BLACK);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    private static String number(double q) {
        if (q == Math.rint(q)) return String.valueOf((long) q);
        return BigDecimal.valueOf(q).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    /** Light-filled pill: green "Ready", orange "Urgent" or red "Expired". */
    private static class FilledPill extends JLabel {
        private final Color fill;
        private final Color line;

        FilledPill(String text) {
            super(text, SwingConstants.CENTER);
            setOpaque(false);
            setFont(Theme.font(Font.BOLD, 14));
            switch (text) {
                case URGENT -> {
                    fill = new Color(0xFFE2B0); line = new Color(0xE8B56A); setForeground(new Color(0xB36B00));
                }
                case EXPIRED -> {
                    fill = new Color(0xFFCDD2); line = new Color(0xE59A9A); setForeground(Theme.STATUS_RED);
                }
                default -> {
                    fill = new Color(0xB5F0C3); line = new Color(0x7BCB8E); setForeground(new Color(0x1E8449));
                }
            }
            setPreferredSize(new Dimension(102, 50));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
            g2.setColor(line);
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
}
