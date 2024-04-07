package eu.andret.kalendarzswiatnietypowych.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public class Holiday implements Comparable<Holiday>, Parcelable {
	private final int id;
	private final String name;
	private final String description;
	private final boolean usual;
	private final String countryCode;
	private final String url;

	public Holiday(final int id, final String name, final String description, final boolean usual, final String countryCode, final String url) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.usual = usual;
		this.countryCode = countryCode;
		this.url = url;
	}

	public Holiday(@NonNull final FloatingHoliday floatingHoliday) {
		this(floatingHoliday.getId(), floatingHoliday.getName(), floatingHoliday.getDescription(), floatingHoliday.isUsual(), floatingHoliday.getCountryCode(), floatingHoliday.getUrl());
	}

	protected Holiday(@NonNull final Parcel in) {
		id = in.readInt();
		name = in.readString();
		description = in.readString();
		usual = in.readByte() != 0;
		countryCode = in.readString();
		url = in.readString();
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
				&& Objects.equals(url, holiday.url);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, description, usual, countryCode, url);
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

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(@NonNull final Parcel parcel, final int i) {
		parcel.writeInt(id);
		parcel.writeString(name);
		parcel.writeString(description);
		parcel.writeByte((byte) (usual ? 1 : 0));
		parcel.writeString(countryCode);
		parcel.writeString(url);
	}

	public static final Creator<Holiday> CREATOR = new Creator<>() {
		@Override
		public Holiday createFromParcel(final Parcel in) {
			return new Holiday(in);
		}

		@Override
		public Holiday[] newArray(final int size) {
			return new Holiday[size];
		}
	};
}
