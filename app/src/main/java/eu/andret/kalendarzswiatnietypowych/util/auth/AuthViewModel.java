package eu.andret.kalendarzswiatnietypowych.util.auth;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import eu.andret.kalendarzswiatnietypowych.util.NetworkMonitor;

public final class AuthViewModel extends AndroidViewModel {
	private static final String TAG = "AuthViewModel";

	private final AuthRepository repo;
	private final NetworkMonitor network;
	private final ExecutorService io = Executors.newSingleThreadExecutor();

	private final MediatorLiveData<AuthUiState> ui = new MediatorLiveData<>();
	private final MutableLiveData<Boolean> launchGoogle = new MutableLiveData<>();

	public AuthViewModel(@NonNull final Application app) {
		super(app);
		repo = new AuthRepository();
		network = new NetworkMonitor(app);

		ui.addSource(network, online -> {
			AuthUiState current = ui.getValue();
			if (current == null) {
				current = AuthUiState.idle(!Boolean.TRUE.equals(online), repo.getCurrentUser());
			}
			ui.setValue(new AuthUiState(current.loading, !Boolean.TRUE.equals(online), current.user, current.errorMessage));
		});

		ui.setValue(AuthUiState.idle(!Boolean.TRUE.equals(network.getValue()), repo.getCurrentUser()));
	}

	public LiveData<AuthUiState> getUi() {
		return ui;
	}

	public LiveData<Boolean> isOnline() {
		return network;
	}

	public LiveData<Boolean> getLaunchGoogleEvent() {
		return launchGoogle;
	}

	private boolean isOffline() {
		final AuthUiState current = ui.getValue();
		return current != null && current.offline;
	}

	public void start() {
		final FirebaseUser current = repo.getCurrentUser();
		if (current == null) {
			return;
		}
		ui.postValue(AuthUiState.progress(isOffline()));
		io.submit(() -> {
			final Result<FirebaseUser> res = repo.reloadAndGetCurrentUserBlocking();
			if (res.status == Result.Status.SUCCESS) {
				ui.postValue(AuthUiState.idle(isOffline(), res.data));
			} else {
				Log.d(TAG, "Auth failed, status=" + res.status);
				ui.postValue(AuthUiState.error(isOffline(), mapError(res.throwable)));
			}
		});
	}

	public void clickGoogle() {
		launchGoogle.postValue(Boolean.TRUE);
	}

	public void handleGoogleIdToken(@NonNull final String idToken) {
		ui.postValue(AuthUiState.progress(isOffline()));
		io.submit(() -> {
			final Result<FirebaseUser> res = repo.exchangeGoogleIdTokenBlocking(idToken);
			if (res.status == Result.Status.SUCCESS) {
				ui.postValue(AuthUiState.idle(isOffline(), res.data));
			} else {
				Log.d(TAG, "Auth failed, status=" + res.status);
				ui.postValue(AuthUiState.error(isOffline(), mapError(res.throwable)));
			}
		});
	}

	public void signInAnonymously() {
		ui.postValue(AuthUiState.progress(isOffline()));
		io.submit(() -> {
			final Result<FirebaseUser> res = repo.signInAnonymouslyBlocking();
			if (res.status == Result.Status.SUCCESS) {
				ui.postValue(AuthUiState.idle(isOffline(), res.data));
			} else {
				Log.d(TAG, "Auth failed, status=" + res.status);
				ui.postValue(AuthUiState.error(isOffline(), mapError(res.throwable)));
			}
		});
	}

	public void reportError(@NonNull final String message) {
		Log.d(TAG, "Auth error reported");
		ui.postValue(AuthUiState.error(isOffline(), message));
	}

	private String mapError(final Throwable e) {
		if (e == null) {
			return "Authentication failed";
		}
		if (repo.isNetworkError(e)) {
			return "No internet connection";
		}
		return e.getMessage() != null ? e.getMessage() : "Authentication failed";
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		io.shutdownNow();
	}
}
