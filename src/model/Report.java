package model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Numbers for the Reports page, worked out from donations and distribution records
 * between two dates. The chart is split into days, weeks or months depending on the range.
 */
public class Report {
    public static final String[] PRESETS = {"This week", "This month", "Last 30 days", "Last month", "Last 3 months"};
    public static final int MAX_DAYS = 366;

    /** One group of bars in the chart. */
    public static class Period {
        public final String label;
        public final LocalDate start;
        public final LocalDate end;
        public int donated, collected, distributed;

        Period(String label, LocalDate start, LocalDate end) {
            this.label = label;
            this.start = start;
            this.end = end;
        }
    }

    /** One row of the "by food type" table. */
    public static class TypeRow {
        public final String type;
        public int donated, collected, distributed;
        TypeRow(String type) { this.type = type; }
    }

    private final LocalDate from;
    private final LocalDate to;
    private int donated, collected, distributed, beneficiariesServed;
    private final List<Period> periods;
    private final Map<String, TypeRow> typeRows = new LinkedHashMap<>();
    private final Map<String, Double> unitTotals = new LinkedHashMap<>();
    private final List<Distribution> log = new ArrayList<>();

    private Report(LocalDate from, LocalDate to) {
        this.from = from;
        this.to = to;
        this.periods = buildPeriods(from, to);
    }

    /** Start and end date for a preset such as "This month". */
    public static LocalDate[] presetRange(String preset, LocalDate today) {
        switch (preset == null ? "" : preset) {
            case "This week":
                return new LocalDate[]{today.with(DayOfWeek.MONDAY), today};
            case "Last 30 days":
                return new LocalDate[]{today.minusDays(29), today};
            case "Last 3 months":
                return new LocalDate[]{today.minusMonths(3).plusDays(1), today};
            case "Last month": {
                LocalDate first = today.minusMonths(1).withDayOfMonth(1);
                return new LocalDate[]{first, first.withDayOfMonth(first.lengthOfMonth())};
            }
            default:
                return new LocalDate[]{today.withDayOfMonth(1), today};
        }
    }

    /** Returns an error message if the range cannot be used, otherwise null. */
    public static String validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) return "Please choose both dates.";
        if (from.isAfter(to)) return "The start date must be on or before the end date.";
        if (ChronoUnit.DAYS.between(from, to) + 1 > MAX_DAYS) return "Please choose a range of one year or less.";
        return null;
    }

    /**
     * @param donations     the donations this user may see
     * @param distributions the distribution records of those donations
     */
    public static Report generate(List<Donation> donations, List<Distribution> distributions,
                                  LocalDate from, LocalDate to) {
        Report r = new Report(from, to);
        Map<Integer, Donation> byId = new HashMap<>();

        for (Donation d : donations) {
            byId.put(d.getId(), d);
            if (inRange(d.getDateAdded(), from, to)) {
                r.donated++;
                r.periodFor(d.getDateAdded()).donated++;
                r.typeRow(d.getDonationType()).donated++;
            }
            if (d.getCollectedDate() != null && inRange(d.getCollectedDate(), from, to)) {
                r.collected++;
                r.periodFor(d.getCollectedDate()).collected++;
                r.typeRow(d.getDonationType()).collected++;
            }
        }

        List<Distribution> sorted = new ArrayList<>(distributions);
        sorted.sort(Comparator.comparing(Distribution::getDate).thenComparingInt(Distribution::getId));
        Set<Integer> countedDonations = new HashSet<>();
        Set<Integer> beneficiaries = new HashSet<>();
        for (Distribution x : sorted) {
            if (!inRange(x.getDate(), from, to)) continue;
            r.log.add(x);
            beneficiaries.add(x.getBeneficiaryId());
            r.unitTotals.merge(x.getUnit(), x.getQuantity(), Double::sum);
            if (countedDonations.add(x.getDonationId())) {          // count each donation once
                r.distributed++;
                r.periodFor(x.getDate()).distributed++;
                Donation d = byId.get(x.getDonationId());
                if (d != null) r.typeRow(d.getDonationType()).distributed++;
            }
        }
        r.beneficiariesServed = beneficiaries.size();
        r.log.sort(Comparator.comparing(Distribution::getDate).thenComparingInt(Distribution::getId).reversed());
        return r;
    }

    // ---------------- results ----------------
    public LocalDate getFrom() { return from; }
    public LocalDate getTo() { return to; }
    public int getDonated() { return donated; }
    public int getCollected() { return collected; }
    public int getDistributed() { return distributed; }
    public int getBeneficiariesServed() { return beneficiariesServed; }
    public List<Period> getPeriods() { return periods; }
    public List<TypeRow> getTypeRows() { return new ArrayList<>(typeRows.values()); }
    public List<Distribution> getLog() { return log; }
    public boolean isEmpty() { return donated + collected + distributed == 0; }

    /** For example "45 packs, 33 kg". */
    public String getUnitText() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Double> e : unitTotals.entrySet()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(formatNumber(e.getValue())).append(' ').append(e.getKey());
        }
        return sb.toString();
    }

    // ---------------- internals ----------------
    private static boolean inRange(LocalDate d, LocalDate from, LocalDate to) {
        return !d.isBefore(from) && !d.isAfter(to);
    }

    private Period periodFor(LocalDate d) {
        for (Period p : periods) {
            if (inRange(d, p.start, p.end)) return p;
        }
        return periods.get(periods.size() - 1);       // cannot happen: d is always inside the range
    }

    private TypeRow typeRow(String type) {
        return typeRows.computeIfAbsent(type, TypeRow::new);
    }

    private static List<Period> buildPeriods(LocalDate from, LocalDate to) {
        List<Period> list = new ArrayList<>();
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        DateTimeFormatter md = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);
        if (days <= 14) {                                              // one group per day
            for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) list.add(new Period(d.format(md), d, d));
        } else if (days <= 120) {                                      // one group per week
            for (LocalDate s = from; !s.isAfter(to); s = s.plusDays(7)) {
                LocalDate e = s.plusDays(6).isAfter(to) ? to : s.plusDays(6);
                String label;
                if (s.equals(e)) label = s.format(md);
                else if (s.getMonth() == e.getMonth()) label = s.format(md) + "\u2013" + e.getDayOfMonth();
                else label = s.format(md) + "\u2013" + e.format(md);
                list.add(new Period(label, s, e));
            }
        } else {                                                       // one group per month
            DateTimeFormatter my = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);
            for (LocalDate s = from; !s.isAfter(to); ) {
                LocalDate end = s.withDayOfMonth(s.lengthOfMonth());
                LocalDate e = end.isAfter(to) ? to : end;
                list.add(new Period(s.format(my), s, e));
                s = e.plusDays(1);
            }
        }
        return list;
    }

    private static String formatNumber(double q) {
        if (q == Math.rint(q)) return String.valueOf((long) q);
        return BigDecimal.valueOf(q).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
}
