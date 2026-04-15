package eu.andret.kalendarzswiatnietypowych.fragment;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import eu.andret.kalendarzswiatnietypowych.FerrioApplication;
import eu.andret.kalendarzswiatnietypowych.util.ApiClient;
import eu.andret.kalendarzswiatnietypowych.util.ApiException;
import eu.andret.kalendarzswiatnietypowych.util.AuthHelper;
import eu.andret.kalendarzswiatnietypowych.util.CancellableRequest;

public abstract class AuthenticatedDialogFragment extends DialogFragment {
	private final List<CancellableRequest> pendingRequests = new ArrayList<>();

	@NonNull
	protected String getFirebaseToken() {
		return AuthHelper.getFirebaseToken();
	}

	@NonNull
	protected ApiClient getApiClient() {
		return AuthHelper.getApiClient(this);
	}

	protected void submitAuthenticated(
			@NonNull final AuthenticatedFragment.SubmitFunction submitter,
			@NonNull final Runnable onSuccess,
			@NonNull final Consumer<ApiException> onError) {
		final Activity activity = requireActivity();
		final CancellableRequest cancel = new CancellableRequest();
		pendingRequests.add(cancel);
		CompletableFuture.runAsync(() -> {
			try {
				submitter.apply(getFirebaseToken(), cancel);
			} catch (final ApiException ex) {
				throw new RuntimeException(ex);
			}
		}, FerrioApplication.IO_EXECUTOR).whenComplete((result, throwable) -> {
			if (!isAdded()) {
				return;
			}
			activity.runOnUiThread(() -> {
				if (!isAdded()) {
					return;
				}
				pendingRequests.remove(cancel);
				if (throwable != null) {
					onError.accept(AuthenticatedFragment.unwrapApiException(throwable));
				} else {
					onSuccess.run();
				}
			});
		});
	}

	@Override
	public void onDestroyView() {
		for (final CancellableRequest cancel : pendingRequests) {
			cancel.cancel();
		}
		pendingRequests.clear();
		super.onDestroyView();
	}

	protected void showBanDialog(@NonNull final String reason) {
		AuthHelper.showBanDialog(this, reason);
	}

	protected void handleApiError(@NonNull final ApiException ex) {
		AuthHelper.handleApiError(this, ex);
	}
}
