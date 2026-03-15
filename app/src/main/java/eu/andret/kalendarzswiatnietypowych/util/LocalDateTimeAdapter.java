package eu.andret.kalendarzswiatnietypowych.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Override
	public void write(final JsonWriter out, final LocalDateTime value) throws IOException {
		out.value(value.format(formatter));
	}

	@Override
	public LocalDateTime read(final JsonReader in) throws IOException {
		return LocalDateTime.parse(in.nextString(), formatter);
	}
}
