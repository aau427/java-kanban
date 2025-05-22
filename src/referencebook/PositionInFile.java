package referencebook;

public enum PositionInFile {
    ID(0),
    TYPE(1),
    NAME(2),
    STATE(3),
    DESCRIPTION(4),
    EPIC(5);
    private int position;

    PositionInFile(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
