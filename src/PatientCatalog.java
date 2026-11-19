import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PatientCatalog {

    private final Map<Integer, Patient> patients = new LinkedHashMap<>();

    public void add(Patient patient) {
        patients.put(patient.getPatientId(), patient);
    }

    // Returns null if there is no patient with that id.
    public Patient getPatient(int patientId) {
        return patients.get(patientId);
    }

    // Case-insensitive match on full name, phone or RAMQ number.
    public List<Patient> search(String query) {
        String q = "";
        List<Patient> result = new ArrayList<>();

        if (query != null) {
            q = query.trim().toLowerCase();
        }
        
        for (Patient p : patients.values()) {
            
            String firstName = p.getFirstName().toLowerCase();
            String lastName = p.getLastName().toLowerCase();
            String fullName = firstName + " " + lastName;
            String ramq = "";
            
            if (p.getRamqNumber() != null) {
                ramq = p.getRamqNumber().toLowerCase();
            }

            if (fullName.contains(q) || p.getPhone().contains(q) || ramq.contains(q)) {
                result.add(p);
            }
        }

        return result;
    }
}
