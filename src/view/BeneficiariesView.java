package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Beneficiaries page: search + a table of registered beneficiaries and how often they received food. */
public class BeneficiariesView extends JPanel {
    private final SimpleTable table = new SimpleTable(
            new String[]{"Name", "Email", "Contact Number", "Times Served", "Last Received"},
            new double[]{0.24, 0.30, 0.18, 0.13, 0.15});
    private final Theme.HintField search = new Theme.HintField("Search beneficiaries...");
    private List<String[]> all = new ArrayList<>();

    public BeneficiariesView() {
        setLayout(new BorderLayout(0, 14));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(22, 28, 22, 28));

        JPanel north = new JPanel(new GridBagLayout());
        north.setOpaque(false);
        north.add(Theme.label("Beneficiaries", 34, Font.BOLD, Color.BLACK), Theme.row(0, 0));
        north.add(Theme.label("Registered beneficiaries and the food they received.", 14, Font.PLAIN, Theme.MUTED), Theme.row(1, 2));
        search.setPreferredSize(new Dimension(320, 40));
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchRow.setOpaque(false);
        searchRow.add(search);
        north.add(searchRow, Theme.row(2, 14));
        add(north, BorderLayout.NORTH);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(table, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
    }

    /** Each row: name, email, contact, times served, last received. */
    public void setRows(List<String[]> rows) {
        all = new ArrayList<>(rows);
        applyFilter();
    }

    private void applyFilter() {
        String q = search.getText().trim().toLowerCase();
        List<String[]> list = new ArrayList<>();
        for (String[] r : all) {
            boolean match = q.isEmpty();
            for (String cell : r) if (cell.toLowerCase().contains(q)) match = true;
            if (match) list.add(r);
        }
        table.setRows(list, "No beneficiaries found.");
    }
}
