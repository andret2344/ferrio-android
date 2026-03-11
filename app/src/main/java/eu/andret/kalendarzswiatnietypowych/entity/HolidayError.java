package eu.andret.kalendarzswiatnietypowych.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public class HolidayError {
	private final int metadata;
	private final String language;
	private final String reportType;
	private final String description;

	public HolidayError(final int metadata, final String language, final String reportType, final String description) {
		this.metadata = metadata;
		this.language = language;
		this.reportType = reportType;
		this.description = description;
	}

	@Override
	public boolean equals(@Nullable final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final HolidayError that = (HolidayError) o;
		return metadata == that.metadata
				&& Objects.equals(language, that.language)
				&& Objects.equals(reportType, that.reportType)
				&& Objects.equals(description, that.description);
	}

	@Override
	public int hashCode() {
		return Objects.hash(metadata, language, reportType, description);
	}

	@NonNull
	@Override
	public String toString() {
		return "HolidayError{" +
				"metadata=" + metadata +
				", language='" + language + '\'' +
				", reportType='" + reportType + '\'' +
				", description='" + description + '\'' +
				'}';
	}
}
