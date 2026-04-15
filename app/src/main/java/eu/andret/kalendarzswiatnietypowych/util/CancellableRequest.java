package eu.andret.kalendarzswiatnietypowych.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import okhttp3.Call;

/**
 * Handle that lets a caller cancel an in-flight OkHttp call.
 * <p>
 * The handle is created before the {@link Call} exists. The {@link ApiClient} attaches the call
 * via {@link #attach(Call)} just before executing it. If {@link #cancel()} is invoked before
 * the call is attached, the call is canceled as soon as it is attached.
 */
public final class CancellableRequest {
	@Nullable
	private Call call;
	private boolean canceled;

	public synchronized void attach(@NonNull final Call newCall) {
		if (canceled) {
			newCall.cancel();
		} else {
			call = newCall;
		}
	}

	public synchronized void cancel() {
		canceled = true;
		if (call != null) {
			call.cancel();
			call = null;
		}
	}
}
