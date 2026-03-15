package eu.andret.kalendarzswiatnietypowych.fragment;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import eu.andret.kalendarzswiatnietypowych.util.ApiClient;
import eu.andret.kalendarzswiatnietypowych.util.ApiException;
import eu.andret.kalendarzswiatnietypowych.util.AuthHelper;

public abstract class AuthenticatedFragment extends Fragment {
	private final List<CompletableFuture<?>> pendingFutures = new ArrayList<>();

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
		final CompletableFuture<?> future = CompletableFuture.supplyAsync(() -> {
			try {
				return fetcher.apply(getFirebaseToken());
			} catch (final ApiException ex) {
				throw new RuntimeException(ex);
			}
		}).whenComplete((result, throwable) -> {
			if (!isAdded()) {
				return;
			}
			activity.runOnUiThread(() -> {
				if (!isAdded()) {
					return;
				}
				if (throwable != null) {
					final Throwable cause = throwable.getCause();
					if (cause instanceof ApiException) {
						onError.accept((ApiException) cause);
					} else {
						onError.accept(new ApiException(0, null));
					}
				} else {
					onSuccess.accept(result);
				}
			});
		});
		pendingFutures.add(future);
	}

	@Override
	public void onDestroyView() {
		for (final CompletableFuture<?> future : pendingFutures) {
			future.cancel(true);
		}
		pendingFutures.clear();
		super.onDestroyView();
	}

	protected void handleApiError(@NonNull final ApiException ex) {
		AuthHelper.handleApiError(this, ex);
	}

	@FunctionalInterface
	protected interface FetchFunction<T> {
		List<T> apply(String token) throws ApiException;
	}
}
