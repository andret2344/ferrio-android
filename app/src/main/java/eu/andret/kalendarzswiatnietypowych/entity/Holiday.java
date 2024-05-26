package eu.andret.kalendarzswiatnietypowych.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "holiday")
public class Holiday implements Comparable<Holiday> {
	@PrimaryKey
	private final int id;
	private final String name;
	private final String description;
	private final boolean usual;
	private final String countryCode;
	private final String countryName;
	private final String url;

	public Holiday(final int id, final String name, final String description, final boolean usual, final String countryCode, final String countryName, final String url) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.usual = usual;
		this.countryCode = countryCode;
		this.countryName = countryName;
		this.url = url;
	}

	public Holiday(@NonNull final FloatingHoliday floatingHoliday) {
		this(-floatingHoliday.getId(), floatingHoliday.getName(), floatingHoliday.getDescription(),
				floatingHoliday.isUsual(), floatingHoliday.getCountryCode(), floatingHoliday.getCountryName(), floatingHoliday.getUrl());
	}

	public int getId() {
		return id;
	}

	@NonNull
	public String getName() {
		return name;
	}

	@NonNull
	public String getDescription() {
		return description;
	}

	public boolean isUsual() {
		return usual;
	}

	@Nullable
	public String getCountryCode() {
		return countryCode;
	}

	@Nullable
	public String getCountryName() {
		return countryName;
	}

	@NonNull
	public String getUrl() {
		return url;
	}

	@Override
	public boolean equals(@Nullable final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final Holiday holiday = (Holiday) o;
		return id == holiday.id
				&& Objects.equals(name, holiday.name)
				&& Objects.equals(description, holiday.description)
				&& usual == holiday.usual
				&& Objects.equals(countryCode, holiday.countryCode)
				&& Objects.equals(countryName, holiday.countryName)
				&& Objects.equals(url, holiday.url);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, description, usual, countryCode, countryName, url);
	}

	@NonNull
	@Override
	public String toString() {
		return "Holiday{" +
				"id=" + id +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				", usual=" + usual +
				", countryCode='" + countryCode + '\'' +
				", countryName='" + countryName + '\'' +
				", url='" + url + '\'' +
				'}';
	}

	@Override
	public int compareTo(@NonNull final Holiday o) {
		if (usual != o.usual) {
			return usual ? -1 : 1;
		}
		return name.compareTo(o.name);
	}
}
