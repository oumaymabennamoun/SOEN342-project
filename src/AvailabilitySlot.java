import java.time.LocalDate;
import java.time.LocalDateTime;
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

    public boolean isAvailable() {
        return status == AvailabilityStatus.AVAILABLE;
    }

    // True if the slot is free, on the same day as preferredTime, and starts at or after that time.
    public boolean isAvailableOn(LocalDateTime preferredTime) {
        return isAvailable()
                && date.equals(preferredTime.toLocalDate())
                && !startTime.isBefore(preferredTime.toLocalTime());
    }

    public boolean overlaps(AvailabilitySlot other) {
        return date.equals(other.date)
                && startTime.isBefore(other.endTime)
                && other.startTime.isBefore(endTime);
    }

    public void markUnavailable() {
        if (!isAvailable()) {
            throw new IllegalStateException("Slot " + slotId + " is no longer available.");
        }
        status = AvailabilityStatus.BOOKED;
    }

    public void markAvailable() {
        status = AvailabilityStatus.AVAILABLE;
    }

    public LocalDateTime getStart() {
        return date.atTime(startTime);
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
