package view;

import model.Donation;
import model.Volunteer;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/** Small popup to pick a volunteer and a pickup date. Returns null if cancelled. */
public final class AssignVolunteerDialog {
    public static final class Result {
        public final Volunteer volunteer;
        public final LocalDate pickupDate;

        Result(Volunteer volunteer, LocalDate pickupDate) {
            this.volunteer = volunteer;
            this.pickupDate = pickupDate;
        }
    }

    private AssignVolunteerDialog() { }

    public static Result show(Component parent, Donation d, List<Volunteer> volunteers) {
        JComboBox<Volunteer> box = new JComboBox<>(volunteers.toArray(new Volunteer[0]));
        box.setFont(Theme.font(Font.PLAIN, 14));
        box.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                                    boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof Volunteer) setText(((Volunteer) value).getFullName());
                return this;
            }
        });
        for (Volunteer v : volunteers) {
            if (d.hasVolunteer() && v.getId() == d.getAssignedVolunteerId()) box.setSelectedItem(v);
        }

        // the pickup date must be between today and the deadline
        LocalDate today = LocalDate.now();
        LocalDate latest = d.getDeadline().isBefore(today) ? today : d.getDeadline();
        LocalDate start = (d.getPickupDate() != null) ? d.getPickupDate() : today;
        DateSpinner spinner = new DateSpinner(start, today, latest);

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
        return new Result((Volunteer) box.getSelectedItem(), spinner.getDate());
    }
}
