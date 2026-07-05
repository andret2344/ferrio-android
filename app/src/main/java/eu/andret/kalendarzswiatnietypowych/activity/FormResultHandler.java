package eu.andret.kalendarzswiatnietypowych.activity;

import androidx.annotation.NonNull;

import eu.andret.kalendarzswiatnietypowych.util.ApiException;

public interface FormResultHandler {
	void showSuccessDialog();

	void showErrorDialog();

	void showBanDialog(@NonNull String reason);

	/**
	 * Routes a failed submission to the ban dialog when the user is banned, otherwise to the
	 * generic error dialog. Shared by the fixed/floating suggestion flows so both handle the
	 * ban/error split identically.
	 */
	default void showSubmitError(@NonNull final ApiException ex) {
		if (ex.isBanned()) {
			showBanDialog(ex.getBanReason());
		} else {
			showErrorDialog();
		}
	}
}
