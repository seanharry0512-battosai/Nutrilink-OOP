package view;

import model.Donation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Details of one donation + its collection information (volunteer, pickup date, status). */
public class DonationDetailsView extends JPanel {
    private static final Color GRAY = new Color(0x6B6B6B);

    private final JLabel backLabel = Theme.label("\u2190  Back to Donations", 26, Font.BOLD, GRAY);
    private final JPanel imageHolder = new JPanel(new BorderLayout());
    private final JLabel nameLabel = Theme.label("", 28, Font.BOLD, Color.BLACK);
    private final StatusPill statusPill = new StatusPill(Donation.AVAILABLE);

    private final JLabel quantity = value();
    private final JLabel donor = value();
    private final JLabel location = value();
    private final JLabel deadline = value();
    private final JLabel condition = value();
    private final JLabel notes = value();

    private final JLabel volunteer = value();
    private final JLabel pickup = value();
    private final StatusPill collectionPill = new StatusPill(Donation.COL_PENDING);

    private final OutlineButton assignButton = new OutlineButton("Assign Volunteer", Theme.GREEN, Theme.GREEN, Color.WHITE);
    private final JButton updateButton = Theme.primary("Update Status");

    private Donation current;

    public DonationDetailsView() {
        setLayout(new BorderLayout(0, 16));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(22, 28, 22, 28));

        // back link
        backLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backRow.setOpaque(false);
        backRow.add(backLabel);
        add(backRow, BorderLayout.NORTH);

        // top: picture + info
        imageHolder.setOpaque(false);
        imageHolder.setPreferredSize(new Dimension(210, 210));
        JPanel imgWrap = new JPanel(new BorderLayout());
        imgWrap.setOpaque(false);
        imgWrap.add(imageHolder, BorderLayout.NORTH);

        JPanel info = new JPanel(new GridBagLayout());
        info.setOpaque(false);
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        titleRow.setOpaque(false);
        titleRow.add(nameLabel);
        titleRow.add(statusPill);
        GridBagConstraints t = new GridBagConstraints();
        t.gridx = 0; t.gridy = 0; t.gridwidth = 2; t.anchor = GridBagConstraints.WEST;
        t.insets = new Insets(0, 0, 8, 0);
        info.add(titleRow, t);
        addRow(info, 1, "Quantity", quantity);
        addRow(info, 2, "Donor", donor);
        addRow(info, 3, "Location", location);
        addRow(info, 4, "Deadline", deadline);
        addRow(info, 5, "Food Condition", condition);
        addRow(info, 6, "Notes", notes);
        JPanel infoWrap = new JPanel(new BorderLayout());
        infoWrap.setOpaque(false);
        infoWrap.add(info, BorderLayout.NORTH);

        JPanel topRow = new JPanel(new BorderLayout(28, 0));
        topRow.setOpaque(false);
        topRow.add(imgWrap, BorderLayout.WEST);
        topRow.add(infoWrap, BorderLayout.CENTER);

        // collection information
        JLabel collTitle = Theme.label("Collection Information", 24, Font.BOLD, Color.BLACK);
        collTitle.setOpaque(true);
        collTitle.setBackground(new Color(0xF3F4F6));
        collTitle.setBorder(new EmptyBorder(8, 16, 8, 40));
        JPanel collTitleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        collTitleRow.setOpaque(false);
        collTitleRow.add(collTitle);

        JPanel pillWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pillWrap.setOpaque(false);
        pillWrap.add(collectionPill);
        JPanel collRow = new JPanel(new GridLayout(1, 3, 30, 0));
        collRow.setOpaque(false);
        collRow.setBorder(new EmptyBorder(0, 10, 0, 0));
        collRow.add(column("Assigned Volunteer:", volunteer));
        collRow.add(column("Pickup Date:", pickup));
        collRow.add(column("Status:", pillWrap));

        // buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        buttons.setOpaque(false);
        buttons.add(assignButton);
        buttons.add(updateButton);

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        content.add(topRow, Theme.row(0, 0));
        content.add(collTitleRow, Theme.row(1, 26));
        content.add(collRow, Theme.row(2, 14));
        content.add(buttons, Theme.row(3, 28));
        GridBagConstraints filler = Theme.row(4, 0);
        filler.weighty = 1;
        filler.fill = GridBagConstraints.BOTH;
        JPanel space = new JPanel();
        space.setOpaque(false);
        content.add(space, filler);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        add(scroll, BorderLayout.CENTER);
    }

    // ---------------- API for the controller ----------------
    public JLabel getBackLabel() { return backLabel; }
    public JButton getAssignButton() { return assignButton; }
    public JButton getUpdateStatusButton() { return updateButton; }
    public Donation getCurrentDonation() { return current; }

    /** Only coordinators and volunteers may assign volunteers or change the collection status. */
    public void setManagerActions(boolean allowed) {
        assignButton.setVisible(allowed);
        updateButton.setVisible(allowed);
    }

    public void showDonation(Donation d) {
        current = d;
        if (d == null) {
            resetView();
            return;
        }

        imageHolder.removeAll();
        imageHolder.add(new FoodImage(d.getFoodName(), d.getDonationType(), 210), BorderLayout.NORTH);

        nameLabel.setText(d.getFoodName());
        statusPill.setStatus(d.getStatus());
        quantity.setText(d.getQuantityText());
        donor.setText(d.getDonorName());
        location.setText(d.getPickupLocation());
        deadline.setText(d.getDeadlineText());
        condition.setText(d.getCondition());
        String n = d.getNotes() == null ? "" : d.getNotes().trim();
        String formattedNotes = n.isEmpty() ? "-" : "<html><div style='width:420px'>" + esc(n).replace("\n", "<br>") + "</div></html>";
        notes.setText(formattedNotes);

        volunteer.setText(d.getAssignedVolunteerText());
        pickup.setText(d.getPickupText());
        collectionPill.setStatus(d.getCollectionStatus());

        assignButton.setEnabled(!Donation.COL_COMPLETED.equals(d.getCollectionStatus()));
        updateButton.setEnabled(!Donation.DISTRIBUTED.equals(d.getStatus()));

        imageHolder.revalidate();
        imageHolder.repaint();
        revalidate();
        repaint();
    }

    private void resetView() {
        current = null;
        imageHolder.removeAll();
        imageHolder.revalidate();
        imageHolder.repaint();

        nameLabel.setText("");
        statusPill.setStatus(Donation.AVAILABLE);
        quantity.setText("");
        donor.setText("");
        location.setText("");
        deadline.setText("");
        condition.setText("");
        notes.setText("-");
        volunteer.setText("");
        pickup.setText("");
        collectionPill.setStatus(Donation.COL_PENDING);
        assignButton.setEnabled(false);
        updateButton.setEnabled(false);
    }

    // ---------------- helpers ----------------
    private static JLabel value() { return Theme.label("", 16, Font.BOLD, Color.BLACK); }

    private static void addRow(JPanel p, int y, String label, JLabel value) {
        GridBagConstraints l = new GridBagConstraints();
        l.gridx = 0; l.gridy = y; l.anchor = GridBagConstraints.NORTHWEST;
        l.insets = new Insets(5, 0, 5, 36);
        p.add(Theme.label(label, 15, Font.BOLD, Theme.MUTED), l);

        GridBagConstraints v = new GridBagConstraints();
        v.gridx = 1; v.gridy = y; v.weightx = 1; v.fill = GridBagConstraints.HORIZONTAL;
        v.anchor = GridBagConstraints.NORTHWEST;
        v.insets = new Insets(5, 0, 5, 0);
        p.add(value, v);
    }

    private static JPanel column(String title, JComponent value) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 6));
        p.setOpaque(false);
        p.add(Theme.label(title, 15, Font.BOLD, Theme.MUTED));
        p.add(value);
        return p;
    }

    private static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}