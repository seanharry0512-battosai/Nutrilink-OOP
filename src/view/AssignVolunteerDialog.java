package view;

import model.Donation;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/** Small popup to pick a volunteer and a pickup date. Returns null if cancelled. */
public final class AssignVolunteerDialog {
    public static final class Result {
        public final String volunteer;
        public final LocalDate pickupDate;

        Result(String volunteer, LocalDate pickupDate) {
            this.volunteer = volunteer;
            this.pickupDate = pickupDate;
        }
    }

    private AssignVolunteerDialog() { }

    public static Result show(Component parent, Donation d, List<String> volunteers) {
        JComboBox<String> box = new JComboBox<>(volunteers.toArray(new String[0]));
        box.setFont(Theme.font(Font.PLAIN, 14));
        if (d.hasVolunteer()) box.setSelectedItem(d.getAssignedVolunteer());

        LocalDate start = (d.getPickupDate() != null) ? d.getPickupDate() : LocalDate.now();
        if (start.isAfter(d.getDeadline())) start = d.getDeadline();
        DateSpinner spinner = new DateSpinner(start);

        JPanel p = new JPanel(new GridLayout(0, 1, 0, 6));
        p.add(Theme.label(d.getFoodName() + " - " + d.getQuantityText(), 16, Font.BOLD, Color.BLACK));
        p.add(Theme.label("Deadline: " + d.getDeadlineText(), 13, Font.PLAIN, Theme.MUTED));
        p.add(Theme.fieldLabel("Volunteer"));
        p.add(box);
        p.add(Theme.fieldLabel("Pickup date"));
        p.add(spinner);

        int choice = JOptionPane.showConfirmDialog(parent, p, "Assign Volunteer",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return null;
        return new Result((String) box.getSelectedItem(), spinner.getDate());
    }
}