package eu.andret.kalendarzswiatnietypowych.util;

import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Tasks;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

import eu.andret.kalendarzswiatnietypowych.FerrioApplication;
import eu.andret.kalendarzswiatnietypowych.R;

public final class AuthHelper {

	private AuthHelper() {
	}

	@NonNull
	public static String getFirebaseToken() {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			throw new IllegalStateException("getFirebaseToken() must not be called on the main thread");
		}
		try {
			final com.google.firebase.auth.FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
			if (user == null) {
				throw new IllegalStateException("No authenticated user");
			}
			final String token = Tasks.await(user.getIdToken(false)).getToken();
			if (token == null) {
				throw new IllegalStateException("Firebase token is null");
			}
			return token;
		} catch (final Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@NonNull
	public static ApiClient getApiClient(@NonNull final Fragment fragment) {
		return ((FerrioApplication) fragment.requireActivity().getApplication()).getApiClient();
	}

	public static void showBanDialog(@NonNull final Fragment fragment,
			@NonNull final String reason) {
		if (!fragment.isAdded()) {
			return;
		}
		new MaterialAlertDialogBuilder(fragment.requireActivity())
				.setTitle(R.string.ban_title)
				.setMessage(fragment.getString(R.string.ban_message, reason))
				.setPositiveButton(R.string.ok, null)
				.create()
				.show();
	}

	public static void showErrorDialog(@NonNull final Fragment fragment) {
		if (!fragment.isAdded()) {
			return;
		}
		new MaterialAlertDialogBuilder(fragment.requireActivity())
				.setTitle(R.string.error_title)
				.setMessage(R.string.error_message)
				.setPositiveButton(R.string.ok, null)
				.create()
				.show();
	}

	public static void handleApiError(@NonNull final Fragment fragment,
			@NonNull final ApiException ex) {
		if (ex.isBanned()) {
			showBanDialog(fragment, ex.getBanReason());
		} else {
			showErrorDialog(fragment);
		}
	}
}
