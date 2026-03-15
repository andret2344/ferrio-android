package eu.andret.kalendarzswiatnietypowych.util.auth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class Result<T> {
	public enum Status {SUCCESS, ERROR}

	public final Status status;
	public final T data;
	public final Throwable throwable;

	private Result(@NonNull final Status status, @Nullable final T data, @Nullable final Throwable throwable) {
		this.status = status;
		this.data = data;
		this.throwable = throwable;
	}

	public static <T> Result<T> success(final T data) {
		return new Result<>(Status.SUCCESS, data, null);
	}

	public static <T> Result<T> error(final Throwable throwable) {
		return new Result<>(Status.ERROR, null, throwable);
	}
}
