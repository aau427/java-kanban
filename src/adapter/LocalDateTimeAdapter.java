package adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import common.Managers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    private static final DateTimeFormatter dtf = Managers.getDefaultDateTimeFormatter();


    @Override
    public void write(final JsonWriter jsonWriter, final LocalDateTime localDate) throws IOException {
        if (localDate == null) {
            jsonWriter.value("дата не установлена");
        } else {
            jsonWriter.value(localDate.format(dtf));
        }
    }

    @Override
    public LocalDateTime read(final JsonReader jsonReader) throws IOException {
        String str = jsonReader.nextString();

        if (str.equals("дата не установлена")) {
            return null;
        } else return LocalDateTime.parse(str, dtf);
    }
}