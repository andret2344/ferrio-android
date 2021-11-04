package eu.andret.kalendarzswiatnietypowych.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import lombok.Value;

@Value
public class Holiday implements Comparable<Holiday>, Parcelable {
	public static final Parcelable.Creator<Holiday> CREATOR = new Parcelable.Creator<Holiday>() {
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

	int metadataId;
	@NonNull
	String text;
	boolean usual;
	@Nullable
	String url;

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
