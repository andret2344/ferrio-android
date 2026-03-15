package eu.andret.kalendarzswiatnietypowych.util.auth;

import java.util.concurrent.atomic.AtomicBoolean;

public final class AuthEvent<T> {
	private final T content;
	private final AtomicBoolean handled = new AtomicBoolean(false);

	public AuthEvent(final T content) {
		this.content = content;
	}

	public T getContentIfNotHandled() {
		return handled.compareAndSet(false, true) ? content : null;
	}

	public T peek() {
		return content;
	}
}
