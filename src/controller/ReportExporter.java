package controller;

import model.Distribution;
import model.Report;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Turns a Report into text for a file: CSV (opens in Excel) or HTML (opens in a browser, can be printed to PDF). */
final class ReportExporter {
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

    private ReportExporter() { }

    static String toCsv(Report r, String generatedBy) {
        StringBuilder sb = new StringBuilder("\uFEFF");          // byte-order mark so Excel reads UTF-8 correctly
        line(sb, "NutriLink Report");
        line(sb, "Date range", r.getFrom().format(DAY) + " to " + r.getTo().format(DAY));
        line(sb, "Generated", LocalDate.now().format(DAY) + " by " + generatedBy);
        sb.append('\n');
        line(sb, "Summary");
        line(sb, "Donations received", String.valueOf(r.getDonated()));
        line(sb, "Collected", String.valueOf(r.getCollected()));
        line(sb, "Distributed (donations)", String.valueOf(r.getDistributed()));
        line(sb, "Beneficiaries served", String.valueOf(r.getBeneficiariesServed()));
        line(sb, "Food given out", r.getUnitText());
        sb.append('\n');
        line(sb, "Activity by period");
        line(sb, "Period", "Donated", "Collected", "Distributed");
        for (Report.Period p : r.getPeriods()) {
            line(sb, p.label, String.valueOf(p.donated), String.valueOf(p.collected), String.valueOf(p.distributed));
        }
        sb.append('\n');
        line(sb, "By food type");
        line(sb, "Food type", "Donated", "Collected", "Distributed");
        for (Report.TypeRow t : r.getTypeRows()) {
            line(sb, t.type, String.valueOf(t.donated), String.valueOf(t.collected), String.valueOf(t.distributed));
        }
        sb.append('\n');
        line(sb, "Distribution log");
        line(sb, "Date", "Food", "Beneficiary", "Quantity");
        for (Distribution d : r.getLog()) line(sb, d.getDateText(), d.getFoodName(), d.getBeneficiaryName(), d.getQuantityText());
        return sb.toString();
    }

    private static void line(StringBuilder sb, String... cells) {
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) sb.append(',');
            String c = cells[i] == null ? "" : cells[i];
            if (c.contains(",") || c.contains("\"") || c.contains("\n")) c = "\"" + c.replace("\"", "\"\"") + "\"";
            sb.append(c);
        }
        sb.append('\n');
    }

    static String toHtml(Report r, String generatedBy) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>NutriLink Report</title><style>")
          .append("body{font-family:Segoe UI,Arial,sans-serif;margin:32px;color:#222}")
          .append("h1{color:#1E8449;margin-bottom:0}h2{margin-top:30px}.muted{color:#777}")
          .append(".cards{display:flex;gap:14px;margin-top:18px}")
          .append(".card{flex:1;border:1px solid #c9ced6;border-radius:10px;padding:12px 16px}")
          .append(".card b{display:block;font-size:30px}")
          .append("table{border-collapse:collapse;width:100%}th{background:#f3f4f6;text-align:left}")
          .append("th,td{padding:8px 12px;border-bottom:1px solid #eee}")
          .append(".bar{display:inline-block;height:10px;border-radius:3px;margin-right:6px}")
          .append("@media print{body{margin:12px}}</style></head><body>");
        sb.append("<h1>NutriLink Report</h1><div class=\"muted\">")
          .append(esc(r.getFrom().format(DAY))).append(" to ").append(esc(r.getTo().format(DAY)))
          .append(" &middot; generated ").append(esc(LocalDate.now().format(DAY)))
          .append(" by ").append(esc(generatedBy)).append("</div>");
        sb.append("<div class=\"cards\">")
          .append(card("Donations received", r.getDonated()))
          .append(card("Collected", r.getCollected()))
          .append(card("Distributed", r.getDistributed()))
          .append(card("Beneficiaries served", r.getBeneficiariesServed()))
          .append("</div>");
        if (!r.getUnitText().isEmpty()) sb.append("<p class=\"muted\">Food given out: ").append(esc(r.getUnitText())).append("</p>");

        int max = 1;
        for (Report.Period p : r.getPeriods()) max = Math.max(max, Math.max(p.donated, Math.max(p.collected, p.distributed)));
        sb.append("<h2>Activity by period</h2><table><tr><th>Period</th><th>Donated</th><th>Collected</th><th>Distributed</th></tr>");
        for (Report.Period p : r.getPeriods()) {
            sb.append("<tr><td>").append(esc(p.label)).append("</td>")
              .append(barCell(p.donated, max, "#1E8449"))
              .append(barCell(p.collected, max, "#2B7BBF"))
              .append(barCell(p.distributed, max, "#7B3FBF")).append("</tr>");
        }
        sb.append("</table><h2>By food type</h2><table><tr><th>Food type</th><th>Donated</th><th>Collected</th><th>Distributed</th></tr>");
        for (Report.TypeRow t : r.getTypeRows()) {
            sb.append("<tr><td>").append(esc(t.type)).append("</td><td>").append(t.donated).append("</td><td>")
              .append(t.collected).append("</td><td>").append(t.distributed).append("</td></tr>");
        }
        sb.append("</table><h2>Distribution log</h2><table><tr><th>Date</th><th>Food</th><th>Beneficiary</th><th>Quantity</th></tr>");
        for (Distribution d : r.getLog()) {
            sb.append("<tr><td>").append(esc(d.getDateText())).append("</td><td>").append(esc(d.getFoodName()))
              .append("</td><td>").append(esc(d.getBeneficiaryName())).append("</td><td>")
              .append(esc(d.getQuantityText())).append("</td></tr>");
        }
        sb.append("</table></body></html>");
        return sb.toString();
    }

    private static String card(String caption, int value) {
        return "<div class=\"card\"><b>" + value + "</b>" + esc(caption) + "</div>";
    }

    private static String barCell(int value, int max, String color) {
        int width = (int) Math.round(value * 120.0 / max);
        return "<td><span class=\"bar\" style=\"width:" + width + "px;background:" + color + "\"></span>" + value + "</td>";
    }

    private static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
