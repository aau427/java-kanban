package model;

import referencebook.States;

public class SubTask extends Task {
    private int parentEpic; //

    public SubTask(String subTaskName, String subTaskDescription, States subTaskState, int parentEpic) {
        super(subTaskName, subTaskDescription, subTaskState);
        this.parentEpic = parentEpic;
    }

    public SubTask(int subTaskId, String subTaskName, String subTaskDescription, States subTaskState, int parentEpic) {
        super(subTaskId, subTaskName, subTaskDescription, subTaskState);
        this.parentEpic = parentEpic;
    }

    public int getParentEpic() {
        return parentEpic;
    }

    @Override
    public String toString() {
        String tmpDescription = "Don't set";
        if (taskDescription != null) {
            tmpDescription = taskDescription;
        }
        return " SubTasks{Id = " + taskId + ", Name='" + taskName + '\''
                + ", Description = '" + tmpDescription + '\'' + ", State='" + taskState.name() + '\''
                + ", Epic = " + parentEpic + "}";
    }
}

