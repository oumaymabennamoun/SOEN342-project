import java.time.DayOfWeek;
import java.util.Set;

// How availability repeats inside a date range.
public class RecurrencePattern {

    private final RepeatType type;
    private final Set<DayOfWeek> daysOfWeek; // used when type is WEEKLY
    private final int dayOfMonth;            // used when type is MONTHLY (1-31)

    public RecurrencePattern(RepeatType type, Set<DayOfWeek> daysOfWeek, int dayOfMonth) {
        this.type = type;
        this.daysOfWeek = daysOfWeek;
        this.dayOfMonth = dayOfMonth;
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
