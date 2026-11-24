package chorechecklist.inheritance;

import chorechecklist.abstraction.Task;

import java.time.LocalDate;

/** Household task; extends Task and overrides category-specific behavior. */
public class HouseholdTask extends Task {
    private String area;

    public HouseholdTask(int id, String name, String description, LocalDate dueDate, String area, String priority) {
        super(id, name, description, dueDate, priority);
        this.area = area;
    }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    @Override public String getCategory() { return "Household"; }
    @Override public String getSpecificDetails() { return "Area: " + area; }
}
