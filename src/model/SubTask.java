package model;

import referencebook.States;

public class SubTask extends Task {
    private int parentEpic;

    public SubTask(String name, String description, States state, int parentEpic) {
        super(name, description, state);
        this.parentEpic = parentEpic;
    }

    public int getParentEpic() {
        return parentEpic;
    }

    public void setParentEpic(int parentEpic) {
        this.parentEpic = parentEpic;
    }

    @Override
    public String toString() {
        String tmpDescription = "Не указан!";
        if (description != null) {
            tmpDescription = description;
        }
        return " SubTask{Id = " + id + ", Name='" + name + '\''
                + ", Description = '" + tmpDescription + '\'' + ", State='" + state + '\''
                + ", Epic = " + parentEpic + "}";
    }
}

