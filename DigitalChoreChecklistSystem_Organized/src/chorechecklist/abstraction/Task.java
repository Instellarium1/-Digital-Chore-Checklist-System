package chorechecklist.abstraction;

import java.time.LocalDate;

/** Abstract parent class: shared task data is encapsulated in private fields. */
public abstract class Task {
    private final int taskId;
    private String taskName;
    private String description;
    private LocalDate dueDate;
    private String status;
    private String priority;

    public static final String HIGH = "High";
    public static final String MEDIUM = "Medium";
    public static final String LOW = "Low";

    public Task(int taskId, String taskName, String description, LocalDate dueDate, String priority) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.description = description;
        this.dueDate = dueDate;
        this.status = "Pending";
        this.priority = normalizePriority(priority);
    }
    public int getTaskId() { return taskId; }
    public String getTaskName() { return taskName; }
    public String getDescription() { return description; }
    public LocalDate getDueDate() { return dueDate; }
    public String getStatus() { return status; }
    public String getPriority() { return priority; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public void setDescription(String description) { this.description = description; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setPriority(String priority) { this.priority = normalizePriority(priority); }
    public void markAsCompleted() { status = "Completed"; }
    public void markAsPending() { status = "Pending"; }

    /** Sort rank used for ordering: 0 = High (top), 1 = Medium, 2 = Low (bottom). */
    public int getPriorityRank() {
        if (HIGH.equals(priority)) return 0;
        if (MEDIUM.equals(priority)) return 1;
        return 2;
    }

    private static String normalizePriority(String p) {
        if (HIGH.equalsIgnoreCase(p)) return HIGH;
        if (LOW.equalsIgnoreCase(p)) return LOW;
        return MEDIUM;
    }

    /** Each subclass provides its own category-specific details. */
    public abstract String getCategory();
    public abstract String getSpecificDetails();

    public String getDetails() {
        return "ID: " + taskId + "\nTask: " + taskName + "\nDescription: " + description
            + "\nDue date: " + dueDate + "\nStatus: " + status
            + "\nPriority: " + priority + "\nCategory: " + getCategory() + "\n" + getSpecificDetails();
    }
}
