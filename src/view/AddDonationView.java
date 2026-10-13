package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class AddDonationView extends JPanel {
    private final JTextField foodNameField = new Theme.HintField("e.g. Cooked Meals");
    private final JComboBox<String> typeBox = new JComboBox<>(new String[]{
            "Cooked Food", "Bakery / Bread", "Vegetables", "Fruits", "Canned Goods", "Rice / Grains", "Beverages", "Other"});
    private final JTextField quantityField = new Theme.HintField("e.g. 12");
    private final JComboBox<String> unitBox = new JComboBox<>(new String[]{"packs", "kg", "pcs", "liters", "boxes"});
    private final JTextField pickupField = new Theme.HintField("e.g. Quezon City");
    private final JSpinner deadlineSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
    private final JRadioButton freshButton = new JRadioButton("Fresh", true);
    private final JRadioButton safeButton = new JRadioButton("Safe for Consumption");
    private final JTextArea notesArea = new JTextArea(3, 20);
    private final JButton submitButton = Theme.primary("Submit Donation");
    private final JButton cancelButton = Theme.button("Cancel", Theme.LIGHT_GRAY, Color.BLACK);

    public AddDonationView() {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(22, 28, 22, 28));

        for (JComboBox<String> box : java.util.List.of(typeBox, unitBox)) {
            box.setFont(Theme.font(Font.PLAIN, 14));
            box.setBackground(Color.WHITE);
            box.setPreferredSize(new Dimension(200, 40));
        }
        deadlineSpinner.setEditor(new JSpinner.DateEditor(deadlineSpinner, "MMMM d, yyyy"));
        deadlineSpinner.setFont(Theme.font(Font.PLAIN, 14));
        deadlineSpinner.setPreferredSize(new Dimension(240, 40));

        ButtonGroup group = new ButtonGroup();
        JPanel condition = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        condition.setOpaque(false);
        for (JRadioButton rb : new JRadioButton[]{freshButton, safeButton}) {
            rb.setOpaque(false);
            rb.setFont(Theme.font(Font.BOLD, 14));
            group.add(rb);
            condition.add(rb);
        }

        notesArea.setFont(Theme.font(Font.PLAIN, 14));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setBorder(BorderFactory.createCompoundBorder(
                new Theme.RoundedBorder(Theme.BORDER, 10), new EmptyBorder(4, 6, 4, 6)));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(cancelButton);
        buttons.add(submitButton);

        JLabel title = Theme.label("Add Food Donation", 30, Font.BOLD, Color.BLACK);
        JPanel deadlineWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        deadlineWrap.setOpaque(false);
        deadlineWrap.add(deadlineSpinner);

        place(title, 0, 0, 2, 0, 0);
        place(Theme.fieldLabel("Food Name"), 0, 1, 1, 14, 0);
        place(Theme.fieldLabel("Donation Type"), 1, 1, 1, 14, 0);
        place(foodNameField, 0, 2, 1, 4, 0);
        place(typeBox, 1, 2, 1, 4, 0);
        place(Theme.fieldLabel("Quantity"), 0, 3, 1, 10, 0);
        place(Theme.fieldLabel("Unit"), 1, 3, 1, 10, 0);
        place(quantityField, 0, 4, 1, 4, 0);
        place(unitBox, 1, 4, 1, 4, 0);
        place(Theme.fieldLabel("Pickup Location"), 0, 5, 2, 10, 0);
        place(pickupField, 0, 6, 2, 4, 0);
        place(Theme.fieldLabel("Distribution Deadline"), 0, 7, 2, 10, 0);
        place(deadlineWrap, 0, 8, 2, 4, 0);
        place(Theme.fieldLabel("Food Condition"), 0, 9, 2, 10, 0);
        place(condition, 0, 10, 2, 2, 0);
        place(Theme.fieldLabel("Additional Notes"), 0, 11, 2, 8, 0);
        place(notesScroll, 0, 12, 2, 4, 1);
        place(buttons, 0, 13, 2, 12, 0);
    }

    private void place(JComponent c, int x, int y, int w, int top, double weighty) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = x; g.gridy = y; g.gridwidth = w; g.weightx = 1; g.weighty = weighty;
        g.fill = (weighty > 0) ? GridBagConstraints.BOTH : GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(top, x == 1 ? 15 : 0, 0, 0);
        add(c, g);
    }

    public String getFoodName() { return foodNameField.getText(); }
    public String getDonationType() { return (String) typeBox.getSelectedItem(); }
    public String getQuantityText() { return quantityField.getText(); }
    public String getUnit() { return (String) unitBox.getSelectedItem(); }
    public String getPickupLocation() { return pickupField.getText(); }
    public LocalDate getDeadline() {
        return ((Date) deadlineSpinner.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
    public String getCondition() { return safeButton.isSelected() ? "Safe for Consumption" : "Fresh"; }
    public String getNotes() { return notesArea.getText().trim(); }
    public JButton getSubmitButton() { return submitButton; }
    public JButton getCancelButton() { return cancelButton; }

    public void clear() {
        foodNameField.setText("");
        quantityField.setText("");
        pickupField.setText("");
        notesArea.setText("");
        typeBox.setSelectedIndex(0);
        unitBox.setSelectedIndex(0);
        freshButton.setSelected(true);
        deadlineSpinner.setValue(new Date());
    }
}
