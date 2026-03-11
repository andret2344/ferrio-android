package eu.andret.kalendarzswiatnietypowych.activity;

import androidx.annotation.NonNull;

public interface FormResultHandler {
	void showSuccessDialog();

	void showErrorDialog();

	void showBanDialog(@NonNull String reason);
}
