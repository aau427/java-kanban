package model;

import referencebook.States;

public class SubTask extends Task {
    private Epic parentEpic; //

    public SubTask(String subTaskName, String subTaskDescription, States subTaskState, Epic parentEpic) {
        super(subTaskName, subTaskDescription, subTaskState);
        this.parentEpic = parentEpic;
    }

    public SubTask(int subTaskId, String subTaskName, String subTaskDescription, States subTaskState, Epic parentEpic) {
        super(subTaskId, subTaskName, subTaskDescription, subTaskState);
        this.parentEpic = parentEpic;
    }

    public int getParentEpic() {
        return parentEpic.taskId;
    }

    @Override
    public String toString() {
        String tmpDescription = "Don't set";
        if (taskDescription != null) {
            tmpDescription = taskDescription;
        }
        return " SubTask{Id = " + taskId + ", Name='" + taskName + '\''
                + ", Description = '" + tmpDescription + '\'' + ", State='" + taskState.name() + '\''
                + ", Epic = " + parentEpic.getTaskId() + "}";
    }
}

