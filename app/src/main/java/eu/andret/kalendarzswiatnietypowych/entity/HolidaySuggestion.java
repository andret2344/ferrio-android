package eu.andret.kalendarzswiatnietypowych.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.time.LocalDateTime;

public interface HolidaySuggestion {
	int getId();

	@NonNull
	String getDisplayDate();

	@NonNull
	String getName();

	@NonNull
	String getDescription();

	@NonNull
	LocalDateTime getDatetime();

	@Nullable
	String getCountry();

	@NonNull
	ReportState getReportState();

	@Nullable
	String getComment();

	@Nullable
	Integer getHolidayId();

	@Nullable
	String getFullHolidayId();

	@Override
	boolean equals(Object obj);
}
