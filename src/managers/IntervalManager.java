package managers;

import common.Managers;
import exception.ManagerIntervalException;
import exception.ManagerSaveException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntervalManager {
    /*
    Разбиваем десятилетний отрезок с 01.01.2025 на полуинтервалы по 15 минут: [0, 15), [15,30), [30,45) и т.д.
    номера интервалов храним в HashMap в качестве ключей
    значение ключа - это признак занятости интервала: true. Если интервал свободен, то false
     */
    private final Map<Integer, Boolean> timeIntervals;
    private final int quantityOfIntervals = 10 * 365 * 24 * 4; //Интервал 10 лет по 15 минут
    private final LocalDateTime intervalStartTime = LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0);
    private final DateTimeFormatter dateTimeFormatter = Managers.getDefaultDateTimeFormatter();

    public IntervalManager() {
        timeIntervals = new HashMap<>();
        initializeTimeInterval();
    }

    public Map<Integer, Boolean> getTimeIntervals() {
        return timeIntervals;
    }

    public void occupyIntervals(LocalDateTime startTime, LocalDateTime endTime) throws ManagerSaveException {
        if (!canUseInterval(startTime, endTime)) {
            throw new ManagerIntervalException("Интервал времени: [" +
                    startTime.format(dateTimeFormatter) + ", " +
                    endTime.format(dateTimeFormatter) + ") занят!");
        }
        occupyOrRestoreTimeIntervals(startTime, endTime, true);
    }

    public void restoreIntervals(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        occupyOrRestoreTimeIntervals(startDateTime, endDateTime, false);
    }

    public boolean canUseInterval(LocalDateTime startTime, LocalDateTime endTime) {
        List<Integer> intervalsList = determineIntervalsByDates(startTime, endTime);
        return !intervalsList.stream().map(timeIntervals::get)
                .anyMatch(isIntervalOccupy -> isIntervalOccupy == true);
    }

    private List<Integer> determineIntervalsByDates(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        //сколько прошло целых минут с начала интервала
        Duration durationToStartDate = Duration.between(intervalStartTime, startDateTime);
        int numberOfFirstInterval = (int) durationToStartDate.toMinutes() / 15;
        Duration durationToEndDate = Duration.between(intervalStartTime, endDateTime);
        int numberOfFinishInterval = (int) durationToEndDate.toMinutes() / 15;
        List<Integer> returnList = new ArrayList<>();
        for (int i = numberOfFirstInterval; i <= numberOfFinishInterval; i++) {
            returnList.add(i);
        }
        return returnList;
    }

    private void occupyOrRestoreTimeIntervals(LocalDateTime startDateTime, LocalDateTime endDateTime, boolean isOccupy) {
        determineIntervalsByDates(startDateTime, endDateTime).forEach(i -> timeIntervals.put(i, isOccupy));
    }

    private void initializeTimeInterval() {
        for (int i = 0; i < quantityOfIntervals; i++) {
            timeIntervals.put(i, false);
        }
    }
}