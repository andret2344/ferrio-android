package eu.andret.kalendarzswiatnietypowych.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.time.LocalDateTime;
import java.util.Objects;

public class FixedHolidaySuggestion {
	private int id;
	private int day;
	private int month;
	private String name;
	private String description;
	private LocalDateTime datetime;
	private String country;
	private ReportState reportState;
	private Integer holidayId;

	public FixedHolidaySuggestion(final int id, final int day, final int month, final String name,
			final String description, final LocalDateTime datetime, final String country,
			final ReportState reportState, final Integer holidayId) {
		this.id = id;
		this.day = day;
		this.month = month;
		this.name = name;
		this.description = description;
		this.datetime = datetime;
		this.country = country;
		this.reportState = reportState;
		this.holidayId = holidayId;
	}

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public int getDay() {
		return day;
	}

	public void setDay(final int day) {
		this.day = day;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(final int month) {
		this.month = month;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public LocalDateTime getDatetime() {
		return datetime;
	}

	public void setDatetime(final LocalDateTime datetime) {
		this.datetime = datetime;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(final String country) {
		this.country = country;
	}

	public ReportState getReportState() {
		return reportState;
	}

	public void setReportState(final ReportState reportState) {
		this.reportState = reportState;
	}

	public Integer getHolidayId() {
		return holidayId;
	}

	@Nullable
	public String getFullHolidayId() {
		if (holidayId == null) {
			return null;
		}
		return "fixed-" + holidayId;
	}

	public void setHolidayId(final Integer holidayId) {
		this.holidayId = holidayId;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final FixedHolidaySuggestion that = (FixedHolidaySuggestion) o;
		return id == that.id
				&& day == that.day
				&& month == that.month
				&& Objects.equals(name, that.name)
				&& Objects.equals(description, that.description)
				&& Objects.equals(datetime, that.datetime)
				&& Objects.equals(country, that.country)
				&& reportState == that.reportState
				&& Objects.equals(holidayId, that.holidayId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, day, month, name, description, datetime, country, reportState, holidayId);
	}

	@NonNull
	@Override
	public String toString() {
		return "FixedHolidaySuggestion{" +
				"id=" + id +
				", day=" + day +
				", month=" + month +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				", datetime=" + datetime +
				", country='" + country + '\'' +
				", reportState=" + reportState +
				", holidayId=" + holidayId +
				'}';
	}
}
