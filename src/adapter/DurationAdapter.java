package adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import common.Managers;

import java.io.IOException;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

public class DurationAdapter extends TypeAdapter<Duration> {
    private static final DateTimeFormatter dtf = Managers.getDefaultDateTimeFormatter();

    @Override
    public void write(final JsonWriter jsonWriter, final Duration duration) throws IOException {
        if (duration == null) {
            jsonWriter.value(Duration.ofDays(0).toMinutes());
        } else {
            jsonWriter.value(duration.toMinutes());
        }
    }

    @Override
    public Duration read(final JsonReader jsonReader) throws IOException {
        String str = jsonReader.nextString();
        if (str == null) {
            return Duration.ofDays(0);
        } else {
            int durationInMinutes = Integer.parseInt(str);
            return Duration.ofMinutes(durationInMinutes);
        }
    }
}
