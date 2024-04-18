package eu.andret.kalendarzswiatnietypowych.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import eu.andret.kalendarzswiatnietypowych.R;

public class LoginActivity extends AppCompatActivity {
	private GoogleSignInClient googleSignInClient;
	private CallbackManager callbackManager;
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
		final TextView textView = (TextView) signInButton.getChildAt(0);
		textView.setText("Sign in with Google");

		callbackManager = CallbackManager.Factory.create();
		final LoginButton facebookLoginButton = findViewById(R.id.activity_login_sign_in_facebook);
		facebookLoginButton.setPermissions("id", "name", "email", "public_profile");
		facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<>() {
			@Override
			public void onSuccess(@NonNull final LoginResult loginResult) {
				firebaseSignIn(FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken()));
			}

			@Override
			public void onCancel() {
				// do nothing
			}

			@Override
			public void onError(@NonNull final FacebookException error) {
				error.printStackTrace();
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		updateUI(firebaseAuth.getCurrentUser());
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		callbackManager.onActivityResult(requestCode, resultCode, data);
	}

	private void firebaseSignIn(@NonNull final AuthCredential credential) {
		firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
			if (task.isSuccessful()) {
				updateUI(firebaseAuth.getCurrentUser());
			} else {
				Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
				updateUI(null);
			}
		});
	}

	private void updateUI(@Nullable final FirebaseUser user) {
		if (user != null) {
			startActivity(new Intent(this, MainActivity.class));
			finish();
		}
	}
}

