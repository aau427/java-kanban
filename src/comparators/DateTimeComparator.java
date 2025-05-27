package comparators;

import java.time.LocalDateTime;
import java.util.Comparator;

public class DateTimeComparator implements Comparator<LocalDateTime> {
    @Override
    public int compare(LocalDateTime date1, LocalDateTime date2) {
        if (date1 == null && date2 == null) {
            return 0;
        } else if (date1 == null) {
            return -1;
        } else if (date2 == null) {
            return 1;
        } else if (date1.isAfter(date2)) {
            return 1;
        } else if (date2.isAfter(date1)) {
            return -1;
        } else {
            return 0;
        }
    }
}
