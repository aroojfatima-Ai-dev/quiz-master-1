package app.quizmaster.service;

import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Thymeleaf helper: ${@fmt.date(instant)} */
@Component("fmt")
public class Fmt {
    private static final ZoneId ZONE = ZoneId.of(System.getenv().getOrDefault("APP_TZ", "Asia/Karachi"));
    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("d MMM yyyy").withZone(ZONE);
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm").withZone(ZONE);
    public String date(Instant i) { return i == null ? "—" : D.format(i); }
    public String dateTime(Instant i) { return i == null ? "—" : DT.format(i); }
    public String pct(double d) { return String.format("%.0f%%", d); }
    public String pct1(Double d) { return d == null ? "—" : String.format("%.1f%%", d); }
}
