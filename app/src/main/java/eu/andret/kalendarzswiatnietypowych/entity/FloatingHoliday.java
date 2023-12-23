package eu.andret.kalendarzswiatnietypowych.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "floating_holiday")
public class FloatingHoliday {
	@PrimaryKey
	private final int id;
	private final boolean usual;
	private final String name;
	private final String description;
	private final String url;
	private final String script;

	public FloatingHoliday(final int id, final boolean usual, final String name, final String description, final String url, final String script) {
		this.id = id;
		this.usual = usual;
		this.name = name;
		this.description = description;
		this.url = url;
		this.script = script;
	}

	public int getId() {
		return id;
	}

	public boolean isUsual() {
		return usual;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getUrl() {
		return url;
	}

	public String getScript() {
		return script;
	}

	@Override
	public boolean equals(final Object o) {
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
				&& Objects.equals(url, that.url)
				&& Objects.equals(script, that.script);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, usual, name, description, url, script);
	}

	@NonNull
	@Override
	public String toString() {
		return "FloatingHoliday{" +
				"id=" + id +
				", usual=" + usual +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				", url='" + url + '\'' +
				", script='" + script + '\'' +
				'}';
	}
}
