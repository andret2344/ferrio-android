package eu.andret.kalendarzswiatnietypowych.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
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
				firebaseSignIn(GoogleAuthProvider.getCredential(account.getIdToken(), null));
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

		final SignInButton signInButton = findViewById(R.id.activity_login_sign_in_google);
		signInButton.setOnClickListener(view -> activityResult.launch(googleSignInClient.getSignInIntent()));

		final MaterialButton materialButton = findViewById(R.id.activity_login_sign_in_anonymous);
		materialButton.setOnClickListener(v ->
				firebaseAuth.signInAnonymously().addOnCompleteListener(this, this::handleTask));
	}

	@Override
	protected void onStart() {
		super.onStart();
		updateUI(firebaseAuth.getCurrentUser());
	}

	private void firebaseSignIn(@NonNull final AuthCredential credential) {
		firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, this::handleTask);
	}

	private void handleTask(@NonNull final Task<AuthResult> task) {
		if (task.isSuccessful()) {
			updateUI(firebaseAuth.getCurrentUser());
		} else {
			Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
			updateUI(null);
		}
	}

	private void updateUI(@Nullable final FirebaseUser user) {
		if (user != null) {
			startActivity(new Intent(this, MainActivity.class));
			finish();
		}
	}
}
