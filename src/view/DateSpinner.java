package view;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

/** A JSpinner that shows a date like "October 1, 2026" and works with LocalDate. */
public class DateSpinner extends JSpinner {
    public DateSpinner(LocalDate initial) {
        this(initial, null, null);
    }

    /** min and max may be null (no limit). The initial date is kept inside the limits. */
    public DateSpinner(LocalDate initial, LocalDate min, LocalDate max) {
        super(new SpinnerDateModel(toDate(clamp(initial, min, max)), toDate(min), toDate(max), Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor editor = new JSpinner.DateEditor(this, "MMMM d, yyyy");
        editor.getTextField().setFont(Theme.font(Font.PLAIN, 15));
        setEditor(editor);
        setPreferredSize(new Dimension(200, 42));
    }

    private static LocalDate clamp(LocalDate d, LocalDate min, LocalDate max) {
        if (min != null && d.isBefore(min)) return min;
        if (max != null && d.isAfter(max)) return max;
        return d;
    }

    private static Date toDate(LocalDate d) {
        return d == null ? null : Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public LocalDate getDate() {
        return ((Date) getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public void setDate(LocalDate date) {
        setValue(toDate(date));
    }
}
