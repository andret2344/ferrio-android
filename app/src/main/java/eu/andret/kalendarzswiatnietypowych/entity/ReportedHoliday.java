package eu.andret.kalendarzswiatnietypowych.entity;

import androidx.annotation.NonNull;

import java.time.LocalDateTime;
import java.util.Objects;

public class ReportedHoliday {
	private int id;
	private String languageCode;
	private int metadataId;
	private String reportType;
	private String description;
	private LocalDateTime datetime;
	private ReportState reportState;

	public ReportedHoliday(final int id, final String languageCode, final int metadataId,
			final String reportType, final String description, final LocalDateTime datetime,
			final ReportState reportState) {
		this.id = id;
		this.languageCode = languageCode;
		this.metadataId = metadataId;
		this.reportType = reportType;
		this.description = description;
		this.datetime = datetime;
		this.reportState = reportState;
	}

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public String getLanguageCode() {
		return languageCode;
	}

	public void setLanguageCode(final String languageCode) {
		this.languageCode = languageCode;
	}

	public int getMetadataId() {
		return metadataId;
	}

	public void setMetadataId(final int metadataId) {
		this.metadataId = metadataId;
	}

	public String getReportType() {
		return reportType;
	}

	public void setReportType(final String reportType) {
		this.reportType = reportType;
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

	public ReportState getReportState() {
		return reportState;
	}

	public void setReportState(final ReportState reportState) {
		this.reportState = reportState;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ReportedHoliday that = (ReportedHoliday) o;
		return id == that.id
				&& metadataId == that.metadataId
				&& Objects.equals(languageCode, that.languageCode)
				&& Objects.equals(reportType, that.reportType)
				&& Objects.equals(description, that.description)
				&& Objects.equals(datetime, that.datetime)
				&& reportState == that.reportState;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, languageCode, metadataId, reportType, description, datetime, reportState);
	}

	@NonNull
	@Override
	public String toString() {
		return "ReportedHoliday{" +
				"id=" + id +
				", languageCode='" + languageCode + '\'' +
				", metadataId=" + metadataId +
				", reportType='" + reportType + '\'' +
				", description='" + description + '\'' +
				", datetime=" + datetime +
				", reportState=" + reportState +
				'}';
	}
}
