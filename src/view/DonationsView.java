package view;

import model.Donation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/** Donations page: search, State/Date filters, and a list of donations with a status pill. */
public class DonationsView extends JPanel {
    private static final String[] COLUMNS = {"Food", "Quantity", "Location", "Deadline", "Status"};
    private static final double[] WEIGHTS = {0.30, 0.14, 0.19, 0.19, 0.18};
    private static final int MENU_W = 44;
    private static final Color GRAY = new Color(0x666666);

    private final JButton addButton = Theme.button("+  Add Donation", Theme.ORANGE, Color.WHITE);
    private final Theme.HintField search = new Theme.HintField("Search donations...");
    private final JComboBox<String> stateBox = new JComboBox<>(new String[]{
            "All States", Donation.PENDING, Donation.AVAILABLE, Donation.COLLECTED, Donation.DISTRIBUTED});
    private final JComboBox<String> dateBox = new JComboBox<>(new String[]{
            "Newest first", "Oldest first", "Deadline soonest"});
    private final JPanel rows = new JPanel();

    private List<Donation> all = new ArrayList<>();
    private boolean canApprove;
    private Consumer<Donation> onView;
    private Consumer<Donation> onApprove;

    public DonationsView() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(22, 28, 22, 28));

        // ---- title row ----
        addButton.setFont(Theme.font(Font.BOLD, 17));
        addButton.setPreferredSize(new Dimension(200, 44));
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(Theme.label("Donations", 34, Font.BOLD, Color.BLACK), BorderLayout.WEST);
        titleRow.add(addButton, BorderLayout.EAST);

        // ---- search + filters ----
        search.setPreferredSize(new Dimension(320, 40));
        for (JComboBox<String> box : List.of(stateBox, dateBox)) {
            box.setFont(Theme.font(Font.BOLD, 14));
            box.setBackground(Color.WHITE);
            box.setPreferredSize(new Dimension(160, 38));
        }
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filters.setOpaque(false);
        filters.add(stateBox);
        filters.add(dateBox);
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.add(search, BorderLayout.WEST);
        toolbar.add(filters, BorderLayout.EAST);

        // ---- column header bar ----
        JPanel header = new JPanel(new ColumnLayout(WEIGHTS, MENU_W));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                new Theme.RoundedBorder(Theme.BORDER, 16), new EmptyBorder(6, 12, 6, 12)));
        for (int i = 0; i < COLUMNS.length; i++) {
            JLabel l = Theme.label(COLUMNS[i], 17, Font.BOLD, GRAY);
            l.setHorizontalAlignment(i == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);
            header.add(l);
        }
        header.add(Box.createHorizontalStrut(MENU_W));

        JPanel north = new JPanel(new GridBagLayout());
        north.setOpaque(false);
        north.add(titleRow, Theme.row(0, 0));
        north.add(toolbar, Theme.row(1, 14));
        north.add(header, Theme.row(2, 14));
        add(north, BorderLayout.NORTH);
        add(Theme.listScroll(rows), BorderLayout.CENTER);

        // ---- filter events ----
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });
        stateBox.addActionListener(e -> applyFilters());
        dateBox.addActionListener(e -> applyFilters());
    }

    // ---------------- API for the controller ----------------
    public JButton getAddButton() { return addButton; }
    public void setOnView(Consumer<Donation> onView) { this.onView = onView; }
    public void setOnApprove(Consumer<Donation> onApprove) { this.onApprove = onApprove; }

    /** Which buttons the logged-in user may use. */
    public void setPermissions(boolean canAdd, boolean canApprove) {
        addButton.setVisible(canAdd);
        this.canApprove = canApprove;
    }

    public void setDonations(List<Donation> donations) {
        this.all = new ArrayList<>(donations);
        applyFilters();
    }

    // ---------------- internals ----------------
    private void applyFilters() {
        String q = search.getText().trim().toLowerCase();
        String state = (String) stateBox.getSelectedItem();
        String sort = (String) dateBox.getSelectedItem();

        List<Donation> list = new ArrayList<>();
        for (Donation d : all) {
            if (!"All States".equals(state) && !d.getStatus().equals(state)) continue;
            if (!q.isEmpty() && !(d.getFoodName().toLowerCase().contains(q)
                    || d.getPickupLocation().toLowerCase().contains(q)
                    || d.getDonorName().toLowerCase().contains(q))) continue;
            list.add(d);
        }

        if ("Oldest first".equals(sort)) {
            list.sort(Comparator.comparing(Donation::getDateAdded).thenComparingInt(Donation::getId));
        } else if ("Deadline soonest".equals(sort)) {
            list.sort(Comparator.comparing(Donation::getDeadline).thenComparingInt(Donation::getId));
        } else {
            list.sort(Comparator.comparing(Donation::getDateAdded).thenComparingInt(Donation::getId).reversed());
        }
        rebuild(list);
    }

    private void rebuild(List<Donation> list) {
        rows.removeAll();
        if (list.isEmpty()) {
            JLabel empty = Theme.label("No donations found.", 16, Font.PLAIN, Theme.MUTED);
            empty.setBorder(new EmptyBorder(30, 20, 0, 0));
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            rows.add(empty);
        }
        for (Donation d : list) rows.add(buildRow(d));
        rows.revalidate();
        rows.repaint();
    }

    private JPanel buildRow(Donation d) {
        JPanel row = new JPanel(new ColumnLayout(WEIGHTS, MENU_W));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xEEEEEE)), new EmptyBorder(8, 20, 8, 20)));
        row.setPreferredSize(new Dimension(100, 84));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel food = new JPanel();
        food.setLayout(new BoxLayout(food, BoxLayout.X_AXIS));
        food.setOpaque(false);
        food.add(new FoodImage(d.getFoodName(), d.getDonationType(), 60));
        food.add(Box.createHorizontalStrut(12));
        food.add(Theme.label(d.getFoodName(), 16, Font.BOLD, Color.BLACK));

        // a deadline that has passed is shown in red (unless everything was already given out)
        boolean late = d.isExpired() && !d.getStatus().equals(Donation.DISTRIBUTED);
        JLabel deadline = cellLabel(d.getDeadlineText());
        if (late) deadline.setForeground(Theme.STATUS_RED);

        row.add(food);
        row.add(cellLabel(d.getQuantityText()));
        row.add(cellLabel(d.getPickupLocation()));
        row.add(deadline);
        row.add(ColumnLayout.centered(new StatusPill(d.getStatus())));
        row.add(ColumnLayout.centered(new Kebab(k -> showMenu(d, k))));

        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (onView != null) onView.accept(d); }
        });
        return row;
    }

    private static JLabel cellLabel(String text) {
        JLabel l = Theme.label(text, 15, Font.BOLD, GRAY);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    private void showMenu(Donation d, Component invoker) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem view = new JMenuItem("View Details");
        view.addActionListener(e -> { if (onView != null) onView.accept(d); });
        menu.add(view);
        if (canApprove && d.getStatus().equals(Donation.PENDING)) {
            JMenuItem approve = new JMenuItem("Approve");
            approve.addActionListener(e -> { if (onApprove != null) onApprove.accept(d); });
            menu.add(approve);
        }
        menu.show(invoker, 0, invoker.getHeight());
    }

    /** Three vertical dots (the row menu). */
    private static class Kebab extends JComponent {
        Kebab(Consumer<Kebab> onClick) {
            setPreferredSize(new Dimension(32, 40));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { onClick.accept(Kebab.this); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0x8A8F98));
            int cx = getWidth() / 2, cy = getHeight() / 2;
            for (int i = -1; i <= 1; i++) g2.fillOval(cx - 2, cy + i * 8 - 2, 5, 5);
            g2.dispose();
        }
    }
}
