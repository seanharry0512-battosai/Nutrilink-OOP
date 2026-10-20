package view;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A CardLayout panel that fades from the old page to the new one.
 * Use add(page, "name") like a normal CardLayout, then showPage("name") instead of cards.show(...).
 *
 * How it works: just before switching, a picture of the old page is taken. The new page is shown for real
 * and the picture is drawn on top of it, becoming more transparent until it is gone.
 */
public class PageTransition extends JPanel {
    private static final int DURATION_MS = 220;

    private final CardLayout cards = new CardLayout();
    private String current;                 // name of the page being shown
    private BufferedImage oldPage;          // picture of the previous page while fading
    private float alpha;                    // how visible the old picture still is (1 -> 0)
    private Timer timer;
    private long startTime;

    public PageTransition() {
        setLayout(cards);
        setBackground(Color.WHITE);
    }

    /** Switches to a page. The fade only runs if the panel is on screen and the page really changes. */
    public void showPage(String name) {
        boolean animate = current != null && !name.equals(current) && isShowing() && getWidth() > 0 && getHeight() > 0;
        BufferedImage snapshot = animate ? takeSnapshot() : null;   // taken before switching
        stopTimer();
        oldPage = null;
        cards.show(this, name);
        current = name;
        if (snapshot != null) startFade(snapshot);
    }

    private BufferedImage takeSnapshot() {
        BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        paint(g);       // includes a fade that is still running, so quick clicks look smooth
        g.dispose();
        return img;
    }

    private void startFade(BufferedImage snapshot) {
        oldPage = snapshot;
        alpha = 1f;
        startTime = System.currentTimeMillis();
        timer = new Timer(15, e -> {
            float t = Math.min(1f, (System.currentTimeMillis() - startTime) / (float) DURATION_MS);
            float eased = 1f - (1f - t) * (1f - t) * (1f - t);      // starts fast, ends slow
            alpha = 1f - eased;
            if (t >= 1f) {
                stopTimer();
                oldPage = null;
            }
            repaint();
        });
        timer.start();
        repaint();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    @Override public void paint(Graphics g) {
        super.paint(g);
        if (oldPage != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.SrcOver.derive(Math.max(0f, Math.min(1f, alpha))));
            g2.drawImage(oldPage, 0, 0, null);
            g2.dispose();
        }
    }
}
