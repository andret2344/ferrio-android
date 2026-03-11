package eu.andret.kalendarzswiatnietypowych.fragment;

import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.Tasks;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

import eu.andret.kalendarzswiatnietypowych.FerrioApplication;
import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.util.ApiClient;
import eu.andret.kalendarzswiatnietypowych.util.ApiException;

public abstract class AuthenticatedDialogFragment extends DialogFragment {

	@NonNull
	protected String getFirebaseToken() {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			throw new IllegalStateException("getFirebaseToken() must not be called on the main thread");
		}
		try {
			final String token = Tasks.await(
					FirebaseAuth.getInstance().getCurrentUser().getIdToken(false)).getToken();
			if (token == null) {
				throw new IllegalStateException("Firebase token is null");
			}
			return token;
		} catch (final Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@NonNull
	protected ApiClient getApiClient() {
		return ((FerrioApplication) requireActivity().getApplication()).getApiClient();
	}

	protected void showBanDialog(@NonNull final String reason) {
		if (!isAdded()) {
			return;
		}
		new MaterialAlertDialogBuilder(requireActivity())
				.setTitle(R.string.ban_title)
				.setMessage(getString(R.string.ban_message, reason))
				.setPositiveButton(R.string.ok, null)
				.create()
				.show();
	}

	protected void handleApiError(@NonNull final ApiException ex) {
		if (ex.isBanned()) {
			showBanDialog(ex.getBanReason());
		} else if (isAdded()) {
			new MaterialAlertDialogBuilder(requireActivity())
					.setTitle(R.string.error_title)
					.setMessage(R.string.error_message)
					.setPositiveButton(R.string.ok, null)
					.create()
					.show();
		}
	}
}
