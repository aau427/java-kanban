package model;

import referencebook.States;

import java.util.Objects;

public class Task {
    protected Integer id;
    protected String name;
    protected String description;
    protected States state;


    public Task(String name, String description, States state) {
        this.id = null;
        this.name = name;
        this.description = description;
        this.state = state;
    }

    public Task(int id, String name, String description, States state) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.state = state;
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

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isValid() {
        return name != null && id != null;
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
        String tmpDescription = "Не указан!";
        if (description != null) {
            tmpDescription = description;
        }
        return " Task{" + "Id=" + id + ", Name='" + name + '\''
                + ", Description='" + tmpDescription + '\'' + ", State='" + state.name() + '\''
                + '}';
    }

    public void setState(States state) {
        this.state = state;
    }


    public States getState() {
        return state;
    }
}


