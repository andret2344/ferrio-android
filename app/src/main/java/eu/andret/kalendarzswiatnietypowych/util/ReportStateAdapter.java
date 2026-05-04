package eu.andret.kalendarzswiatnietypowych.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

import eu.andret.kalendarzswiatnietypowych.entity.ReportState;

class ReportStateAdapter extends TypeAdapter<ReportState> {
	@Override
	public void write(final JsonWriter out, final ReportState value) throws IOException {
		if (value == null) {
			out.nullValue();
			return;
		}
		out.value(value.name());
	}

	@Override
	public ReportState read(final JsonReader in) throws IOException {
		if (in.peek() == JsonToken.NULL) {
			in.nextNull();
			return ReportState.UNKNOWN;
		}
		final String raw = in.nextString();
		try {
			return ReportState.valueOf(raw);
		} catch (final IllegalArgumentException e) {
			return ReportState.UNKNOWN;
		}
	}
}
