package chorechecklist.polymorphism;

import chorechecklist.abstraction.Task;

/**
 * Demonstrates runtime polymorphism: the same Task reference can point to
 * HouseholdTask or StudentTask, and overridden methods supply the details.
 */
public class TaskFormatter {
    public String formatSpecificDetails(Task task) {
        return task.getSpecificDetails();
    }

    public String formatCategory(Task task) {
        return task.getCategory();
    }
}
