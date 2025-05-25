import common.Managers;
import history.HistoryManager;
import history.InMemoryHistoryManager;
import managers.InMemoryTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @BeforeEach
    public void beforeEach() {
        taskManager = new InMemoryTaskManager();
    }

    @DisplayName("Managers  возвращает проинициализированный и готовый к работе HistoryManager")
    @Test
    public void shouldManagersReturnInitializedHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        assertNotNull(historyManager, "HistoryManager не проинициализирован");
        assertEquals(InMemoryHistoryManager.class, historyManager.getClass(), "Объект не того класса");
    }
}
