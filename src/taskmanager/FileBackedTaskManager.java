package taskmanager;

import exception.ManagerLoadException;
import exception.ManagerSaveException;
import history.HistoryManager;
import model.Epic;
import model.SubTask;
import model.Task;
import referencebook.PositionInFile;
import referencebook.States;
import referencebook.TaskType;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final String fileName;
    private final Charset charset = StandardCharsets.UTF_8;

    public FileBackedTaskManager(final String fileName) {
        this.fileName = fileName;
        load();
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        return new FileBackedTaskManager(file.getName());
    }

    @Override
    public int createEpic(Epic epic) {
        int epicId = super.createEpic(epic);
        if (epicId != -1) {
            save();
        }
        return epicId;
    }

    @Override
    public int createSubTask(SubTask subTask) {
        int subTaskId = super.createSubTask(subTask);
        if (subTaskId != -1) {
            save();
        }
        return subTaskId;
    }

    @Override
    public int createTask(Task task) {
        int taskId = super.createTask(task);
        if (taskId != -1) {
            save();
        }
        return taskId;
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubTasks() {
        super.deleteAllSubTasks();
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteEpicById(int epicId) {
        super.deleteEpicById(epicId);
        save();
    }

    @Override
    public void deleteSubTaskById(int subTaskId) {
        super.deleteSubTaskById(subTaskId);
        save();
    }

    @Override
    public void deleteTaskById(int taskId) {
        super.deleteTaskById(taskId);
        save();
    }

    @Override
    public boolean updateEpic(Epic epic) {
        boolean isUpdate = super.updateEpic(epic);
        if (isUpdate) {
            save();
        }
        return isUpdate;
    }

    @Override
    public boolean updateSubTask(SubTask subTask) {
        boolean isUpdate = super.updateSubTask(subTask);
        if (isUpdate) {
            save();
        }
        return isUpdate;
    }

    @Override
    public boolean updateTask(Task task) {
        boolean isUpdate = super.updateTask(task);
        if (isUpdate) {
            save();
        }
        return isUpdate;
    }

    @Override
    public Epic getEpicById(int epicId) {
        Epic epic = super.getEpicById(epicId);
        save();
        return epic;
    }

    @Override
    public SubTask getSubTaskById(int subTaskId) {
        SubTask subTask = super.getSubTaskById(subTaskId);
        save();
        return subTask;
    }

    @Override
    public Task getTaskById(int taskId) {
        Task task = super.getTaskById(taskId);
        save();
        return task;
    }

    private void save() throws ManagerSaveException {
        try (FileWriter fileWriter = new FileWriter(fileName, charset)) {
            writeFirstLine(fileWriter);
            writeAllTasks(fileWriter);
            writeAllEpics(fileWriter);
            writeAllSubTasks(fileWriter);
            writeAllHistory(fileWriter);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при открытии потока для сохранения в файл", e);
        }
    }

    private void writeFirstLine(FileWriter fileWriter) throws IOException {
        final String firstLine = "id,type,name,status,description,epic" + "\n";
        fileWriter.write(firstLine);
    }

    private void writeAllTasks(final FileWriter fileWriter) throws IOException {
        for (Task task : getTaskList()) {
            fileWriter.write(task.toStringForSaveToFile() + "\n");
        }
    }

    private void writeAllEpics(final FileWriter fileWriter) throws IOException {
        for (Epic epic : getEpicList()) {
            fileWriter.write(epic.toStringForSaveToFile() + "\n");
        }
    }

    private void writeAllSubTasks(final FileWriter fileWriter) throws IOException {
        for (SubTask subTask : getSubTaskList()) {
            fileWriter.write(subTask.toStringForSaveToFile() + "\n");
        }
    }

    private void writeAllHistory(final FileWriter fileWriter) throws IOException {
        /* Не понял, как хранить историю в файле, пример в ТЗ какой-то кривой
        говорится, что "эпик и задача просмотрена, а подзадача - нет"
        при это в примере вообще ничего нет про просмотр, просто сохранены задачи:

        id,type,name,status,description,epic
        1,TASK,Task1,NEW,Description task1,
        2,EPIC,Epic2,DONE,Description epic2,
        3,SUBTASK,Sub Task2,DONE,Description sub task3,2

        Руки оторвать тому, кто составляет задачи, либо я - баран.
        Решил так - после списка задач в файле - пустая строка, потом идентификаторы
        просмотренных задач через запятую, чтоб разбирать строку было легко
         */
        List<Task> historyList = getHistory();
        if (historyList.isEmpty()) {
            return;
        }
        writeEmptyLine(fileWriter);
        fileWriter.write(String.format("%d", historyList.getFirst().getId()));
        for (int i = 1; i < historyList.size(); i++) {
            fileWriter.write(String.format(",%d", historyList.get(i).getId()));
        }
    }

    private void writeEmptyLine(FileWriter fileWriter) throws IOException {
        fileWriter.write("\n");
    }

    private void load() throws ManagerLoadException {
        if (!Files.exists(Paths.get(fileName))) {
            return;
        }
        //загрузить из файла списки Эпиков,  Тасков и Сабтасков и историю
        int id2Set = loadAllFromFile();
        //установить текущее значение нумератора, оно очевидно, что не 1.
        super.setCurrentId(id2Set);
    }

    //возвращает максимальную ID загруженной таски, эпика или сабтаска
    public int loadAllFromFile() throws ManagerSaveException {
        int maxLoadTaskId = 0;
        try (FileReader reader = new FileReader(fileName, StandardCharsets.UTF_8)) {
            BufferedReader fileBuffer = new BufferedReader(reader);
            fileBuffer.readLine(); //это заголовок, он нигде не нужен
            boolean currentLineIsHistory = false;
            while (fileBuffer.ready()) {
                String line = fileBuffer.readLine();
                if (currentLineIsHistory) {
                    loadHistory(line);
                }
                if ((!line.isEmpty()) && (!currentLineIsHistory)) {
                    int tmpId = commonLoadTasks(line);
                    if (tmpId > maxLoadTaskId) {
                        maxLoadTaskId = tmpId;
                    }
                } else {
                    currentLineIsHistory = true;
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при записи загрузке файла с задачами и историей: ", e);
        }
        return maxLoadTaskId;
    }

    private int commonLoadTasks(String line) {
        String[] splitLine = line.split(",");
        int taskId = Integer.parseInt(splitLine[PositionInFile.ID.getPosition()]);
        TaskType taskType = TaskType.valueOf(splitLine[PositionInFile.TYPE.getPosition()]);
        String taskName = splitLine[PositionInFile.NAME.getPosition()];
        States taskState = States.valueOf(splitLine[PositionInFile.STATE.getPosition()]);
        String taskDescription = splitLine[PositionInFile.DESCRIPTION.getPosition()];
        switch (taskType) {
            case EPIC:
                Epic epic = new Epic(taskId, taskName, taskDescription);
                super.createEpicCommon(epic);
                break;
            case TASK:
                Task task = new Task(taskId, taskName, taskDescription, taskState);
                super.createTaskCommon(task);
                break;
            case SUBTASK:
                int parentEpic = Integer.parseInt(splitLine[PositionInFile.EPIC.getPosition()]);
                SubTask subTask = new SubTask(taskId, taskName, taskDescription, taskState, parentEpic);
                super.createSubTaskCommon(subTask);
                break;
        }
        return taskId;
    }

    private void loadHistory(String line) {
        String[] historyListOfId = line.split(",");
        for (String strTaskId : historyListOfId) {
            Integer taskId = Integer.parseInt(strTaskId);
            loadTaskToHistory(taskId);
        }
    }

    private void loadTaskToHistory(Integer taskId) {
        HistoryManager historyManager = super.getHistoryManager();

        Epic epic = super.getEpicByIdWithoutHistory(taskId);
        if (epic != null) {
            historyManager.addTask(epic);
            return;
        }
        Task task = super.getTaskByIdWithoutHistory(taskId);
        if (task != null) {
            historyManager.addTask(task);
            return;
        }
        SubTask subTask = super.getSubTaskByIdWithoutHistory(taskId);
        if (subTask != null) {
            historyManager.addTask(subTask);
        }
    }
}