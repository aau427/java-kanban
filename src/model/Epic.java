package model;

import referencebook.States;
import referencebook.TaskType;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private final List<Integer> childSubTasks = new ArrayList<>();

    public Epic(int id, String epicName, String epicDescription) {
        super(id, epicName, epicDescription, States.NEW);
    }

    public Epic(String epicName, String epicDescription) {
        super(epicName, epicDescription, States.NEW);
    }

    public void setState(States state) {
        this.state = state;
    }

    public Epic getEpicCopy() {
        Epic returnEpic = new Epic(this.getId(), this.getName(), this.getDescription());
        for (Integer id : this.getChildSubTasks()) {
            returnEpic.childSubTasks.add(id);
        }
        returnEpic.setState(this.getState());
        return returnEpic;
    }

    public List<Integer> getChildSubTasks() {
        return childSubTasks;
    }

    @Override
    public String toString() {
        String tmpDescription = "Не указан!";
        if (description != null) {
            tmpDescription = description;
        }
        String epicString = " Epic{" + "Id=" + id + ", Name='" + name + '\'' + ", Description='" + tmpDescription + '\'' + ", State='" + state + '\'';
        if (getChildSubTasks().isEmpty()) {
            epicString = epicString + ", childSubTasks.Size = 0 }";
        } else {
            epicString = epicString + ", childSubTasks{";
            for (Integer subTaskId : getChildSubTasks()) {
                epicString = epicString + " " + subTaskId;
            }
            epicString = epicString + "}";

        }
        return epicString;
    }

    @Override
    public String toStringForSaveToFile() {
        //ID, TYPE, NAME, Status, Description
        return String.format("%d,%s,%s,%s,%s", id, TaskType.EPIC.toString(), name, state.toString(), description);
    }


}