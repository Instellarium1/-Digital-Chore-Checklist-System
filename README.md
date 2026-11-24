# Digital-Chore-Checklist-System
**Description:** Development of a Java-based Digital Chore Checklist System for Monitoring Household Members and Student Responsibilities
## Project Description
 Managing household chores and student responsibilities can be challenging, especially when multiple people have their own routines and tasks for the day. Some people usually forget their tasks because of their busy schedules, time management, and poor organization. Methods such as written checklists tend to be more difficult to manage because people sometimes forget what they have already done and what they still need to accomplish.

The proposed system aims to provide convenient and organized daily task management, where users can monitor and familiarize themselves with their progress and responsibilities. It fosters accountability and encourages household members and students to establish better management habits.
 


## Problem being address
The project addresses the difficulties in managing household chores and student responsibilities, particularly when individuals have multiple tasks and busy daily schedules.

The use of traditional written checklists can make task management difficult because users may forget their completed tasks and the responsibilities they still need to accomplish.

The system aims to address these problems by providing a digital checklist for organizing, monitoring, and managing daily responsibilities.


## Project Objectives
### General Objective:
 To develop a Java-based Digital Chore Checklist System for monitoring household members' and students' daily responsibilities.

#### Specific Objectives:

1. Identify common challenges in managing and monitoring daily responsibilities.

2. Determine the necessary features and functionalities of the system.

3. Design and develop a digital system for task assignment, scheduling, monitoring, and reporting.

4. Evaluate the system in terms of functionality, usability, reliability, efficiency, and user satisfaction.

5. Determine differences between household members' and students' assessments of the system.


## Target Users
### Household Members
- **Add household chores.**

- **View their household chores.**

- **Update their assigned chores.**

- **Mark their household chores as completed.**

### Students
- **Use the checklist to remember their daily tasks.** 

- **Organize their daily responsibilities.**

- **Monitor their tasks and activities.**


##  Main Features

| Feature | Description |
|---|---|
| Add Task | Allows the user to enter a chore name, description, and schedule. |
| View Checklist | Displays all saved chores and their current status. |
| Mark as Completed | Allows the user to change a chore's status from pending to completed. |
| Update Task | Allows the user to edit the details of an existing chore. |
| Delete Task | Allows the user to remove a chore that is no longer needed. |
| Task Categorization | Distinguishes household and academic tasks. |
| Task Status Monitoring | Allows users to monitor their tasks and activities. |

## Technologies Used


The Digital Chore Checklist System uses the following technologies:

- **Java Programming Language** – Used to develop the system and implement its functionalities.
- **Object-Oriented Programming (OOP)** – Used to organize the program through classes, objects, methods, and OOP principles.
- **GitHub** – Used as the source code repository and for project collaboration.
  ## 8. Team Members and Roles

| Team Member | Role and Responsibilities |
|---|---|
| Cura, Vhenice Joy D. | Project Leader – Coordinates team activities and monitors project completion. |
| Springael, Lance Aixer N. | Develops the Task class, including its attributes, constructors, and methods. |
| Jacinto, Jan Daiho M. | Develops the TaskManager class and the add, view, update, and delete functionalities. |
| Cruzat, Marjon R. | Develops the main menu, user input, and program flow; helps integrate the classes. |
| Oliva, Raquelyn Mae L. | Tests and debugs the system; prepares documentation, screenshots, and presentation. |

## OOP Concepts Demonstrated

### 1. Abstraction
The `Task` class is an abstract class that defines the common structure and behavior of tasks. It contains abstract methods such as `getCategory()` and `getSpecificDetails()`, which are implemented by its subclasses.

### 2. Encapsulation
The `Task` class uses private attributes to protect task information. Getters and setters provide controlled access to these attributes. The `TaskManager` class also manages the task list and provides methods for adding, finding, filtering, sorting, and deleting tasks.

### 3. Inheritance
The `HouseholdTask` and `StudentTask` classes inherit from the abstract `Task` class. This allows them to share common task properties and methods while providing their own specific details.

### 4. Polymorphism
The `TaskFormatter` class accepts a `Task` reference and calls methods such as `getCategory()` and `getSpecificDetails()`. These methods can produce different results depending on whether the actual object is a `HouseholdTask` or a `StudentTask`.

## 10. Instructions for Running

### Requirements
- Java Development Kit (JDK) installed
- Java compiler (`javac`) available in the system PATH
- Windows operating system (to use `run.bat`)

### Steps
1. Download or clone this repository.
2. Open the project folder.
3. Double-click `run.bat`, or run it from Command Prompt.
4. The batch file will compile the Java source files and launch the application.
5. If compilation fails, check that the JDK is installed and that the source files are correctly organized.
