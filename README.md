# java-kanban
Допущения спринта 7:
1. Дано: (выдержка из ТЗ): 
Дата начала задачи по каким-то причинам может быть не задана.
Тогда при добавлении её не следует учитывать в списке задач
и подзадач, отсортированных по времени начала.
Такая задача не влияет на приоритет других,
а при попадании в список может сломать логику работы
компаратора.

Вывод: 
1.1 дата начала задачи не обязательна к заполнению.
1.2 задачу с незаполненной датой начала не добавлять в список приоритизированных.
1.3 задача с незаполненной датой начала не участвует в проверке "задачи и подзадачи 
    не пересекаются по времени выполнения", ибо не понятно это самое время выполнения.
1.4 про подзадачи речи в п.1 не идет, т.е считаем, что дата начала обязательна к заполнению.

