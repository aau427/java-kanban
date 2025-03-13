package model;

import referencebook.States;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private final List<SubTask> childSubTasks = new ArrayList<>();

    public Epic(String epicName, String epicDescription) {
        super(epicName, epicDescription, States.NEW);
    }

    public Epic(int epicId, String epicName, String epicDescription, States epicState) {
        super(epicId, epicName, epicDescription, epicState);
    }

    public List<SubTask> getChildSubTasks() {
        return childSubTasks;
    }

    public void removeSubtaskFromEpic(SubTask subTask) {
        childSubTasks.remove(subTask);
    }

    @Override
    public String toString() {
        String tmpDescription = "Don't set";
        if (taskDescription != null) {
            tmpDescription = taskDescription;
        }
        String epicString = " Epic{" + "Id=" + taskId + ", Name='" + taskName + '\'' + ", Description='" + tmpDescription + '\'' + ", State='" + taskState.name() + '\'';
        if (getChildSubTasks().size() == 0) {
            epicString = epicString + ", childSubTasks.Size = 0 }";
        } else {
            epicString = epicString + ", SubTaskList[";
            for (SubTask subTask : getChildSubTasks()) {
                epicString = epicString + " " + subTask.toString();
            }
            epicString = epicString + "]";

        }
        return epicString;
    }

}