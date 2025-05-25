package model;

import common.Managers;
import referencebook.States;
import referencebook.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SubTask extends Task {
    private int parentEpic;

    public SubTask(int id, String name, String description, States state, int parentEpic,
                   LocalDateTime startTime, Duration duration) {
        super(id, name, description, state, startTime, duration);
        this.parentEpic = parentEpic;
    }

    public SubTask(String name, String description, States state, int parentEpic,
                   LocalDateTime startTime, Duration duration) {
        super(name, description, state, startTime, duration);
        this.parentEpic = parentEpic;
    }

    public SubTask getSubTaskCopy() {
        return new SubTask(this.id, this.name, this.description, this.state, this.parentEpic,
                this.startTime, this.duration);
    }

    public int getParentEpic() {
        return parentEpic;
    }

    @Override
    public boolean hasValidFields() {
        return name != null && description != null && startTime != null && duration != null;
    }

    @Override
    public String toString() {
        DateTimeFormatter dateTimeFormatter = Managers.getDefaultDateTimeFormatter();
        return " SubTask{Id = " + id + ", Name='" + name + '\''
                + ", Description = '" + description + '\'' + ", State='" + state + '\''
                + ", Epic = " + parentEpic + ", StartTime=" + startTime.format(dateTimeFormatter) + "'"
                + ", Duration = " + duration.toMinutes() + ", EndTime=" + getEndTime().format(dateTimeFormatter) + "'"
                + '}';
    }

    @Override
    public String toStringForIO() {
        //ID, TYPE, NAME, Status, Description, StartTime, Duration, EndTime
        DateTimeFormatter dateTimeFormatter = Managers.getDefaultDateTimeFormatter();
        return String.format("%d,%s,%s,%s,%s,%d,%s,%s", id, TaskType.SUBTASK, name, state.toString(), description,
                parentEpic, startTime.format(dateTimeFormatter), duration.toMinutes());
    }
}

