package eu.andret.kalendarzswiatnietypowych.fragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import eu.andret.kalendarzswiatnietypowych.util.ApiClient;
import eu.andret.kalendarzswiatnietypowych.util.ApiException;
import eu.andret.kalendarzswiatnietypowych.util.AuthHelper;

public abstract class AuthenticatedDialogFragment extends DialogFragment {

	@NonNull
	protected String getFirebaseToken() {
		return AuthHelper.getFirebaseToken();
	}

	@NonNull
	protected ApiClient getApiClient() {
		return AuthHelper.getApiClient(this);
	}

	protected void showBanDialog(@NonNull final String reason) {
		AuthHelper.showBanDialog(this, reason);
	}

	protected void handleApiError(@NonNull final ApiException ex) {
		AuthHelper.handleApiError(this, ex);
	}
}
