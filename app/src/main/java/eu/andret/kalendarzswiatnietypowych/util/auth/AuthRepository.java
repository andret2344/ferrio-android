package eu.andret.kalendarzswiatnietypowych.util.auth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import eu.andret.kalendarzswiatnietypowych.util.TasksExt;

public final class AuthRepository {

	private final FirebaseAuth auth;

	public AuthRepository() {
		auth = FirebaseAuth.getInstance();
	}

	public Result<FirebaseUser> exchangeGoogleIdTokenBlocking(@NonNull final String idToken) {
		try {
			final AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
			final Task<AuthResult> t = auth.signInWithCredential(credential);
			final AuthResult res = TasksExt.await(t);
			return Result.success(res.getUser());
		} catch (final Throwable e) {
			return Result.error(e);
		}
	}

	public Result<FirebaseUser> signInAnonymouslyBlocking() {
		try {
			final Task<AuthResult> t = auth.signInAnonymously();
			final AuthResult res = TasksExt.await(t);
			return Result.success(res.getUser());
		} catch (final Throwable e) {
			return Result.error(e);
		}
	}

	public Result<FirebaseUser> reloadAndGetCurrentUserBlocking() {
		final FirebaseUser current = auth.getCurrentUser();
		if (current == null) {
			return Result.error(new IllegalStateException("No authenticated user"));
		}
		try {
			final Task<Void> t = current.reload();
			TasksExt.await(t);
			return Result.success(auth.getCurrentUser());
		} catch (final Throwable e) {
			return Result.error(e);
		}
	}

	public boolean isNetworkError(@Nullable final Throwable e) {
		return e instanceof FirebaseNetworkException;
	}

	@Nullable
	public FirebaseUser getCurrentUser() {
		return auth.getCurrentUser();
	}
}
