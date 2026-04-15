package eu.andret.kalendarzswiatnietypowych.fragment;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import eu.andret.kalendarzswiatnietypowych.FerrioApplication;
import eu.andret.kalendarzswiatnietypowych.util.ApiClient;
import eu.andret.kalendarzswiatnietypowych.util.ApiException;
import eu.andret.kalendarzswiatnietypowych.util.AuthHelper;
import eu.andret.kalendarzswiatnietypowych.util.CancellableRequest;

public abstract class AuthenticatedFragment extends Fragment {
	private final List<CancellableRequest> pendingRequests = new ArrayList<>();

	@NonNull
	protected String getFirebaseToken() {
		return AuthHelper.getFirebaseToken();
	}

	@NonNull
	protected ApiClient getApiClient() {
		return AuthHelper.getApiClient(this);
	}

	protected <T> void fetchAuthenticated(
			@NonNull final FetchFunction<T> fetcher,
			@NonNull final Consumer<List<T>> onSuccess,
			@NonNull final Consumer<ApiException> onError) {
		final Activity activity = requireActivity();
		final CancellableRequest cancel = new CancellableRequest();
		pendingRequests.add(cancel);
		CompletableFuture.supplyAsync(() -> {
			try {
				return fetcher.apply(getFirebaseToken(), cancel);
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
					onError.accept(unwrapApiException(throwable));
				} else {
					onSuccess.accept(result);
				}
			});
		});
	}

	protected void submitAuthenticated(
			@NonNull final SubmitFunction submitter,
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
					onError.accept(unwrapApiException(throwable));
				} else {
					onSuccess.run();
				}
			});
		});
	}

	@NonNull
	static ApiException unwrapApiException(@NonNull final Throwable throwable) {
		Throwable cause = throwable;
		while (cause != null) {
			if (cause instanceof ApiException) {
				return (ApiException) cause;
			}
			cause = cause.getCause();
		}
		return new ApiException(0, null);
	}

	@Override
	public void onDestroyView() {
		for (final CancellableRequest cancel : pendingRequests) {
			cancel.cancel();
		}
		pendingRequests.clear();
		super.onDestroyView();
	}

	protected void handleApiError(@NonNull final ApiException ex) {
		AuthHelper.handleApiError(this, ex);
	}

	@FunctionalInterface
	protected interface FetchFunction<T> {
		List<T> apply(String token, CancellableRequest cancel) throws ApiException;
	}

	@FunctionalInterface
	protected interface SubmitFunction {
		void apply(String token, CancellableRequest cancel) throws ApiException;
	}
}
