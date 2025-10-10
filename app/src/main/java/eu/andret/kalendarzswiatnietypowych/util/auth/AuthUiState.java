package eu.andret.kalendarzswiatnietypowych.util.auth;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;

import java.util.Optional;

public final class AuthUiState {
	public final boolean loading;
	public final boolean offline;
	public final FirebaseUser user;
	public final String errorMessage;

	public AuthUiState(final boolean loading, final boolean offline, @Nullable final FirebaseUser user, @Nullable final String errorMessage) {
		this.loading = loading;
		this.offline = offline;
		this.user = user;
		this.errorMessage = errorMessage;
		final String s = Optional.ofNullable(user).map(FirebaseUser::getEmail).orElse(null);
		Log.d("Ferrio-AuthUiState", "loading=" + loading + ", offline=" + offline + ", user=" + s + ", errorMessage=" + errorMessage);
	}

	public static AuthUiState idle(final boolean offline, @Nullable final FirebaseUser user) {
		Log.d("Ferrio-AuthUiState", "idle");
		return new AuthUiState(false, offline, user, null);
	}

	public static AuthUiState progress(final boolean offline) {
		Log.d("Ferrio-AuthUiState", "loading");
		return new AuthUiState(true, offline, null, null);
	}

	public static AuthUiState error(final boolean offline, final String msg) {
		Log.d("Ferrio-AuthUiState", "error");
		return new AuthUiState(false, offline, null, msg);
	}
}
