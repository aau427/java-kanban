package model;

import referencebook.States;

import java.util.Objects;

public class Task {
    protected Integer taskId;
    protected String taskName;
    protected String taskDescription;
    protected States taskState;


    public Task(String taskName, String taskDescription, States taskState) {
        this.taskId = null;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.taskState = taskState;
    }

    public Task(int taskId, String taskName, String taskDescription, States taskState) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.taskState = taskState;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public States getTaskState() {
        return taskState;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public boolean checkTask() {
        if (taskName == null) {
            return false;
        } else return taskId != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(taskId, task.taskId);
    }

    @Override
    public int hashCode() {
        return taskId;
    }

    @Override
    public String toString() {
        String tmpDescription = "Не указали!";
        if (taskDescription != null) {
            tmpDescription = taskDescription;
        }
        return " Tasks.Task{" + "Id=" + taskId + ", Name='" + taskName + '\''
                + ", Description='" + tmpDescription + '\'' + ", State='" + taskState.name() + '\''
                + '}';
    }

    public void setState(States taskState) {
        this.taskState = taskState;
    }


    public States getState() {
        return taskState;
    }
}


