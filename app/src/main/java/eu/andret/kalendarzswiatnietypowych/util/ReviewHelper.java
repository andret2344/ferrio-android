package eu.andret.kalendarzswiatnietypowych.util;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

public final class ReviewHelper {
	private static final String TAG = "Ferrio-Review";

	private ReviewHelper() {
	}

	public static void requestReview(@NonNull final Activity activity, @NonNull final Runnable onComplete) {
		final ReviewManager manager = ReviewManagerFactory.create(activity);
		manager.requestReviewFlow().addOnCompleteListener(request -> {
			if (request.isSuccessful()) {
				manager.launchReviewFlow(activity, request.getResult())
						.addOnCompleteListener(flow -> onComplete.run());
			} else {
				Log.w(TAG, "Failed to request in-app review flow", request.getException());
				onComplete.run();
			}
		});
	}
}
