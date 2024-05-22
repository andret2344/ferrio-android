package eu.andret.kalendarzswiatnietypowych.entity;

import androidx.annotation.NonNull;

import java.util.Objects;

public class MissingFixedHoliday {
	private int id;
	private String userId;
	private int day;
	private int month;
	private String name;
	private String description;
	private ReportState reportState;
	private String holidayId;

	public MissingFixedHoliday(final int id, final String userId, final int day, final int month, final String name, final String description, final ReportState reportState, final String holidayId) {
		this.id = id;
		this.userId = userId;
		this.day = day;
		this.month = month;
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

	public ReportState getReportState() {
		return reportState;
	}

	public void setReportState(final ReportState reportState) {
		this.reportState = reportState;
	}

	public String getHolidayId() {
		return holidayId;
	}

	public void setHolidayId(final String holidayId) {
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
		final MissingFixedHoliday that = (MissingFixedHoliday) o;
		return id == that.id
				&& day == that.day
				&& month == that.month
				&& Objects.equals(userId, that.userId)
				&& Objects.equals(name, that.name)
				&& Objects.equals(description, that.description)
				&& reportState == that.reportState
				&& Objects.equals(holidayId, that.holidayId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, userId, day, month, name, description, reportState, holidayId);
	}

	@NonNull
	@Override
	public String toString() {
		return "MissingFixedHoliday{" +
				"id=" + id +
				", userId='" + userId + '\'' +
				", day=" + day +
				", month=" + month +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				", reportState=" + reportState +
				", holidayId='" + holidayId + '\'' +
				'}';
	}
}
