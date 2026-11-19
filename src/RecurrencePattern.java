import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

// How availability repeats inside a date range.
public class RecurrencePattern {

    private final RepeatType type;
    private final Set<DayOfWeek> daysOfWeek; // used when type is WEEKLY
    private final int dayOfMonth;            // used when type is MONTHLY (1-31)

    public RecurrencePattern(RepeatType type, Set<DayOfWeek> daysOfWeek, int dayOfMonth) {
        if (type == null) {
            throw new IllegalArgumentException("A repeat type is required.");
        }
        if (type == RepeatType.WEEKLY && (daysOfWeek == null || daysOfWeek.isEmpty())) {
            throw new IllegalArgumentException("A weekly pattern needs at least one day of the week.");
        }
        if (type == RepeatType.MONTHLY && (dayOfMonth < 1 || dayOfMonth > 31)) {
            throw new IllegalArgumentException("Day of month must be between 1 and 31.");
        }

        Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        if (daysOfWeek != null) {
            days.addAll(daysOfWeek);
        }

        this.type = type;
        this.daysOfWeek = days;
        this.dayOfMonth = dayOfMonth;
    }

    public static RecurrencePattern weekly(Set<DayOfWeek> daysOfWeek) {
        return new RecurrencePattern(RepeatType.WEEKLY, daysOfWeek, 0);
    }

    public static RecurrencePattern monthly(int dayOfMonth) {
        return new RecurrencePattern(RepeatType.MONTHLY, null, dayOfMonth);
    }

    public static RecurrencePattern daily() {
        return new RecurrencePattern(RepeatType.DAILY, null, 0);
    }

    // Every date from startDate to endDate (both included) that fits the pattern, in order.
    public List<LocalDate> generateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("A start date and an end date are required.");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date " + endDate + " is before start date " + startDate + ".");
        }

        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            if (fits(d)) {
                dates.add(d);
            }
        }
        return dates;
    }

    private boolean fits(LocalDate date) {
        switch (type) {
            case WEEKLY:
                return daysOfWeek.contains(date.getDayOfWeek());
            case MONTHLY:
                return date.getDayOfMonth() == dayOfMonth;
            default:
                return true; // DAILY
        }
    }

    public RepeatType getType() {
        return type;
    }

    public Set<DayOfWeek> getDaysOfWeek() {
        return daysOfWeek;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }
}
