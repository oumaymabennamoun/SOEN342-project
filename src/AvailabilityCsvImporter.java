import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AvailabilityCsvImporter implements AvailabilityImporter {

    private static final String[] HEADER = {"First Name", "Last Name", "Title", "Specialty", "Date", "Start", "End"};
    private static final int MAX_ERRORS_SHOWN = 10;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("H:mm").withResolverStyle(ResolverStyle.STRICT);

    private record Line(int number, String text) {
    }

    private record Row(int lineNumber, String firstName, String lastName, String title, String specialty,
                       LocalDate date, LocalTime start, LocalTime end) {

        String professionalKey() {
            return keyOf(firstName, lastName);
        }

        String professionalName() {
            return firstName + " " + lastName;
        }

        String timeText() {
            return date + " " + start + "-" + end;
        }

        boolean sameTimeAs(Row other) {
            return date.equals(other.date) && start.equals(other.start) && end.equals(other.end);
        }

        boolean overlaps(Row other) {
            return date.equals(other.date) && start.isBefore(other.end) && other.start.isBefore(end);
        }
    }

    @Override
    public int importFrom(String filePath, MedicalProfessionalCatalog professionalCatalog, SpecialtyCatalog specialtyCatalog) {
        List<Line> lines = readRecords(filePath);
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("The file is empty. It needs a header row.");
        }

        checkHeader(lines.get(0));

        List<Row> rows = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            try {
                rows.add(parseLine(lines.get(i).text(), lines.get(i).number()));
            } catch (IllegalArgumentException e) {
                errors.add(e.getMessage());
            }
        }
        errors.addAll(checkProfessionalsConsistent(rows, professionalCatalog));
        errors.addAll(checkNoOverlaps(rows, professionalCatalog));
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(describe(errors));
        }

        return commit(rows, professionalCatalog, specialtyCatalog);
    }


    private List<Line> readRecords(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("No file path was given.");
        }

        Path path;
        try {
            path = Path.of(filePath.trim());
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("'" + filePath + "' is not a valid file path.");
        }
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("File not found: " + path);
        }

        List<String> physical;
        try {
            physical = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (CharacterCodingException e) {
            throw new IllegalArgumentException("The file is not valid UTF-8.");
        } catch (IOException e) {
            throw new IllegalArgumentException("The file could not be read: " + e.getMessage());
        }

        if (!physical.isEmpty() && physical.get(0).startsWith("\uFEFF")) {
            physical.set(0, physical.get(0).substring(1)); // byte order mark
        }

        List<Line> records = new ArrayList<>();
        StringBuilder pending = null;
        int pendingStart = 0;
        for (int i = 0; i < physical.size(); i++) {
            String text = physical.get(i);
            if (pending == null) {
                if (text.isBlank()) {
                    continue;
                }
                pending = new StringBuilder(text);
                pendingStart = i + 1;
            } else {
                pending.append('\n').append(text);
            }
            if (countQuotes(pending) % 2 == 0) {
                records.add(new Line(pendingStart, pending.toString()));
                pending = null;
            }
        }
        if (pending != null) {
            throw new IllegalArgumentException("Line " + pendingStart + ": a quoted value is never closed.");
        }
        return records;
    }

    private static int countQuotes(CharSequence text) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '"') {
                count++;
            }
        }
        return count;
    }

    private void checkHeader(Line header) {
        List<String> found = splitFields(header.text(), header.number());
        boolean ok = found.size() >= HEADER.length;
        for (int i = 0; ok && i < HEADER.length; i++) {
            ok = found.get(i).trim().equalsIgnoreCase(HEADER[i]);
        }
        if (!ok) {
            throw new IllegalArgumentException("Invalid header. Expected the columns " + String.join(", ", HEADER)
                    + " (in that order) but found: " + String.join(", ", found));
        }
    }


    private Row parseLine(String line, int lineNumber) {
        List<String> fields = splitFields(line, lineNumber);
        if (fields.size() < HEADER.length) {
            throw bad(lineNumber, "expected " + HEADER.length + " columns but found " + fields.size());
        }

        String first = fields.get(0).trim();
        String last = fields.get(1).trim();
        String title = fields.get(2).trim();
        String specialty = fields.get(3).trim();
        if (first.isEmpty() || last.isEmpty() || title.isEmpty() || specialty.isEmpty()) {
            throw bad(lineNumber, "first name, last name, title and specialty must not be blank");
        }

        LocalDate date;
        LocalTime start;
        LocalTime end;
        try {
            date = LocalDate.parse(fields.get(4).trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw bad(lineNumber, "'" + fields.get(4).trim() + "' is not a valid date (use YYYY-MM-DD)");
        }
        try {
            start = LocalTime.parse(fields.get(5).trim(), TIME_FORMAT);
        } catch (DateTimeParseException e) {
            throw bad(lineNumber, "'" + fields.get(5).trim() + "' is not a valid start time (use HH:MM, 24-hour)");
        }
        try {
            end = LocalTime.parse(fields.get(6).trim(), TIME_FORMAT);
        } catch (DateTimeParseException e) {
            throw bad(lineNumber, "'" + fields.get(6).trim() + "' is not a valid end time (use HH:MM, 24-hour)");
        }
        if (!end.isAfter(start)) {
            throw bad(lineNumber, "the end time must be after the start time");
        }

        return new Row(lineNumber, first, last, title, specialty, date, start, end);
    }

    private static List<String> splitFields(String line, int lineNumber) {
        List<String> fields = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        int i = 0;
        int n = line.length();

        while (true) {
            value.setLength(0);
            if (i < n && line.charAt(i) == '"') {
                i++;
                boolean closed = false;
                while (i < n && !closed) {
                    char c = line.charAt(i);
                    if (c != '"') {
                        value.append(c);
                        i++;
                    } else if (i + 1 < n && line.charAt(i + 1) == '"') {
                        value.append('"');
                        i += 2;
                    } else {
                        closed = true;
                        i++;
                    }
                }
                if (!closed) {
                    throw bad(lineNumber, "a quoted value is never closed");
                }
                if (i < n && line.charAt(i) != ',') {
                    throw bad(lineNumber, "unexpected text after a closing quote");
                }
            } else {
                while (i < n && line.charAt(i) != ',') {
                    if (line.charAt(i) == '"') {
                        throw bad(lineNumber, "a quote inside an unquoted value (quote the whole value and write \"\" for a quote)");
                    }
                    value.append(line.charAt(i));
                    i++;
                }
            }
            fields.add(value.toString());

            if (i >= n) {
                return fields;
            }
            i++;
        }
    }


    private List<String> checkProfessionalsConsistent(List<Row> rows, MedicalProfessionalCatalog catalog) {
        List<String> errors = new ArrayList<>();
        Map<String, String[]> expected = new HashMap<>(); 

        for (Row row : rows) {
            String[] known = expected.get(row.professionalKey());
            if (known == null) {
                MedicalProfessional existing = catalog.findByName(row.firstName(), row.lastName());
                if (existing != null) {
                    known = new String[]{existing.getTitle(), existing.getSpecialty().getName()};
                } else {
                    known = new String[]{row.title(), row.specialty()};
                }
                expected.put(row.professionalKey(), known);
            }
            if (!known[0].equalsIgnoreCase(row.title()) || !known[1].equalsIgnoreCase(row.specialty())) {
                errors.add("Line " + row.lineNumber() + ": " + row.professionalName() + " is already known as "
                        + known[0] + " (" + known[1] + "), not " + row.title() + " (" + row.specialty() + ")");
            }
        }
        return errors;
    }

    private List<String> checkNoOverlaps(List<Row> rows, MedicalProfessionalCatalog catalog) {
        List<String> errors = new ArrayList<>();
        Map<String, List<Row>> accepted = new HashMap<>(); 

        for (Row row : rows) {
            List<Row> earlier = accepted.computeIfAbsent(row.professionalKey(), k -> new ArrayList<>());
            MedicalProfessional existing = catalog.findByName(row.firstName(), row.lastName());

            boolean handled = false;
            if (existing != null) {
                for (AvailabilitySlot s : existing.getSlots()) {
                    if (sameTime(s, row)) {
                        handled = true;
                    } else if (overlaps(s, row)) {
                        errors.add("Line " + row.lineNumber() + ": " + row.timeText() + " overlaps an existing slot of "
                                + row.professionalName() + " (" + s.getDate() + " " + s.getStartTime() + "-" + s.getEndTime() + ")");
                        handled = true; 
                    }
                }
            }
            for (Row other : earlier) {
                if (other.sameTimeAs(row)) {
                    handled = true;
                } else if (other.overlaps(row)) {
                    errors.add("Line " + row.lineNumber() + ": " + row.timeText() + " overlaps line " + other.lineNumber()
                            + " (" + other.timeText() + ") for " + row.professionalName());
                    handled = true;
                }
            }
            if (!handled) {
                earlier.add(row);
            }
        }
        return errors;
    }

    private static boolean sameTime(AvailabilitySlot s, Row row) {
        return s.getDate().equals(row.date()) && s.getStartTime().equals(row.start()) && s.getEndTime().equals(row.end());
    }

    private static boolean overlaps(AvailabilitySlot s, Row row) {
        return s.getDate().equals(row.date()) && s.getStartTime().isBefore(row.end()) && row.start().isBefore(s.getEndTime());
    }


    private int commit(List<Row> rows, MedicalProfessionalCatalog professionalCatalog, SpecialtyCatalog specialtyCatalog) {
        int added = 0;
        for (Row row : rows) {
            Specialty specialty = specialtyCatalog.findByName(row.specialty());
            if (specialty == null) {
                specialty = new Specialty(row.specialty());
                specialtyCatalog.add(specialty);
            }

            MedicalProfessional mp = professionalCatalog.findByName(row.firstName(), row.lastName());
            if (mp == null) {
                mp = new MedicalProfessional(row.firstName(), row.lastName(), row.title(), "", "", specialty);
                professionalCatalog.add(mp);
            }

            if (hasSlot(mp, row)) {
                continue; 
            }
            if (mp.addSlot(new AvailabilitySlot(row.date(), row.start(), row.end()))) {
                added++;
            }
        }
        return added;
    }

    private static boolean hasSlot(MedicalProfessional mp, Row row) {
        for (AvailabilitySlot s : mp.getSlots()) {
            if (sameTime(s, row)) {
                return true;
            }
        }
        return false;
    }

    // ==================== Helpers ====================

    private static String describe(List<String> errors) {
        StringBuilder text = new StringBuilder("The file has " + errors.size() + " problem(s):");
        for (int i = 0; i < errors.size() && i < MAX_ERRORS_SHOWN; i++) {
            text.append("\n  - ").append(errors.get(i));
        }
        if (errors.size() > MAX_ERRORS_SHOWN) {
            text.append("\n  ... and ").append(errors.size() - MAX_ERRORS_SHOWN).append(" more");
        }
        return text.toString();
    }

    private static IllegalArgumentException bad(int lineNumber, String reason) {
        return new IllegalArgumentException("Line " + lineNumber + ": " + reason);
    }

    private static String keyOf(String firstName, String lastName) {
        return firstName.trim().toLowerCase(Locale.ROOT) + "|" + lastName.trim().toLowerCase(Locale.ROOT);
    }
}