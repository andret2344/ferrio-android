package eu.andret.kalendarzswiatnietypowych.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.time.LocalDateTime;
import java.util.Objects;

public class FloatingHolidaySuggestion {
	private int id;
	private String date;
	private String name;
	private String description;
	private LocalDateTime datetime;
	private String country;
	private ReportState reportState;
	private Integer holidayId;

	public FloatingHolidaySuggestion(final int id, final String date, final String name,
			final String description, final LocalDateTime datetime, final String country,
			final ReportState reportState, final Integer holidayId) {
		this.id = id;
		this.date = date;
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

	public String getDate() {
		return date;
	}

	public void setDate(final String date) {
		this.date = date;
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
		return "floating-" + holidayId;
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
		final FloatingHolidaySuggestion that = (FloatingHolidaySuggestion) o;
		return id == that.id
				&& Objects.equals(date, that.date)
				&& Objects.equals(name, that.name)
				&& Objects.equals(description, that.description)
				&& Objects.equals(datetime, that.datetime)
				&& Objects.equals(country, that.country)
				&& reportState == that.reportState
				&& Objects.equals(holidayId, that.holidayId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, date, name, description, datetime, country, reportState, holidayId);
	}

	@NonNull
	@Override
	public String toString() {
		return "FloatingHolidaySuggestion{" +
				"id=" + id +
				", date='" + date + '\'' +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				", datetime=" + datetime +
				", country='" + country + '\'' +
				", reportState=" + reportState +
				", holidayId=" + holidayId +
				'}';
	}
}
