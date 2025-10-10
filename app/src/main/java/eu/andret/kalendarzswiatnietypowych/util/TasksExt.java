package eu.andret.kalendarzswiatnietypowych.util;

import com.google.android.gms.tasks.Task;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class TasksExt {
	private TasksExt() {
	}

	public static <T> T await(final Task<T> task) throws Exception {
		// If already completed, return or throw right away
		if (task.isComplete()) {
			if (task.isSuccessful()) {
				return task.getResult();
			}
			throw task.getException() != null ? task.getException() : new Exception("Unknown task error");
		}

		// Otherwise block the current thread until the task completes
		final CountDownLatch latch = new CountDownLatch(1);
		task.addOnCompleteListener(t -> latch.countDown());

		// wait up to 30 seconds (customize if needed)
		if (!latch.await(30, TimeUnit.SECONDS)) {
			throw new Exception("Firebase task timeout");
		}

		if (task.isSuccessful()) {
			return task.getResult();
		}
		throw task.getException() != null ? task.getException() : new Exception("Unknown task error");
	}
}
