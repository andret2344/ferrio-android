package eu.andret.kalendarzswiatnietypowych.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import eu.andret.kalendarzswiatnietypowych.R;

public class LoginActivity extends AppCompatActivity {
	private GoogleSignInClient googleSignInClient;
	private FirebaseAuth firebaseAuth;

	private final ActivityResultLauncher<Intent> activityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
		if (result.getResultCode() == Activity.RESULT_OK) {
			final Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
			try {
				final GoogleSignInAccount account = task.getResult(ApiException.class);
				firebaseAuthWithGoogle(account.getIdToken());
			} catch (final ApiException e) {
				updateUI(null);
			}
		}
	});

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestIdToken(getString(R.string.default_web_client_id))
				.requestEmail()
				.build();

		googleSignInClient = GoogleSignIn.getClient(this, gso);
		firebaseAuth = FirebaseAuth.getInstance();

		final Button signInButton = findViewById(R.id.sign_in_button);
		signInButton.setOnClickListener(view -> activityResult.launch(googleSignInClient.getSignInIntent()));
	}
	private void firebaseAuthWithGoogle(final String idToken) {
		final AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
		firebaseAuth.signInWithCredential(credential)
				.addOnCompleteListener(this, task -> {
					if (task.isSuccessful()) {
						updateUI(firebaseAuth.getCurrentUser());
					} else {
						Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
						updateUI(null);
					}
				});
	}

	@Override
	protected void onStart() {
		super.onStart();
		updateUI(firebaseAuth.getCurrentUser());
	}

	private void updateUI(@Nullable final FirebaseUser user) {
		if (user != null) {
			final Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			finish();
		}
	}
}

