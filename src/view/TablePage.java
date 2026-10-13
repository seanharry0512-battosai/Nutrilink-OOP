package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

/** Reusable page: title + table + action buttons. Used for Donations, Inventory, Collection... */
public class TablePage extends JPanel {
    private final DefaultTableModel model;
    private final JTable table;
    private final JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

    public TablePage(String title, String subtitle, String[] columns) {
        setLayout(new BorderLayout(0, 14));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(22, 28, 22, 28));

        JPanel head = new JPanel(new GridLayout(2, 1));
        head.setOpaque(false);
        head.add(Theme.label(title, 30, Font.BOLD, Color.BLACK));
        head.add(Theme.label(subtitle, 14, Font.PLAIN, Theme.MUTED));

        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(32);
        table.setFont(Theme.font(Font.PLAIN, 14));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(0xDDDDDD));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                setBackground(Theme.GREEN);
                setForeground(Color.WHITE);
                setFont(Theme.font(Font.BOLD, 13));
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
        int statusCol = Arrays.asList(columns).indexOf("Status");
        if (statusCol >= 0) table.getColumnModel().getColumn(statusCol).setCellRenderer(new StatusRenderer());

        actions.setOpaque(false);
        add(head, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
    }

    public JButton addAction(String text, Color bg) {
        JButton b = Theme.button(text, bg, Color.WHITE);
        actions.add(b);
        return b;
    }

    public void setRows(List<Object[]> rows) {
        model.setRowCount(0);
        for (Object[] r : rows) model.addRow(r);
    }

    /** Returns the ID (first column) of the selected row, or -1 if nothing is selected. */
    public int getSelectedId() {
        int r = table.getSelectedRow();
        return (r < 0) ? -1 : Integer.parseInt(model.getValueAt(r, 0).toString());
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, f, r, c);
            setHorizontalAlignment(CENTER);
            setFont(Theme.font(Font.BOLD, 13));
            if (!sel) {
                String s = String.valueOf(v);
                setBackground(Theme.statusBg(s));
                setForeground(Theme.statusFg(s));
            }
            return this;
        }
    }
}
