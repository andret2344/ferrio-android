package eu.andret.kalendarzswiatnietypowych.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Locale;
import java.util.Objects;

@Entity(tableName = "floating_holiday")
public class FloatingHoliday {
	@PrimaryKey
	private final int id;
	private final boolean usual;
	private final String name;
	private final String description;
	private final String countryCode;
	private final String url;
	private final String script;

	public FloatingHoliday(final int id, final boolean usual, final String name, final String description,
						   final String countryCode, final String url, final String script) {
		this.id = id;
		this.usual = usual;
		this.name = name;
		this.description = description;
		this.countryCode = countryCode;
		this.url = url;
		this.script = script;
	}

	public int getId() {
		return id;
	}

	public boolean isUsual() {
		return usual;
	}

	@NonNull
	public String getName() {
		return name;
	}

	@NonNull
	public String getDescription() {
		return description;
	}

	@Nullable
	public String getCountryCode() {
		return countryCode;
	}

	@Nullable
	public String getCountryName() {
		if (getCountryCode() == null) {
			return null;
		}
		return new Locale(Locale.getDefault().getLanguage(), countryCode).getDisplayCountry();
	}

	@Nullable
	public String getUrl() {
		return url;
	}

	@NonNull
	public String getScript() {
		return script;
	}

	@Override
	public boolean equals(@Nullable final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final FloatingHoliday that = (FloatingHoliday) o;
		return id == that.id
				&& usual == that.usual
				&& Objects.equals(name, that.name)
				&& Objects.equals(description, that.description)
				&& Objects.equals(countryCode, that.countryCode)
				&& Objects.equals(url, that.url)
				&& Objects.equals(script, that.script);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, usual, name, description, countryCode, url, script);
	}

	@NonNull
	@Override
	public String toString() {
		return "FloatingHoliday{" +
				"id=" + id +
				", usual=" + usual +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				", countryCode='" + countryCode + '\'' +
				", url='" + url + '\'' +
				", script='" + script + '\'' +
				'}';
	}
}
