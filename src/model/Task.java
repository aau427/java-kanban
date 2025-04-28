package model;

import referencebook.States;

import java.util.Objects;

/*  из тз: "С помощью сеттеров экземпляры задач позволяют
 изменить любое своё поле, но это может повлиять на данные внутри менеджера".
  Вариант1: Ограничить видимость сетерров.  Можно было бы запихнуть классы Task, SubTask и Epic в один пакет с TaskManager.
  при этом сеттеры у Таsk и потомков сделать protected, т.е. доступные потомкам и классам в пакете, в т.ч.
  и TaskManager. Классы за пакетом к сеттерам доступа бы не имели.
  Мне такой подход не нравится, будет все в куче и таски и манагер. Плохое структурирование, а впереди куча спринтов....

  Вариант2: учитываем принцип - любой update извне только через TaskManager. Получаем, что реально нужен только setState
  у Эпика. Т.к. при изменении списка подзадач (добавили, удалили, проапдейтили), манагер перерассчитывает статус Эпика.
  Т.е.    -  оставляем только setState, но переносим его из Task в Эпик
               (нам не нужно менять сеттером статус таски и сабтаски).
                Иные сеттеры грохаем (немного помучаемся с заменой setId)
          -  в связи с тем, что останется только один сеттер setState у Эпика, то задача не допустить, чтобы получили
          ссылку на Эпик из внутреннего состояния манагера! Иначе по ссылке могут поменять его статус.
          Таких методов всего 2: getEpicById и GetEpicList
          вот и будем там возвращать копии эпиков с другими ссылками. Пусть меняют на здоровье, состояние манагера
          не изменится.
  Т.е. выбираем Вариант2.  Если бы было больше правок (обязательных сеттеров), то выбрал бы Вариант1.
 */
public class Task {
    protected Integer id;
    protected String name;
    protected String description;
    protected States state;

    public Task(int id, String name, String description, States state) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.state = state;
    }

    public Task(String name, String description, States state) {
        this.id = null;
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

    public boolean isValid() {
        return name != null && description != null;
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

    public States getState() {
        return state;
    }
}


