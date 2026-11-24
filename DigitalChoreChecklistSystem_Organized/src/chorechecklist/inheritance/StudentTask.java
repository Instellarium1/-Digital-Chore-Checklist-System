package chorechecklist.inheritance;

import chorechecklist.abstraction.Task;

import java.time.LocalDate;

/** Academic task; extends Task and overrides category-specific behavior. */
public class StudentTask extends Task {
    private String subject;
    private String type;

    public StudentTask(int id, String name, String description, LocalDate dueDate, String subject, String type, String priority) {
        super(id, name, description, dueDate, priority);
        this.subject = subject; this.type = type;
    }
    public String getSubject() { return subject; }
    public String getType() { return type; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setType(String type) { this.type = type; }
    @Override public String getCategory() { return "Academic"; }
    @Override public String getSpecificDetails() { return "Subject: " + subject + "\nTask type: " + type; }
}
