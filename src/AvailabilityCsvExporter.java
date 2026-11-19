import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AvailabilityCsvExporter implements AvailabilityExporter {

    private static final String HEADER = "First Name,Last Name,Title,Specialty,Date,Start,End,Status";
    private static final String NEW_LINE = "\r\n";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public int exportTo(String filePath, MedicalProfessionalCatalog professionalCatalog) {
        Path path = checkDestination(filePath);

        StringBuilder csv = new StringBuilder(HEADER).append(NEW_LINE);
        int count = 0;
        for (MedicalProfessional mp : professionalCatalog.getAll()) {
            List<AvailabilitySlot> slots = new ArrayList<>(mp.getSlots());
            slots.sort(Comparator.comparing(AvailabilitySlot::getDate).thenComparing(AvailabilitySlot::getStartTime));
            for (AvailabilitySlot slot : slots) {
                csv.append(toLine(mp, slot)).append(NEW_LINE);
                count++;
            }
        }

        try {
            Files.writeString(path, csv, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("The file could not be written (" + e.getMessage() + ").");
        }
        return count;
    }

    private Path checkDestination(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("No file path was given.");
        }

        Path path;
        try {
            path = Path.of(filePath.trim());
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("'" + filePath + "' is not a valid file path.");
        }
        if (Files.isDirectory(path)) {
            throw new IllegalArgumentException("'" + path + "' is a folder. Give a file name.");
        }
        Path folder = path.toAbsolutePath().getParent();
        if (folder != null && !Files.isDirectory(folder)) {
            throw new IllegalArgumentException("The folder '" + folder + "' does not exist.");
        }
        return path;
    }

    private String toLine(MedicalProfessional mp, AvailabilitySlot slot) {
        return String.join(",",
                quote(mp.getFirstName()),
                quote(mp.getLastName()),
                quote(mp.getTitle()),
                quote(mp.getSpecialty().getName()),
                slot.getDate().toString(),
                slot.getStartTime().format(TIME_FORMAT),
                slot.getEndTime().format(TIME_FORMAT),
                slot.getStatus().name());
    }

    private static String quote(String value) {
        boolean needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n")
                || value.contains("\r") || !value.equals(value.trim());
        if (!needsQuotes) {
            return value;
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}