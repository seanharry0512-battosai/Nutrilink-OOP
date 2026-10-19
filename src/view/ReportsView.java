package view;

import model.Distribution;
import model.Report;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Reports page: date range (with quick presets), summary cards, a bar chart of donated / collected /
 * distributed food, a table by food type and the distribution log. The controller fills it with a Report.
 */
public class ReportsView extends JPanel {
    public static final String CUSTOM = "Custom";
    private static final int LOG_LIMIT = 25;
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

    private final JComboBox<String> presetBox = new JComboBox<>();
    private final DateSpinner fromSpinner = new DateSpinner(LocalDate.now().withDayOfMonth(1));
    private final DateSpinner toSpinner = new DateSpinner(LocalDate.now());
    private final JButton generateButton = Theme.primary("Generate Report");
    private final OutlineButton exportButton = new OutlineButton("Export Report", Theme.GREEN, Theme.GREEN, Color.WHITE);
    private final JLabel errorLabel = Theme.label(" ", 13, Font.BOLD, Theme.STATUS_RED);

    private final ReportCard donatedCard = new ReportCard(Theme.STATUS_GREEN, "Donations received");
    private final ReportCard collectedCard = new ReportCard(Theme.STATUS_BLUE, "Collected");
    private final ReportCard distributedCard = new ReportCard(Theme.STATUS_VIOLET, "Distributed");
    private final ReportCard servedCard = new ReportCard(new Color(0x6B7280), "Beneficiaries served");

    private final JLabel rangeLabel = Theme.label(" ", 14, Font.PLAIN, Theme.MUTED);
    private final BarChart chart = new BarChart();
    private final SimpleTable typeTable = new SimpleTable(
            new String[]{"Food Type", "Donated", "Collected", "Distributed"}, new double[]{0.4, 0.2, 0.2, 0.2});
    private final SimpleTable logTable = new SimpleTable(
            new String[]{"Date", "Food", "Beneficiary", "Quantity"}, new double[]{0.2, 0.28, 0.32, 0.2});
    private final JLabel logNote = Theme.label(" ", 13, Font.PLAIN, Theme.MUTED);
    private boolean updating;

    public ReportsView() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(22, 28, 22, 28));

        // ---- title row ----
        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setOpaque(false);
        titles.add(Theme.label("Reports", 34, Font.BOLD, Color.BLACK));
        titles.add(Theme.label("Donated, collected and distributed food for a date range.", 14, Font.PLAIN, Theme.MUTED));
        exportButton.setPreferredSize(new Dimension(175, 42));
        JPanel exportWrap = new JPanel(new GridBagLayout());
        exportWrap.setOpaque(false);
        exportWrap.add(exportButton);
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(titles, BorderLayout.WEST);
        titleRow.add(exportWrap, BorderLayout.EAST);
        add(titleRow, BorderLayout.NORTH);

        // ---- controls ----
        for (String p : Report.PRESETS) presetBox.addItem(p);
        presetBox.addItem(CUSTOM);
        presetBox.setFont(Theme.font(Font.BOLD, 14));
        presetBox.setBackground(Color.WHITE);
        presetBox.setPreferredSize(new Dimension(150, 42));
        for (DateSpinner s : new DateSpinner[]{fromSpinner, toSpinner}) {
            s.setPreferredSize(new Dimension(180, 42));
            s.addChangeListener(e -> {
                if (!updating) markCustom();
            });
        }
        generateButton.setPreferredSize(new Dimension(180, 42));
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controls.setOpaque(false);
        controls.add(presetBox);
        controls.add(fromSpinner);
        controls.add(Theme.label("to", 15, Font.BOLD, Color.BLACK));
        controls.add(toSpinner);
        controls.add(generateButton);

        JPanel cards = new JPanel(new GridLayout(1, 4, 14, 0));
        cards.setOpaque(false);
        cards.add(donatedCard);
        cards.add(collectedCard);
        cards.add(distributedCard);
        cards.add(servedCard);

        JPanel chartHeader = new JPanel(new BorderLayout());
        chartHeader.setOpaque(false);
        chartHeader.add(Theme.label("Donation Activity", 22, Font.BOLD, Color.BLACK), BorderLayout.WEST);
        chartHeader.add(rangeLabel, BorderLayout.EAST);

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        int y = 0;
        content.add(controls, Theme.row(y++, 0));
        content.add(errorLabel, Theme.row(y++, 4));
        content.add(cards, Theme.row(y++, 8));
        content.add(chartHeader, Theme.row(y++, 22));
        content.add(chart, Theme.row(y++, 4));
        content.add(Theme.label("By Food Type", 22, Font.BOLD, Color.BLACK), Theme.row(y++, 22));
        content.add(typeTable, Theme.row(y++, 8));
        content.add(Theme.label("Distribution Log", 22, Font.BOLD, Color.BLACK), Theme.row(y++, 26));
        content.add(logTable, Theme.row(y++, 8));
        content.add(logNote, Theme.row(y++, 6));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(content, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    // ---------------- API for the controller ----------------
    public JComboBox<String> getPresetBox() { return presetBox; }
    public JButton getGenerateButton() { return generateButton; }
    public JButton getExportButton() { return exportButton; }
    public LocalDate getFrom() { return fromSpinner.getDate(); }
    public LocalDate getTo() { return toSpinner.getDate(); }

    /** Sets the preset name (or CUSTOM) and both dates without triggering "Custom". */
    public void setRange(String preset, LocalDate from, LocalDate to) {
        updating = true;
        presetBox.setSelectedItem(preset);
        fromSpinner.setDate(from);
        toSpinner.setDate(to);
        updating = false;
    }

    public void showError(String message) { errorLabel.setText(message == null ? " " : message); }

    public void showReport(Report r) {
        showError(null);
        rangeLabel.setText(r.getFrom().format(DAY) + "  \u2013  " + r.getTo().format(DAY));
        donatedCard.set(String.valueOf(r.getDonated()), "");
        collectedCard.set(String.valueOf(r.getCollected()), "");
        distributedCard.set(String.valueOf(r.getDistributed()), r.getUnitText().isEmpty() ? "" : r.getUnitText() + " given out");
        servedCard.set(String.valueOf(r.getBeneficiariesServed()), "");
        chart.setPeriods(r.getPeriods());

        List<String[]> types = new ArrayList<>();
        for (Report.TypeRow t : r.getTypeRows()) {
            types.add(new String[]{t.type, String.valueOf(t.donated), String.valueOf(t.collected), String.valueOf(t.distributed)});
        }
        typeTable.setRows(types, "No food in this date range.");

        List<String[]> log = new ArrayList<>();
        for (Distribution d : r.getLog()) {
            if (log.size() >= LOG_LIMIT) break;
            log.add(new String[]{d.getDateText(), d.getFoodName(), d.getBeneficiaryName(), d.getQuantityText()});
        }
        logTable.setRows(log, "No distributions in this date range.");
        logNote.setText(r.getLog().size() > LOG_LIMIT
                ? "Showing the latest " + LOG_LIMIT + " of " + r.getLog().size() + " records. Export the report to get all of them."
                : " ");
    }

    private void markCustom() {
        updating = true;
        presetBox.setSelectedItem(CUSTOM);
        updating = false;
    }

    /** White card with a colored strip on the left: big number, caption and a small note. */
    private static class ReportCard extends JPanel {
        private final Color accent;
        private final JLabel value = Theme.label("0", 30, Font.BOLD, Color.BLACK);
        private final JLabel note = Theme.label(" ", 12, Font.PLAIN, Theme.MUTED);

        ReportCard(Color accent, String caption) {
            this.accent = accent;
            setOpaque(false);
            setLayout(new GridLayout(3, 1, 0, 0));
            setBorder(new EmptyBorder(10, 20, 10, 10));
            setPreferredSize(new Dimension(100, 96));
            add(value);
            add(Theme.label(caption, 14, Font.BOLD, Theme.TEXT_GRAY));
            add(note);
        }

        void set(String number, String text) {
            value.setText(number);
            note.setText(text.isEmpty() ? " " : text);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.setColor(new Color(0xC9CED6));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.setColor(accent);
            g2.fillRoundRect(0, 0, 9, getHeight() - 1, 9, 9);
            g2.fillRect(5, 0, 4, getHeight() - 1);
            g2.dispose();
        }
    }
}
