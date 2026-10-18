package view;

import model.Donation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Distribution page: available food on the left, "Record Distribution" form on the right. */
public class DistributionView extends JPanel {
    private final DefaultTableModel model = new DefaultTableModel(new String[]{"Food", "Quantity", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private List<Donation> shown = new ArrayList<>();

    private final JComboBox<String> beneficiaryBox = new JComboBox<>();
    private final Theme.HintField quantityField = new Theme.HintField("Enter quantity");
    private final JTextField unitField = new JTextField();
    private final DateSpinner dateSpinner = new DateSpinner(LocalDate.now());
    private final JLabel selectedInfo = Theme.label("Select a food item from the list.", 13, Font.PLAIN, Theme.MUTED);
    private final JButton recordButton = Theme.primary("Record Distribution");

    public DistributionView() {
        setLayout(new BorderLayout(0, 18));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(22, 28, 22, 28));
        add(Theme.label("Distribution Management", 34, Font.BOLD, Color.BLACK), BorderLayout.NORTH);

        configureTable();

        // ---- left: available food card ----
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new Theme.RoundedBorder(new Color(0x9AA3AE), 14), new EmptyBorder(10, 16, 12, 16)));
        card.add(Theme.label("Available Food", 26, Font.BOLD, Color.BLACK), BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(card, BorderLayout.NORTH);

        // ---- right: form ----
        beneficiaryBox.setFont(Theme.font(Font.PLAIN, 15));
        beneficiaryBox.setBackground(Color.WHITE);
        beneficiaryBox.setPreferredSize(new Dimension(200, 42));
        Theme.styleField(unitField);
        unitField.setEditable(false);
        unitField.setBackground(new Color(0xF5F5F5));
        unitField.setPreferredSize(new Dimension(110, 40));
        recordButton.setFont(Theme.font(Font.BOLD, 16));
        recordButton.setPreferredSize(new Dimension(200, 46));

        JPanel qtyRow = new JPanel(new BorderLayout(10, 0));
        qtyRow.setOpaque(false);
        qtyRow.add(quantityField, BorderLayout.CENTER);
        qtyRow.add(unitField, BorderLayout.EAST);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.add(Theme.label("Record Distribution", 26, Font.BOLD, Color.BLACK), Theme.row(0, 0));
        form.add(Theme.fieldLabel("Select Beneficiary"), Theme.row(1, 18));
        form.add(beneficiaryBox, Theme.row(2, 6));
        form.add(Theme.fieldLabel("Quantity to distribute"), Theme.row(3, 16));
        form.add(qtyRow, Theme.row(4, 6));
        form.add(Theme.fieldLabel("Date"), Theme.row(5, 16));
        form.add(dateSpinner, Theme.row(6, 6));
        form.add(selectedInfo, Theme.row(7, 14));
        form.add(recordButton, Theme.row(8, 8));
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.add(form, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 36, 0));
        center.setOpaque(false);
        center.add(left);
        center.add(right);
        add(center, BorderLayout.CENTER);
    }

    // ---------------- API for the controller ----------------
    public JButton getRecordButton() { return recordButton; }
    public String getQuantityText() { return quantityField.getText().trim(); }
    public LocalDate getDate() { return dateSpinner.getDate(); }
    public String getBeneficiaryName() { return (String) beneficiaryBox.getSelectedItem(); }

    public Donation getSelectedDonation() {
        int r = table.getSelectedRow();
        return (r >= 0 && r < shown.size()) ? shown.get(r) : null;
    }

    public void setAvailableFood(List<Donation> donations) {
        Donation keep = getSelectedDonation();
        shown = new ArrayList<>(donations);
        model.setRowCount(0);
        for (Donation d : shown) model.addRow(new Object[]{d.getFoodName(), d.getRemainingText(), "Ready"});
        if (keep != null) {
            int idx = shown.indexOf(keep);
            if (idx >= 0) table.setRowSelectionInterval(idx, idx);
        }
        updateSelectionInfo();
    }

    public void setBeneficiaries(List<String> names) {
        String keep = (String) beneficiaryBox.getSelectedItem();
        beneficiaryBox.removeAllItems();
        for (String n : names) beneficiaryBox.addItem(n);
        if (keep != null) beneficiaryBox.setSelectedItem(keep);
    }

    public void clearForm() {
        quantityField.setText("");
        dateSpinner.setDate(LocalDate.now());
    }

    // ---------------- internals ----------------
    private void updateSelectionInfo() {
        Donation d = getSelectedDonation();
        if (d == null) {
            unitField.setText("");
            selectedInfo.setText("Select a food item from the list.");
        } else {
            unitField.setText(d.getUnit());
            selectedInfo.setText("Selected: " + d.getFoodName() + " (" + d.getRemainingText() + " left)");
        }
    }

    private void configureTable() {
        table.setRowHeight(56);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(Theme.font(Font.PLAIN, 15));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(0xE8F5EC));
        table.setSelectionForeground(Color.BLACK);
        table.setPreferredScrollableViewportSize(new Dimension(400, 230));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 44));
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                setBackground(new Color(0xF3F4F6));
                setForeground(Color.BLACK);
                setFont(Theme.font(Font.BOLD, 16));
                setBorder(new EmptyBorder(0, 10, 0, 10));
                setHorizontalAlignment(c == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);
                return this;
            }
        });
        table.getColumnModel().getColumn(0).setCellRenderer(textRenderer(Font.BOLD, SwingConstants.LEFT));
        table.getColumnModel().getColumn(1).setCellRenderer(textRenderer(Font.PLAIN, SwingConstants.CENTER));
        table.getColumnModel().getColumn(2).setCellRenderer(new PillCell());
        table.getColumnModel().getColumn(0).setPreferredWidth(160);
        table.getColumnModel().getColumn(1).setPreferredWidth(110);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) updateSelectionInfo();
        });
    }

    private static DefaultTableCellRenderer textRenderer(int style, int align) {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                setFont(Theme.font(style, 15));
                setHorizontalAlignment(align);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return this;
            }
        };
    }

    /** Draws the green "Ready" pill inside a table cell. */
    private static class PillCell extends JPanel implements TableCellRenderer {
        private String text = "";

        PillCell() { setOpaque(true); }

        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
            text = String.valueOf(v);
            setBackground(sel ? t.getSelectionBackground() : Color.WHITE);
            return this;
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = Math.min(getWidth() - 16, 96), h = 34;
            int x = (getWidth() - w) / 2, y = (getHeight() - h) / 2;
            g2.setColor(new Color(0xB5F0C3));
            g2.fillRoundRect(x, y, w, h, 16, 16);
            g2.setColor(new Color(0x7BCB8E));
            g2.drawRoundRect(x, y, w - 1, h - 1, 16, 16);
            g2.setFont(Theme.font(Font.BOLD, 13));
            g2.setColor(new Color(0x1E8449));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, x + (w - fm.stringWidth(text)) / 2, y + (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}