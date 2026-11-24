DIGITAL CHORE CHECKLIST SYSTEM
Java Swing GUI | OOP Team Project

ORGANIZED PROJECT STRUCTURE

src/
  chorechecklist/
    Main.java
    abstraction/
      Task.java
    encapsulation/
      TaskManager.java
    inheritance/
      HouseholdTask.java
      StudentTask.java
    polymorphism/
      TaskFormatter.java
    ui/
      DigitalChoreChecklistApp.java

WHERE EACH OOP CONCEPT IS DEMONSTRATED

1. Abstraction - abstraction/Task.java
   Task is an abstract parent class. It also holds the shared priority field. It declares getCategory() and
   getSpecificDetails() as abstract methods that subclasses must implement.

2. Encapsulation - encapsulation/TaskManager.java
   The task array, task count, and next ID are private. The application
   manages them through public methods such as addTask(), deleteTask(),
   findTaskById(), getFilteredTasks(), getTasksSortedByPriority(), and the
   count methods. Task data fields are also private
   in the abstract Task class.

3. Inheritance - inheritance/HouseholdTask.java and StudentTask.java
   Both classes extend Task and reuse its common task fields and methods.

4. Polymorphism - polymorphism/TaskFormatter.java
   TaskFormatter accepts a Task reference. At runtime, the overridden
   getCategory() and getSpecificDetails() methods of HouseholdTask or
   StudentTask are called. The UI uses TaskFormatter to display details.

5. Classes, Objects, and Methods
   The project uses classes and objects throughout. TaskManager and the
   user interface call methods to add, edit, display, complete, and delete tasks.

MAIN FILE

Main.java is the single starting point of the complete application.
It launches the Swing GUI in DigitalChoreChecklistApp.java.

HOW TO RUN ON WINDOWS

1. Extract the ZIP file.
2. Make sure a JDK is installed.
3. Open the extracted DigitalChoreChecklistSystem_Organized folder.
4. Double-click run.bat.

You can also open the folder in VS Code and run run.bat from its terminal.
The batch file compiles every Java file into the out folder, then starts
chorechecklist.Main.

GUI FEATURES

- Add Household and Academic tasks (every task has a Priority: High, Medium or Low)
- View all tasks or filter by category
- Priority filter: show All Priorities, High, Medium or Low (works together with
  the category and search box)
- Priority View (sidebar > VIEWS): High tasks always on top, Medium in the
  middle, Low at the bottom. Red = High, Yellow = Medium, Green = Low.
  Tasks with the same priority are ordered by earliest due date.
- Edit selected tasks
- Toggle Pending / Completed
- Delete tasks with confirmation
- View progress summary
- Validate required fields and YYYY-MM-DD dates

DATA STORAGE

Tasks are stored in memory only. They are cleared when the application closes.
No database or permanent file storage is included.
