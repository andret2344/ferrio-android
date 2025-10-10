package eu.andret.kalendarzswiatnietypowych.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.util.auth.AuthUiState;
import eu.andret.kalendarzswiatnietypowych.util.auth.AuthViewModel;

public class LoginActivity extends AppCompatActivity {

	public static final String INTERNET = "INTERNET";

	private AuthViewModel authViewModel;
	private RelativeLayout progress;
	private AlertDialog offlineDialog;
	private CancellationSignal cancellationSignal;

	private CredentialManager credentialManager;
	private Executor executor;

	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
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
				beginGoogleSignIn();
			}
		});

		findViewById(R.id.activity_login_sign_in_google).setOnClickListener(v -> authViewModel.clickGoogle());
		findViewById(R.id.activity_login_sign_in_anonymous).setOnClickListener(v -> authViewModel.signInAnonymously());

		credentialManager = CredentialManager.create(this);
		executor = Executors.newSingleThreadExecutor();
	}

	@Override
	protected void onStart() {
		super.onStart();
		authViewModel.start();
	}

	private void beginGoogleSignIn() {
		Log.d("Ferrio-default_web_client_id", getString(R.string.default_web_client_id));
		final GetGoogleIdOption googleOption = new GetGoogleIdOption.Builder()
				.setServerClientId(getString(R.string.default_web_client_id))
				.build();

		final GetCredentialRequest request = new GetCredentialRequest.Builder()
				.addCredentialOption(googleOption)
//				.setPreferImmediatelyAvailableCredentials(true)
				.build();

		progress.setVisibility(View.VISIBLE);

		cancellationSignal = new CancellationSignal();

		credentialManager.getCredentialAsync(
				this,
				request,
				cancellationSignal,
				executor,
				new CredentialManagerCallback<>() {
					@Override
					public void onResult(final GetCredentialResponse response) {
						handleCredentialResponse(response);
					}

					@Override
					public void onError(@NonNull final GetCredentialException e) {
						progress.post(() -> {
							progress.setVisibility(View.GONE);
							Log.d("Ferrio-LoginActivity-1", "error=" + e.getMessage());
							e.printStackTrace();
							authViewModel.reportError(e.getMessage() != null ? e.getMessage() : "Authentication failed");
						});
					}
				}
		);
	}

	private void handleCredentialResponse(@NonNull final GetCredentialResponse response) {
		final Credential credential = response.getCredential();
		if (credential instanceof CustomCredential) {
			final CustomCredential custom = (CustomCredential) credential;
			if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(custom.getType())) {
				try {
					final GoogleIdTokenCredential googleCred = GoogleIdTokenCredential.createFrom(custom.getData());
					final String idToken = googleCred.getIdToken();
					if (idToken.isEmpty()) {
						throw new IllegalStateException("Missing Google ID token");
					}
					authViewModel.handleGoogleIdToken(idToken);
					return;
				} catch (final Throwable e) {
					Log.d("Ferrio-LoginActivity-2", "error=" + e.getMessage());
					e.printStackTrace();
					authViewModel.reportError(e.getMessage() != null ? e.getMessage() : "Authentication failed");
				}
			} else {
				authViewModel.reportError("Authentication failed");
			}
		} else {
			authViewModel.reportError("Authentication failed");
		}
		progress.post(() -> progress.setVisibility(View.GONE));
	}

	private void render(final AuthUiState s) {
		progress.setVisibility(s.loading ? View.VISIBLE : View.GONE);
		if (s.errorMessage != null) {
			Toast.makeText(this, s.errorMessage, Toast.LENGTH_SHORT).show();
		}
		if (!s.loading && s.user != null) {
			startActivity(new Intent(this, MainActivity.class)
					.putExtra(INTERNET, !s.offline));
			finish();
		}
	}
}
