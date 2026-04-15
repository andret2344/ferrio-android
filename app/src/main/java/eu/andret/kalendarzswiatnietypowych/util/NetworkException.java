package eu.andret.kalendarzswiatnietypowych.util;

import androidx.annotation.Nullable;

/**
 * Represents a transport-layer failure (no connectivity, DNS, TLS handshake, socket timeout,
 * etc.) as distinct from a non-2xx HTTP response. Callers can {@code instanceof}-check to show
 * an "offline" banner instead of a generic error dialog.
 */
public class NetworkException extends ApiException {
	public NetworkException(@Nullable final String message) {
		super(0, message);
	}
}
