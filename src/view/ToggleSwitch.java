package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** On/off switch used on the Settings page. */
public class ToggleSwitch extends JComponent {
    private boolean on;
    private Runnable onChange;

    public ToggleSwitch() {
        Dimension d = new Dimension(48, 28);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (!isEnabled()) return;
                on = !on;
                repaint();
                if (onChange != null) onChange.run();
            }
        });
    }

    public boolean isOn() { return on; }

    /** Changes the switch without calling the listener. */
    public void setOn(boolean on) {
        this.on = on;
        repaint();
    }

    public void setOnChange(Runnable onChange) { this.onChange = onChange; }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(on ? Theme.GREEN : new Color(0xC4C9D0));
        g2.fillRoundRect(0, 2, 47, 24, 24, 24);
        g2.setColor(Color.WHITE);
        g2.fillOval(on ? 25 : 3, 4, 20, 20);
        g2.dispose();
    }
}
