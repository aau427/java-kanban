package model;

import referencebook.States;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private final List<Integer> childSubTasks = new ArrayList<>();

    public Epic(String epicName, String epicDescription) {
        super(epicName, epicDescription, States.NEW);
    }

    public List<Integer> getChildSubTasks() {
        return childSubTasks;
    }

    public void removeSubtaskFromEpic(int subTaskId) {
        //нужно удалить по Object (IdSubTask), а не по index
        childSubTasks.remove(Integer.valueOf(subTaskId));
    }

    @Override
    public String toString() {
        String tmpDescription = "Не указан!";
        if (description != null) {
            tmpDescription = description;
        }
        String epicString = " Epic{" + "Id=" + id + ", Name='" + name + '\'' + ", Description='" + tmpDescription + '\'' + ", State='" + state + '\'';
        if (getChildSubTasks().size() == 0) {
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