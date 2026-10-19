package view;

import model.Report;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/** Grouped bar chart (Donated / Collected / Distributed) drawn with Java2D. Hover a group to see its numbers. */
public class BarChart extends JComponent {
    private static final String[] NAMES = {"Donated", "Collected", "Distributed"};
    private static final Color[] COLORS = {Theme.STATUS_GREEN, Theme.STATUS_BLUE, Theme.STATUS_VIOLET};
    private static final int LEFT = 46, RIGHT = 14, TOP = 42, BOTTOM = 38;

    private List<Report.Period> periods = new ArrayList<>();
    private int hover = -1;

    public BarChart() {
        setPreferredSize(new Dimension(500, 300));
        setMinimumSize(new Dimension(300, 240));
        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) { setHover(indexAt(e.getX(), e.getY())); }
            @Override public void mouseExited(MouseEvent e) { setHover(-1); }
        };
        addMouseMotionListener(mouse);
        addMouseListener(mouse);
    }

    public void setPeriods(List<Report.Period> periods) {
        this.periods = new ArrayList<>(periods);
        this.hover = -1;
        repaint();
    }

    private void setHover(int index) {
        if (index != hover) {
            hover = index;
            repaint();
        }
    }

    private Rectangle plot() {
        return new Rectangle(LEFT, TOP, Math.max(10, getWidth() - LEFT - RIGHT), Math.max(10, getHeight() - TOP - BOTTOM));
    }

    private int indexAt(int x, int y) {
        Rectangle p = plot();
        if (periods.isEmpty() || x < p.x || x >= p.x + p.width || y < p.y || y > p.y + p.height + BOTTOM) return -1;
        return Math.min(periods.size() - 1, (int) ((x - p.x) / (p.width / (double) periods.size())));
    }

    private static int[] values(Report.Period p) { return new int[]{p.donated, p.collected, p.distributed}; }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawLegend(g2);

        Rectangle plot = plot();
        int max = 0;
        for (Report.Period p : periods) for (int v : values(p)) max = Math.max(max, v);

        if (periods.isEmpty() || max == 0) {                                   // nothing to draw
            g2.setColor(new Color(0xDDDDDD));
            g2.drawLine(plot.x, plot.y + plot.height, plot.x + plot.width, plot.y + plot.height);
            g2.setFont(Theme.font(Font.PLAIN, 15));
            g2.setColor(Theme.MUTED);
            String msg = "No activity in this date range.";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, plot.x + (plot.width - fm.stringWidth(msg)) / 2, plot.y + plot.height / 2);
            g2.dispose();
            return;
        }

        // ---- scale and grid ----
        int step = niceStep(max / 4.0);
        int top = (int) Math.ceil(max / (double) step) * step;
        g2.setFont(Theme.font(Font.PLAIN, 12));
        FontMetrics fm = g2.getFontMetrics();
        for (int v = 0; v <= top; v += step) {
            int y = plot.y + plot.height - (int) Math.round(v / (double) top * plot.height);
            g2.setColor(v == 0 ? new Color(0xBBBBBB) : new Color(0xE9E9E9));
            g2.drawLine(plot.x, y, plot.x + plot.width, y);
            g2.setColor(Theme.TEXT_GRAY);
            String s = String.valueOf(v);
            g2.drawString(s, plot.x - 8 - fm.stringWidth(s), y + fm.getAscent() / 2 - 1);
        }

        // ---- bars ----
        int n = periods.size();
        double groupW = plot.width / (double) n;
        double barW = Math.min(34, groupW * 0.8 / 3 - 2);
        double gap = Math.min(4, barW / 4);
        double total = 3 * barW + 2 * gap;
        int labelStep = 1;
        int widest = 0;
        for (Report.Period p : periods) widest = Math.max(widest, fm.stringWidth(p.label));
        while (widest + 10 > groupW * labelStep) labelStep++;

        for (int i = 0; i < n; i++) {
            double gx = plot.x + i * groupW;
            if (i == hover) {
                g2.setColor(new Color(0xF0F7F2));
                g2.fillRect((int) gx + 1, plot.y, (int) groupW - 1, plot.height);
            }
            double x = gx + (groupW - total) / 2;
            int[] vals = values(periods.get(i));
            for (int j = 0; j < 3; j++) {
                if (vals[j] > 0) {
                    double h = Math.max(3, vals[j] / (double) top * plot.height);
                    double y = plot.y + plot.height - h;
                    g2.setColor(COLORS[j]);
                    g2.fill(new RoundRectangle2D.Double(x, y, barW, h, 6, 6));
                    g2.fillRect((int) Math.round(x), (int) Math.round(y + h / 2), (int) Math.round(barW), (int) Math.round(h / 2));
                    if (barW >= 14) {
                        g2.setFont(Theme.font(Font.BOLD, 11));
                        FontMetrics vf = g2.getFontMetrics();
                        String s = String.valueOf(vals[j]);
                        g2.setColor(new Color(0x333333));
                        g2.drawString(s, (float) (x + (barW - vf.stringWidth(s)) / 2), (float) (y - 4));
                    }
                }
                x += barW + gap;
            }
            if (i % labelStep == 0) {
                g2.setFont(Theme.font(Font.PLAIN, 12));
                g2.setColor(Color.BLACK);
                String label = periods.get(i).label;
                g2.drawString(label, (float) (gx + (groupW - fm.stringWidth(label)) / 2), plot.y + plot.height + 20);
            }
        }

        if (hover >= 0 && hover < n) drawTooltip(g2, plot, groupW);
        g2.dispose();
    }

    private void drawLegend(Graphics2D g2) {
        g2.setFont(Theme.font(Font.PLAIN, 13));
        FontMetrics fm = g2.getFontMetrics();
        int x = LEFT;
        for (int i = 0; i < 3; i++) {
            g2.setColor(COLORS[i]);
            g2.fillRoundRect(x, 12, 14, 14, 4, 4);
            g2.setColor(Color.BLACK);
            g2.drawString(NAMES[i], x + 20, 12 + fm.getAscent() - 1);
            x += 20 + fm.stringWidth(NAMES[i]) + 24;
        }
    }

    private void drawTooltip(Graphics2D g2, Rectangle plot, double groupW) {
        Report.Period p = periods.get(hover);
        int[] v = values(p);
        String[] lines = {p.label, "Donated: " + v[0], "Collected: " + v[1], "Distributed: " + v[2]};
        g2.setFont(Theme.font(Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        int w = 0;
        for (String s : lines) w = Math.max(w, fm.stringWidth(s));
        w += 20;
        int h = lines.length * (fm.getHeight() + 2) + 12;
        int x = (int) (plot.x + (hover + 1) * groupW + 6);
        if (x + w > getWidth() - 4) x = (int) (plot.x + hover * groupW - w - 6);
        x = Math.max(4, x);
        int y = plot.y + 6;
        g2.setColor(new Color(0xE6222222, true));
        g2.fillRoundRect(x, y, w, h, 10, 10);
        g2.setColor(Color.WHITE);
        for (int i = 0; i < lines.length; i++) {
            g2.drawString(lines[i], x + 10, y + 8 + fm.getAscent() + i * (fm.getHeight() + 2));
        }
    }

    /** 1, 2, 5, 10, 20, 50... (never below 1, because the numbers are counts). */
    private static int niceStep(double raw) {
        if (raw <= 1) return 1;
        double magnitude = Math.pow(10, Math.floor(Math.log10(raw)));
        double residual = raw / magnitude;
        double nice = residual <= 1 ? 1 : residual <= 2 ? 2 : residual <= 5 ? 5 : 10;
        return (int) Math.max(1, Math.round(nice * magnitude));
    }
}
