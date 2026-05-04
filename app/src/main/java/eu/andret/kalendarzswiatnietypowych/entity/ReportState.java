package eu.andret.kalendarzswiatnietypowych.entity;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;

import eu.andret.kalendarzswiatnietypowych.R;

public enum ReportState {
	REPORTED,
	APPLIED,
	DECLINED,
	ON_HOLD,
	DUPLICATE,
	ALREADY_EXISTS,
	UNKNOWN;

	@StringRes
	public int getLabelResId() {
		switch (this) {
			case REPORTED:
				return R.string.status_reported;
			case APPLIED:
				return R.string.status_applied;
			case DECLINED:
				return R.string.status_declined;
			case ON_HOLD:
				return R.string.status_on_hold;
			case DUPLICATE:
				return R.string.status_duplicate;
			case ALREADY_EXISTS:
				return R.string.status_already_exists;
			case UNKNOWN:
				return R.string.status_unknown;
			default:
				throw new IllegalStateException("Unknown report state: " + this);
		}
	}

	@ColorRes
	public int getColorResId() {
		switch (this) {
			case REPORTED:
				return R.color.status_reported;
			case APPLIED:
				return R.color.status_applied;
			case DECLINED:
				return R.color.status_declined;
			case ON_HOLD:
				return R.color.status_on_hold;
			case DUPLICATE:
				return R.color.status_duplicate;
			case ALREADY_EXISTS:
				return R.color.status_already_exists;
			case UNKNOWN:
				return R.color.status_unknown;
			default:
				throw new IllegalStateException("Unknown report state: " + this);
		}
	}
}
