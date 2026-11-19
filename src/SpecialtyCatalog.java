import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SpecialtyCatalog {

    private final Map<Integer, Specialty> specialties = new LinkedHashMap<>();

    public void add(Specialty specialty) {
        specialties.put(specialty.getSpecialtyId(), specialty);
    }

    // Returns null if there is no specialty with that id.
    public Specialty getSpecialty(int specialtyId) {
        return specialties.get(specialtyId);
    }

    // Case-insensitive match on name. Returns null if there is none.
    public Specialty findByName(String name) {
        if (name == null) {
            return null;
        }
        String wanted = name.trim();
        for (Specialty s : specialties.values()) {
            if (s.getName().equalsIgnoreCase(wanted)) {
                return s;
            }
        }
        return null;
    }

    public List<Specialty> getAll() {
        return new ArrayList<>(specialties.values());
    }
}