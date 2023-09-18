package eu.andret.kalendarzswiatnietypowych.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Holiday implements Comparable<Holiday>, Parcelable {
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

	protected Holiday(final Parcel in) {
		id = in.readInt();
		name = in.readString();
		description = in.readString();
		usual = in.readByte() != 0;
		url = in.readString();
	}

	public int getId() {
		return id;
	}

	@NonNull
	public String getName() {
		return name;
	}

	public boolean isUsual() {
		return usual;
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
		parcel.writeString(url);
	}

	public static final Creator<Holiday> CREATOR = new Creator<Holiday>() {
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
