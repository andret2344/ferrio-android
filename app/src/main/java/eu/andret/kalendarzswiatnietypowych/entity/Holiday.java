package eu.andret.kalendarzswiatnietypowych.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Locale;
import java.util.Objects;

@Entity(tableName = "holiday", indices = @Index({"month", "day"}))
public class Holiday implements Comparable<Holiday> {
	@PrimaryKey
	@NonNull
	private final String id;
	private final int day;
	private final int month;
	private final String name;
	private final String description;
	private final boolean usual;
	private final String country;
	private final String url;
	private final boolean matureContent;

	public Holiday(@NonNull final String id, final int day, final int month, final String name,
			final String description, final boolean usual, final String country,
			final String url, final boolean matureContent) {
		this.id = id;
		this.day = day;
		this.month = month;
		this.name = name;
		this.description = description;
		this.usual = usual;
		this.country = country;
		this.url = url;
		this.matureContent = matureContent;
	}

	@NonNull
	public String getId() {
		return id;
	}

	public int getDay() {
		return day;
	}

	public int getMonth() {
		return month;
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
	public String getCountry() {
		return country;
	}

	@Nullable
	public String getCountryName() {
		if (country == null) {
			return null;
		}
		return Locale.forLanguageTag(country).getDisplayCountry(Locale.getDefault());
	}

	@NonNull
	public String getUrl() {
		return url;
	}

	public boolean isMatureContent() {
		return matureContent;
	}

	public boolean isFloating() {
		return id.startsWith("floating-");
	}

	public boolean isFixed() {
		return id.startsWith("fixed-");
	}

	public int getNumericId() {
		return Integer.parseInt(id.substring(id.indexOf('-') + 1));
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
		return day == holiday.day
				&& month == holiday.month
				&& usual == holiday.usual
				&& matureContent == holiday.matureContent
				&& Objects.equals(id, holiday.id)
				&& Objects.equals(name, holiday.name)
				&& Objects.equals(description, holiday.description)
				&& Objects.equals(country, holiday.country)
				&& Objects.equals(url, holiday.url);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, day, month, name, description, usual, country, url, matureContent);
	}

	@NonNull
	@Override
	public String toString() {
		return "Holiday{" +
				"id='" + id + '\'' +
				", day=" + day +
				", month=" + month +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				", usual=" + usual +
				", country='" + country + '\'' +
				", url='" + url + '\'' +
				", matureContent=" + matureContent +
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
