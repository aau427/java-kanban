package model;

import referencebook.States;
import referencebook.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Epic extends Task {

    private LocalDateTime endTime;

    private final List<Integer> childSubTasks = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description, States.NEW, null, Duration.ZERO);
    }

    public Epic(int id, String name, String description) {
        super(id, name, description, States.NEW, null, Duration.ZERO);
    }

    @Override
    public LocalDateTime getEndTime() {
        throw new RuntimeException("В классе Epic запрещено использовать getEndTime(). " +
                "Воспользуйтесь getEndTimeOptional");
    }

    public Optional<LocalDateTime> getEndTimeOptional() {
        return Optional.ofNullable(endTime);
    }

    @Override
    public LocalDateTime getStartTime() {
        throw new RuntimeException("В классе Epic запрещено использовать getStartTime(). " +
                "Воспользуйтесь getStartTimeOptional");
    }

    public Optional<LocalDateTime> getStartTimeOptional() {
        return Optional.ofNullable(startTime);
    }

    @Override
    public Duration getDuration() {
        if (duration == null) {
            return Duration.ZERO;
        } else {
            return duration;
        }
    }

    public void setState(States state) {
        this.state = state;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Epic getEpicCopy() {
        Epic newEpic = new Epic(this.getId(), this.getName(), this.getDescription());
        newEpic.childSubTasks.addAll(this.getChildSubTasks());
        newEpic.state = this.state;
        newEpic.startTime = this.startTime;
        newEpic.duration = this.duration;
        newEpic.endTime = endTime;
        return newEpic;
    }

    public List<Integer> getChildSubTasks() {
        return childSubTasks;
    }

    @Override
    public String toString() {
        String epicString = " Epic{" + "Id=" + id + ", Name='" + name + ", Description='" + description +
                ", State='" + state;
        if (getChildSubTasks().isEmpty()) {
            epicString = epicString + ", ChildSubTasks.Size = 0";
        } else {
            epicString = epicString + ", ChildSubTasks{";
            for (Integer subTaskId : getChildSubTasks()) {
                epicString = epicString + " " + subTaskId;
            }
            epicString = epicString + "}";

        }
        if (getStartTimeOptional().isPresent()) {
            epicString = epicString + ", StartTime=" + getStartTimeOptional();
        }
        epicString = epicString + ", Duration=" + getDuration().toMinutes();
        if (getEndTimeOptional().isPresent()) {
            epicString = epicString + ", EndTime=" + getEndTimeOptional();
        }
        epicString = epicString + "}";
        return epicString;
    }

    @Override
    public String toStringForIO() {
        //ID, TYPE, NAME, Status, Description
        return String.format("%d,%s,%s,%s,%s", id, TaskType.EPIC, name, state.toString(), description);
    }

    @Override
    public boolean hasValidFields() {
        return name != null && description != null;
    }


}