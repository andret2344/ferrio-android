package eu.andret.kalendarzswiatnietypowych.entity;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Objects;

public class MissingReport {
	private final List<MissingFixedHoliday> fixed;

	public MissingReport(final List<MissingFixedHoliday> fixed) {
		this.fixed = fixed;
	}

	public List<MissingFixedHoliday> getFixed() {
		return fixed;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final MissingReport that = (MissingReport) o;
		return Objects.equals(fixed, that.fixed);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fixed);
	}

	@NonNull
	@Override
	public String toString() {
		return "MissingReport{" +
				"fixed=" + fixed +
				'}';
	}
}
