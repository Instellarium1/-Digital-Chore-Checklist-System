package chorechecklist.encapsulation;

import chorechecklist.abstraction.Task;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Application service. Uses a fixed Task[] array (maximum 100 tasks). */
public class TaskManager {
    private final Task[] taskList = new Task[100];
    private int taskCount = 0;
    private int nextId = 1;

    public int getNextId() { return nextId++; }
    public boolean addTask(Task task) {
        if (taskCount >= taskList.length) return false;
        taskList[taskCount++] = task;
        return true;
    }
    public List<Task> getAllTasks() {
        List<Task> result = new ArrayList<Task>();
        for (int i=0; i<taskCount; i++) result.add(taskList[i]);
        return result;
    }
    public List<Task> getTasksByCategory(String category) {
        List<Task> result = new ArrayList<Task>();
        for (int i=0; i<taskCount; i++)
            if ("All".equals(category) || taskList[i].getCategory().equals(category)) result.add(taskList[i]);
        return result;
    }
    /** Filter by category ("All" or a category name) and priority ("All", "High", "Medium", "Low"). */
    public List<Task> getFilteredTasks(String category, String priority) {
        List<Task> result = new ArrayList<Task>();
        for (int i=0; i<taskCount; i++) {
            Task t = taskList[i];
            boolean categoryOk = "All".equals(category) || t.getCategory().equals(category);
            boolean priorityOk = "All".equals(priority) || t.getPriority().equals(priority);
            if (categoryOk && priorityOk) result.add(t);
        }
        return result;
    }
    /** Same filters, ordered High first, then Medium, then Low (ties: earlier due date first). */
    public List<Task> getTasksSortedByPriority(String category, String priority) {
        List<Task> result = getFilteredTasks(category, priority);
        result.sort(Comparator.comparingInt(Task::getPriorityRank)
            .thenComparing(Task::getDueDate)
            .thenComparingInt(Task::getTaskId));
        return result;
    }
    public Task findTaskById(int id) {
        for (int i=0; i<taskCount; i++) if (taskList[i].getTaskId()==id) return taskList[i];
        return null;
    }
    public boolean deleteTask(int id) {
        for (int i=0; i<taskCount; i++) if (taskList[i].getTaskId()==id) {
            for (int j=i; j<taskCount-1; j++) taskList[j]=taskList[j+1];
            taskList[--taskCount]=null; return true;
        }
        return false;
    }
    public int getTaskCount() { return taskCount; }
    public int getCompletedCount() {
        int count=0;
        for (int i=0; i<taskCount; i++) if ("Completed".equals(taskList[i].getStatus())) count++;
        return count;
    }
    public int getPendingCount() { return taskCount-getCompletedCount(); }
    public double getCompletionRate() {
        return taskCount==0 ? 0.0 : getCompletedCount()*100.0/taskCount;
    }
}
