package model;

import common.Managers;
import referencebook.States;
import referencebook.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Task {
    protected Integer id;
    protected String name;
    protected String description;
    protected States state;
    protected LocalDateTime startTime;
    protected Duration duration;

    public Task(int id, String name, String description, States state, LocalDateTime startTime, Duration duration) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.state = state;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Task(String name, String description, States state, LocalDateTime startTime, Duration duration) {
        this.id = null;
        this.name = name;
        this.description = description;
        this.state = state;
        this.startTime = startTime;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Integer getId() {
        return id;
    }

    public boolean hasValidFields() {
        return name != null && description != null && duration != null;
    }

    public Duration getDuration() {
        return duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return startTime.plus(duration);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        DateTimeFormatter dateTimeFormatter = Managers.getDefaultDateTimeFormatter();
        return " Task{" + "Id=" + id + ", Name='" + name + '\''
                + ", Description='" + description + '\'' + ", State='" + state.name() + '\''
                + ", StartTime=" + startTime.format(dateTimeFormatter) + "'" + ", Duration = " + duration.toMinutes()
                + ", EndTime=" + getEndTime().format(dateTimeFormatter) + "'"
                + '}';
    }

    public States getState() {
        return state;
    }

    public String toStringForIO() {
        //ID, TYPE, NAME, Status, Description, StartTime, duration
        String startTimeToString = "";
        if (startTime != null) {
            startTimeToString = startTime.format(Managers.getDefaultDateTimeFormatter());
        }
        return String.format("%d,%s,%s,%s,%s,%s,%s,%d", id, TaskType.TASK, name, state.toString(), description, null,
                startTimeToString, duration.toMinutes());
    }
}


