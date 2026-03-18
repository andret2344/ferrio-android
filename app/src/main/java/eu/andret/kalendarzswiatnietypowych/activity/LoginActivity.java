package eu.andret.kalendarzswiatnietypowych.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import eu.andret.kalendarzswiatnietypowych.FerrioApplication;
import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.util.auth.AuthUiState;
import eu.andret.kalendarzswiatnietypowych.util.auth.AuthViewModel;

public class LoginActivity extends AppCompatActivity {

	private static final String TAG = "Ferrio-LoginActivity";


	private AuthViewModel authViewModel;
	private RelativeLayout progress;
	private CancellationSignal cancellationSignal;

	private CredentialManager credentialManager;
	private ExecutorService executorService;

	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		EdgeToEdge.enable(this);
		final AlertDialog offlineDialog;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		progress = findViewById(R.id.activity_login_layout_progress);
		offlineDialog = new MaterialAlertDialogBuilder(this)
				.setTitle(R.string.no_internet_connection)
				.setMessage(R.string.no_internet)
				.setCancelable(false)
				.create();

		authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

		authViewModel.getUi().observe(this, this::render);

		authViewModel.isOnline().observe(this, online -> {
			if (Boolean.TRUE.equals(online)) {
				if (offlineDialog.isShowing()) {
					offlineDialog.dismiss();
				}
			} else {
				if (!offlineDialog.isShowing()) {
					offlineDialog.show();
				}
			}
		});

		authViewModel.getLaunchGoogleEvent().observe(this, launch -> {
			if (Boolean.TRUE.equals(launch)) {
				authViewModel.consumeGoogleEvent();
				beginGoogleSignIn();
			}
		});

		findViewById(R.id.activity_login_sign_in_google).setOnClickListener(v -> authViewModel.clickGoogle());
		final View anonymousButton = findViewById(R.id.activity_login_sign_in_anonymous);
		anonymousButton.setOnClickListener(v ->
				new MaterialAlertDialogBuilder(this)
						.setMessage(R.string.anonymous_login_alert)
						.setPositiveButton(R.string.ok, (dialog, which) -> authViewModel.signInAnonymously())
						.setNegativeButton(android.R.string.cancel, null)
						.show());
		final TextView moreOptions = findViewById(R.id.activity_login_more_options);
		moreOptions.setOnClickListener(v -> {
			final boolean expanding = anonymousButton.getVisibility() != View.VISIBLE;
			final int chevron = expanding ? R.drawable.baseline_expand_less_24 : R.drawable.baseline_expand_more_24;
			anonymousButton.setVisibility(expanding ? View.VISIBLE : View.GONE);
			moreOptions.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, chevron, 0);
		});

		credentialManager = CredentialManager.create(this);
		executorService = Executors.newSingleThreadExecutor();
	}

	@Override
	protected void onStart() {
		super.onStart();
		authViewModel.start();
	}

	private void beginGoogleSignIn() {
		final String clientId = getString(R.string.default_web_client_id);

		final GetGoogleIdOption googleOption = new GetGoogleIdOption.Builder()
				.setServerClientId(clientId)
				.build();

		final GetCredentialRequest request = new GetCredentialRequest.Builder()
				.addCredentialOption(googleOption)
				.build();

		progress.setVisibility(View.VISIBLE);
		cancellationSignal = new CancellationSignal();

		credentialManager.getCredentialAsync(
				this,
				request,
				cancellationSignal,
				executorService,
				new CredentialManagerCallback<>() {
					@Override
					public void onResult(final GetCredentialResponse response) {
						handleCredentialResponse(response);
					}

					@Override
					public void onError(@NonNull final GetCredentialException e) {
						Log.w(TAG, "GetGoogleIdOption failed, falling back to sign-in flow", e);
						progress.post(() -> beginGoogleSignInFallback(clientId));
					}
				}
		);
	}

	private void beginGoogleSignInFallback(@NonNull final String clientId) {
		final GetSignInWithGoogleOption signInOption = new GetSignInWithGoogleOption.Builder(clientId)
				.build();

		final GetCredentialRequest request = new GetCredentialRequest.Builder()
				.addCredentialOption(signInOption)
				.build();

		cancellationSignal = new CancellationSignal();

		credentialManager.getCredentialAsync(
				this,
				request,
				cancellationSignal,
				executorService,
				new CredentialManagerCallback<>() {
					@Override
					public void onResult(final GetCredentialResponse response) {
						handleCredentialResponse(response);
					}

					@Override
					public void onError(@NonNull final GetCredentialException e) {
						progress.post(() -> {
							progress.setVisibility(View.GONE);
							Log.e(TAG, "Sign-in fallback failed", e);
							authViewModel.reportError(e.getMessage() != null ? e.getMessage() : getString(R.string.auth_failed));
						});
					}
				}
		);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (cancellationSignal != null) {
			cancellationSignal.cancel();
			cancellationSignal = null;
		}
		if (executorService != null) {
			executorService.shutdownNow();
		}
	}

	private void handleCredentialResponse(@NonNull final GetCredentialResponse response) {
		final Credential credential = response.getCredential();
		if (credential instanceof CustomCredential) {
			final CustomCredential custom = (CustomCredential) credential;
			if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(custom.getType())) {
				final GoogleIdTokenCredential googleCred = GoogleIdTokenCredential.createFrom(custom.getData());
				final String idToken = googleCred.getIdToken();
				if (!idToken.isEmpty()) {
					authViewModel.handleGoogleIdToken(idToken);
					return;
				}
				authViewModel.reportError(getString(R.string.auth_failed));
			} else {
				authViewModel.reportError(getString(R.string.auth_failed));
			}
		} else {
			authViewModel.reportError(getString(R.string.auth_failed));
		}
		progress.post(() -> progress.setVisibility(View.GONE));
	}

	private void render(final AuthUiState s) {
		progress.setVisibility(s.loading ? View.VISIBLE : View.GONE);
		if (s.errorMessage != null) {
			Toast.makeText(this, s.errorMessage, Toast.LENGTH_SHORT).show();
		}
		if (!s.loading && s.user != null) {
			FerrioApplication.refreshWidgets(this);
			startActivity(new Intent(this, MainActivity.class));
			finish();
		}
	}
}
