package model;

import referencebook.States;

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

}