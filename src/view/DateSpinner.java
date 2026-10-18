package view;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/** A JSpinner that shows a date like "October 1, 2026" and works with LocalDate. */
public class DateSpinner extends JSpinner {
    public DateSpinner(LocalDate initial) {
        super(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(this, "MMMM d, yyyy");
        editor.getTextField().setFont(Theme.font(Font.PLAIN, 15));
        setEditor(editor);
        setPreferredSize(new Dimension(200, 42));
        setDate(initial);
    }

    public LocalDate getDate() {
        return ((Date) getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public void setDate(LocalDate date) {
        setValue(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }
}