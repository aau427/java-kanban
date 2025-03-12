package managers;

public class IdManager {
    private static Integer currentId = 0;

    public static Integer getNextId() {
        return ++currentId;
    }
}