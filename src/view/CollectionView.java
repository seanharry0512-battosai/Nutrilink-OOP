package view;

import model.Donation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Collection Management page: Pending / Ongoing / Completed tabs with a list of donations. */
public class CollectionView extends JPanel {
    private static final String[] TABS = {Donation.COL_PENDING, Donation.COL_ONGOING, Donation.COL_COMPLETED};
    private static final Color GRAY = new Color(0x6B6B6B);

    private final JLabel[] tabLabels = new JLabel[TABS.length];
    private final List<List<Donation>> data = new ArrayList<>();
    private final JPanel list = new JPanel();
    private int current = 0;

    private Consumer<Donation> onView;
    private Consumer<Donation> onAssign;
    private Consumer<Donation> onComplete;

    public CollectionView() {
        for (int i = 0; i < TABS.length; i++) data.add(new ArrayList<>());

        setLayout(new BorderLayout(0, 16));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(22, 28, 22, 28));

        // ---- title + tab bar ----
        JPanel tabBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 40, 12)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xF3F4F6));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
            }
        };
        tabBar.setOpaque(false);
        tabBar.setPreferredSize(new Dimension(640, 56));
        for (int i = 0; i < TABS.length; i++) {
            final int index = i;
            JLabel l = Theme.label(TABS[i], 20, Font.BOLD, GRAY);
            l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            l.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { select(index); }
            });
            tabLabels[i] = l;
            tabBar.add(l);
        }
        JPanel tabWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabWrap.setOpaque(false);
        tabWrap.add(tabBar);

        JPanel north = new JPanel(new GridBagLayout());
        north.setOpaque(false);
        north.add(Theme.label("Collection Management", 32, Font.BOLD, Color.BLACK), Theme.row(0, 0));
        north.add(tabWrap, Theme.row(1, 16));
        add(north, BorderLayout.NORTH);

        // ---- list ----
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(Color.WHITE);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(list, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        styleTabs();
    }

    // ---------------- API for the controller ----------------
    public void setOnView(Consumer<Donation> onView) { this.onView = onView; }
    public void setOnAssign(Consumer<Donation> onAssign) { this.onAssign = onAssign; }
    public void setOnComplete(Consumer<Donation> onComplete) { this.onComplete = onComplete; }

    public void setData(List<Donation> pending, List<Donation> ongoing, List<Donation> completed) {
        data.set(0, new ArrayList<>(pending));
        data.set(1, new ArrayList<>(ongoing));
        data.set(2, new ArrayList<>(completed));
        for (int i = 0; i < TABS.length; i++) tabLabels[i].setText(TABS[i] + " (" + data.get(i).size() + ")");
        rebuild();
    }

    // ---------------- internals ----------------
    private void select(int index) {
        current = index;
        styleTabs();
        rebuild();
    }

    private void styleTabs() {
        for (int i = 0; i < tabLabels.length; i++) {
            boolean selected = (i == current);
            tabLabels[i].setForeground(selected ? Theme.GREEN : GRAY);
            tabLabels[i].setBorder(selected
                    ? BorderFactory.createMatteBorder(0, 0, 3, 0, Theme.GREEN)
                    : new EmptyBorder(0, 0, 3, 0));
        }
    }

    private void rebuild() {
        list.removeAll();
        List<Donation> items = data.get(current);
        if (items.isEmpty()) {
            JLabel empty = Theme.label("Nothing here yet.", 16, Font.PLAIN, Theme.MUTED);
            empty.setBorder(new EmptyBorder(30, 10, 0, 0));
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            list.add(empty);
        }
        for (Donation d : items) list.add(buildRow(d));
        list.revalidate();
        list.repaint();
    }

    private JPanel buildRow(Donation d) {
        JPanel row = new JPanel(new BorderLayout(18, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xEEEEEE)), new EmptyBorder(14, 10, 14, 10)));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));

        // picture
        JPanel imgWrap = new JPanel(new BorderLayout());
        imgWrap.setOpaque(false);
        imgWrap.add(new FoodImage(d.getFoodName(), d.getDonationType(), 84), BorderLayout.NORTH);

        // text
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        JPanel title = new JPanel();
        title.setLayout(new BoxLayout(title, BoxLayout.X_AXIS));
        title.setOpaque(false);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.add(Theme.label(d.getFoodName(), 22, Font.BOLD, Color.BLACK));
        title.add(Box.createHorizontalStrut(10));
        title.add(Theme.label("- " + d.getQuantityText(), 16, Font.BOLD, GRAY));
        info.add(title);
        info.add(line("Donor: " + d.getDonorName()));
        info.add(line("Pickup: " + d.getPickupLocation()));
        info.add(line("Deadline: " + d.getDeadlineText()));
        if (d.hasVolunteer()) {
            info.add(line("Volunteer: " + d.getAssignedVolunteer() + "  |  Pickup date: " + d.getPickupText()));
        }

        // buttons
        JPanel buttons = new JPanel(new GridLayout(0, 1, 0, 10));
        buttons.setOpaque(false);
        OutlineButton view = new OutlineButton("View", new Color(0x9CA3AF), GRAY, new Color(0xF3F4F6));
        view.addActionListener(e -> { if (onView != null) onView.accept(d); });
        buttons.add(view);

        if (current == 0) {
            OutlineButton assign = new OutlineButton("Assign Volunteer", Theme.GREEN, Theme.GREEN, new Color(0x9ED6AA));
            assign.addActionListener(e -> { if (onAssign != null) onAssign.accept(d); });
            buttons.add(assign);
        } else if (current == 1) {
            OutlineButton done = new OutlineButton("Mark Completed", Theme.GREEN, Theme.GREEN, new Color(0x9ED6AA));
            done.addActionListener(e -> { if (onComplete != null) onComplete.accept(d); });
            buttons.add(done);
        }
        JPanel east = new JPanel(new GridBagLayout());   // keeps the buttons vertically centered
        east.setOpaque(false);
        east.add(buttons);

        row.add(imgWrap, BorderLayout.WEST);
        row.add(info, BorderLayout.CENTER);
        row.add(east, BorderLayout.EAST);
        return row;
    }

    private static JLabel line(String text) {
        JLabel l = Theme.label(text, 15, Font.BOLD, GRAY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
}