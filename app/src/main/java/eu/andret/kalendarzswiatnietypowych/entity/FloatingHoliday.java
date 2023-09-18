package eu.andret.kalendarzswiatnietypowych.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

public class FloatingHoliday implements Parcelable {
	private final int id;
	private final boolean usual;
	private final String name;
	private final String description;
	private final String url;
	private final String script;

	protected FloatingHoliday(final Parcel in) {
		id = in.readInt();
		usual = in.readByte() != 0;
		name = in.readString();
		description = in.readString();
		url = in.readString();
		script = in.readString();
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

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(@NonNull final Parcel parcel, final int flags) {
		parcel.writeInt(id);
		parcel.writeByte((byte) (usual ? 1 : 0));
		parcel.writeString(name);
		parcel.writeString(description);
		parcel.writeString(url);
		parcel.writeString(script);
	}


	public static final Creator<FloatingHoliday> CREATOR = new Creator<>() {
		@Override
		public FloatingHoliday createFromParcel(final Parcel in) {
			return new FloatingHoliday(in);
		}

		@Override
		public FloatingHoliday[] newArray(final int size) {
			return new FloatingHoliday[size];
		}
	};

}
