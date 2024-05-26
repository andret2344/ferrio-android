package eu.andret.kalendarzswiatnietypowych.entity;

import androidx.annotation.NonNull;

import java.util.Objects;

public class MissingFloatingHoliday {
	private int id;
	private String userId;
	private String date;
	private String name;
	private String description;
	private ReportState reportState;
	private Integer holidayId;

	public MissingFloatingHoliday(final int id, final String userId, final String date, final String name, final String description, final ReportState reportState, final Integer holidayId) {
		this.id = id;
		this.userId = userId;
		this.date = date;
		this.name = name;
		this.description = description;
		this.reportState = reportState;
		this.holidayId = holidayId;
	}

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(final String userId) {
		this.userId = userId;
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

	public ReportState getReportState() {
		return reportState;
	}

	public void setReportState(final ReportState reportState) {
		this.reportState = reportState;
	}

	public Integer getHolidayId() {
		return holidayId;
	}

	public void setHolidayId(final int holidayId) {
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
		final MissingFloatingHoliday that = (MissingFloatingHoliday) o;
		return id == that.id
				&& Objects.equals(date, that.date)
				&& Objects.equals(userId, that.userId)
				&& Objects.equals(name, that.name)
				&& Objects.equals(description, that.description)
				&& reportState == that.reportState
				&& Objects.equals(holidayId, that.holidayId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, userId, date, name, description, reportState, holidayId);
	}

	@NonNull
	@Override
	public String toString() {
		return "MissingFixedHoliday{" +
				"id=" + id +
				", userId='" + userId + '\'' +
				", date='" + date + '\'' +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				", reportState=" + reportState +
				", holidayId=" + holidayId +
				'}';
	}
}
