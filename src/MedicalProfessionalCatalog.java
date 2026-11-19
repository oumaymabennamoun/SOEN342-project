import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MedicalProfessionalCatalog {

    private final Map<Integer, MedicalProfessional> professionals = new LinkedHashMap<>();

    public void add(MedicalProfessional professional) {
        professionals.put(professional.getProfessionalId(), professional);
    }

    // Returns null if there is no professional with that id.
    public MedicalProfessional getProfessional(int professionalId) {
        return professionals.get(professionalId);
    }

    public List<MedicalProfessional> getAll() {
        return new ArrayList<>(professionals.values());
    }
    
 
    public MedicalProfessional findByName(String first, String last) {
        if (first == null || last == null) {
            return null;
        }
        for (MedicalProfessional mp : professionals.values()) {
            if (mp.getFirstName().equalsIgnoreCase(first.trim()) && mp.getLastName().equalsIgnoreCase(last.trim())) {
                return mp;
            }
        }
        return null;
    }

    public List<MedicalProfessional> findBySpecialty(String specialty) {
        List<MedicalProfessional> result = new ArrayList<>();
        if (specialty == null) {
            return result;
        }
        for (MedicalProfessional mp : professionals.values()) {
            if (mp.getSpecialty().getName().equalsIgnoreCase(specialty.trim())) {
                result.add(mp);
            }
        }
        return result;
    }
    public AvailabilitySlot findSlot(int slotId) {
        for (MedicalProfessional mp : professionals.values()) {
            AvailabilitySlot s = mp.getSlot(slotId);
            if (s != null) {
                return s;
            }
        }
        return null;
    }
}
