public class Specialty {

    private static int nextId = 1;

    private final int specialtyId;
    private final String name;

    public Specialty(String name) {
        this.specialtyId = nextId++;
        this.name = name;
    }

    public int getSpecialtyId() {
        return specialtyId;
    }

    public String getName() {
        return name;
    }
}
