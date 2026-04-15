package eu.andret.kalendarzswiatnietypowych.util.auth;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

import eu.andret.kalendarzswiatnietypowych.util.Util;

/**
 * Thin facade over {@link FirebaseAuth} that centralizes the "can this user do X" checks the UI
 * repeats. Keeps the rule "suggestions/reports require a non-anonymous user" in one place so any
 * future change (e.g., feature-flagging it on or gating by email verification) lands in one edit.
 */
public final class AuthSession {

	private AuthSession() {
	}

	@NonNull
	public static Optional<FirebaseUser> getCurrentUser() {
		return Optional.ofNullable(FirebaseAuth.getInstance().getCurrentUser());
	}

	public static boolean isSignedIn() {
		return getCurrentUser().isPresent();
	}

	public static boolean isAnonymous() {
		return getCurrentUser()
				.map(FirebaseUser::isAnonymous)
				.orElse(false);
	}

	/**
	 * Anonymous users may browse and search, but cannot submit user-authored content (holiday
	 * suggestions, error reports) or view their own submission history.
	 */
	public static boolean canSubmitUserContent() {
		return getCurrentUser()
				.filter(Predicate.not(FirebaseUser::isAnonymous))
				.isPresent();
	}

	@Nullable
	public static String displayName() {
		return getCurrentUser()
				.map(FirebaseUser::getDisplayName)
				.orElse(null);
	}

	@Nullable
	public static String email() {
		return getCurrentUser()
				.map(FirebaseUser::getEmail)
				.orElse(null);
	}

	/**
	 * Returns the URL of the avatar to display for the current user: Google photo for signed-in
	 * accounts, a deterministic Gravatar identicon for anonymous ones, or {@code null} if nobody
	 * is signed in.
	 */
	@Nullable
	public static String avatarUrl() {
		final FirebaseUser user = getCurrentUser().orElse(null);
		if (user == null) {
			return null;
		}
		if (user.isAnonymous()) {
			return String.format(Locale.ROOT, "https://gravatar.com/avatar/%s?d=identicon", Util.sha256(user.getUid()));
		}
		return Optional.ofNullable(user.getPhotoUrl())
				.map(Uri::toString)
				.orElse(null);
	}

	@NonNull
	public static String requireUid() {
		return getCurrentUser()
				.map(FirebaseUser::getUid)
				.orElseThrow(() -> new IllegalStateException("No authenticated user"));
	}
}
