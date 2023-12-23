package eu.andret.kalendarzswiatnietypowych.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Entity(tableName = "holiday")
public class Holiday implements Comparable<Holiday> {
	@PrimaryKey
	private final int id;
	private final String name;
	private final String description;
	private final boolean usual;
	private final String url;

	public Holiday(final int id, final String name, final String description, final boolean usual, final String url) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.usual = usual;
		this.url = url;
	}

	public Holiday(final FloatingHoliday floatingHoliday) {
		this(floatingHoliday.getId(), floatingHoliday.getName(), floatingHoliday.getDescription(), floatingHoliday.isUsual(), floatingHoliday.getUrl());
	}

	public int getId() {
		return id;
	}

	@NonNull
	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public boolean isUsual() {
		return usual;
	}

	public String getUrl() {
		return url;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final Holiday holiday = (Holiday) o;
		return id == holiday.id
				&& usual == holiday.usual
				&& Objects.equals(name, holiday.name)
				&& Objects.equals(description, holiday.description)
				&& Objects.equals(url, holiday.url);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, description, usual, url);
	}

	@NotNull
	@Override
	public String toString() {
		return "Holiday{" +
				"id=" + id +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				", usual=" + usual +
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
