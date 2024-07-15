package eu.andret.kalendarzswiatnietypowych.persistance;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import eu.andret.kalendarzswiatnietypowych.entity.Holiday;

public final class Converters {
	private static final Gson GSON = new Gson();

	private Converters() {
	}

	@TypeConverter
	public static List<Holiday> fromString(final String value) {
		final Type listType = TypeToken.getParameterized(List.class, Holiday.class).getType();
		return GSON.fromJson(value, listType);
	}

	@TypeConverter
	public static String fromList(final List<Holiday> list) {
		return GSON.toJson(list);
	}
}
