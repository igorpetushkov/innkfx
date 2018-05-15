package innkfx.utils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {
    public final static ZoneId MINSK_ZONE = ZoneId.of("Europe/Minsk");

    /*** Format dates ***/

    public static String formatToGMT0(long time) {
        return DateUtils.formatToGMT(time, " 0");
    }

    public static String formatToGMT3(long time) {
        return DateUtils.formatToGMT(time, "+3:00");
    }

    public static String formatToGMT(long time, String gmt) {
        TimeZone GMT_TIME_ZONE = TimeZone.getTimeZone("GMT" + gmt);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm");
        simpleDateFormat.setTimeZone(GMT_TIME_ZONE);
        return simpleDateFormat.format(time);
    }

    /*** Periods of days ***/

    public static long getTodayStartTime() {
        LocalDate ld = ZonedDateTime.ofInstant(Instant.now(), MINSK_ZONE).toLocalDate();
        ZonedDateTime zdtStart = ld.atStartOfDay(MINSK_ZONE);

        return new Date(zdtStart.toInstant().toEpochMilli()).getTime();
    }

    public static long getYesterdayStartTime() {
        LocalDate ld = ZonedDateTime.ofInstant(Instant.now(), MINSK_ZONE).toLocalDate();
        ZonedDateTime zdtStart = ld.atStartOfDay(MINSK_ZONE);
        ZonedDateTime yst = zdtStart.minusDays(1);

        return new Date(yst.toInstant().toEpochMilli()).getTime();
    }

    public static long getWeekStartTime() {
        LocalDate ld = ZonedDateTime.ofInstant(Instant.now(), MINSK_ZONE).toLocalDate();
        ZonedDateTime zdtStart = ld.atStartOfDay(MINSK_ZONE);
        ZonedDateTime wst = zdtStart.minusDays(ld.getDayOfWeek().getValue() - 1);

        return new Date(wst.toInstant().toEpochMilli()).getTime();
    }

    public static long getLastWeekStartTime() {
        LocalDate ld = ZonedDateTime.ofInstant(Instant.now(), MINSK_ZONE).toLocalDate();
        ZonedDateTime zdtStart = ld.atStartOfDay(MINSK_ZONE);
        ZonedDateTime wst = zdtStart.minusDays(ld.getDayOfWeek().getValue() - 1 + 7);

        return new Date(wst.toInstant().toEpochMilli()).getTime();
    }

    public static long getMonthStartTime() {
        LocalDate ld = ZonedDateTime.ofInstant(Instant.now(), MINSK_ZONE).toLocalDate();
        ZonedDateTime zdtStart = ld.atStartOfDay(MINSK_ZONE);
        ZonedDateTime mst = zdtStart.minusDays(ld.getDayOfMonth() - 1);

        return new Date(mst.toInstant().toEpochMilli()).getTime();
    }

    /*** Calculate dates ***/

    public static long calcTimestamp(long time, int calendarFieldNumber, int value) {
        Calendar cal = Calendar.getInstance();
        Timestamp timestamp = new Timestamp(time);

        cal.setTimeInMillis(timestamp.getTime());
        cal.add(calendarFieldNumber, value);

        return cal.getTime().getTime();
    }
}
