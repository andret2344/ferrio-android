package eu.andret.kalendarzswiatnietypowych.util.auth;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;

public final class AuthUiState {
	public final boolean loading;
	public final boolean offline;
	public final FirebaseUser user;
	public final String errorMessage;

	public AuthUiState(final boolean loading, final boolean offline,
			@Nullable final FirebaseUser user, @Nullable final String errorMessage) {
		this.loading = loading;
		this.offline = offline;
		this.user = user;
		this.errorMessage = errorMessage;
	}

	public static AuthUiState idle(final boolean offline, @Nullable final FirebaseUser user) {
		return new AuthUiState(false, offline, user, null);
	}

	public static AuthUiState progress(final boolean offline) {
		return new AuthUiState(true, offline, null, null);
	}

	public static AuthUiState error(final boolean offline, final String msg) {
		return new AuthUiState(false, offline, null, msg);
	}
}
