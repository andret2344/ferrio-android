package eu.andret.kalendarzswiatnietypowych.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public class Holiday implements Comparable<Holiday>, Parcelable {
	public static final Parcelable.Creator<Holiday> CREATOR = new Parcelable.Creator<>() {
		@NonNull
		@Override
		public Holiday createFromParcel(final Parcel in) {
			final int metadataIdRead = in.readInt();
			final String textRead = in.readString();
			final boolean usualRead = Boolean.parseBoolean(in.readString());
			final String urlRead = in.readString();
			return new Holiday(metadataIdRead, textRead, usualRead, urlRead);
		}

		@NonNull
		@Override
		public Holiday[] newArray(final int size) {
			return new Holiday[size];
		}
	};

	private final int metadataId;
	@NonNull
	private final String text;
	private final boolean usual;
	@Nullable
	private final String url;

	public Holiday(final int metadataId, @NonNull final String text, final boolean usual, @Nullable final String url) {
		this.metadataId = metadataId;
		this.text = text;
		this.usual = usual;
		this.url = url;
	}

	public int getMetadataId() {
		return metadataId;
	}

	@NonNull
	public String getText() {
		return text;
	}

	public boolean isUsual() {
		return usual;
	}

	@Nullable
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
		return metadataId == holiday.metadataId && usual == holiday.usual && text.equals(holiday.text) && Objects.equals(url, holiday.url);
	}

	@Override
	public int hashCode() {
		return Objects.hash(metadataId, text, usual, url);
	}

	@NonNull
	@Override
	public String toString() {
		return "Holiday{" +
				"metadataId=" + metadataId +
				", text='" + text + '\'' +
				", usual=" + usual +
				", url='" + url + '\'' +
				'}';
	}

	@Override
	public int compareTo(@NonNull final Holiday o) {
		if (usual != o.usual) {
			return usual ? -1 : 1;
		}
		return text.compareTo(o.text);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel parcel, final int i) {
		parcel.writeInt(metadataId);
		parcel.writeString(text);
		parcel.writeString(String.valueOf(usual));
		parcel.writeString(url);
	}
}
