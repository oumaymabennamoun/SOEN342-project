import java.time.LocalDate;
import java.time.LocalTime;

public class AvailabilitySlot {

    private static int nextId = 1;

    private final int slotId;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private AvailabilityStatus status;

    public AvailabilitySlot(LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.slotId = nextId++;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = AvailabilityStatus.AVAILABLE;
    }

    public int getSlotId() {
        return slotId;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public AvailabilityStatus getStatus() {
        return status;
    }
}
