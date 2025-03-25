package eu.andret.kalendarzswiatnietypowych.activity;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.util.Util;

public class LoginActivity extends AppCompatActivity {
	private FirebaseAuth firebaseAuth;
	private MutableLiveData<Boolean> internet;
	private AlertDialog alertDialog;
	private RelativeLayout progressLayout;
	private final ActivityResultLauncher<Intent> activityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
		final Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
		try {
			final GoogleSignInAccount account = task.getResult(ApiException.class);
			firebaseSignIn(GoogleAuthProvider.getCredential(account.getIdToken(), null));
		} catch (final ApiException e) {
			progressLayout.setVisibility(View.GONE);
			Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	});

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		progressLayout = findViewById(R.id.activity_login_layout_progress);

		alertDialog = new MaterialAlertDialogBuilder(this)
				.setTitle(R.string.no_internet_connection)
				.setCancelable(false)
				.setMessage(R.string.no_internet)
				.create();

		configureObservers();

		final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestIdToken(getString(R.string.default_web_client_id))
				.requestEmail()
				.build();

		final GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
		firebaseAuth = FirebaseAuth.getInstance();

		final SignInButton signInButton = findViewById(R.id.activity_login_sign_in_google);
		signInButton.setOnClickListener(view -> {
			progressLayout.setVisibility(View.VISIBLE);
			activityResult.launch(googleSignInClient.getSignInIntent());
		});

		final MaterialButton materialButton = findViewById(R.id.activity_login_sign_in_anonymous);
		materialButton.setOnClickListener(v ->
				firebaseAuth.signInAnonymously().addOnCompleteListener(this, this::handleTask));
	}

	@Override
	protected void onStart() {
		super.onStart();

		final FirebaseUser user = firebaseAuth.getCurrentUser();
		if (user == null) {
			return;
		}
		progressLayout.setVisibility(View.VISIBLE);
		if (user.isAnonymous()) {
			navigateToMainActivity();
			return;
		}
		user.getIdToken(true).addOnCompleteListener(this::handleTask);
	}

	private void firebaseSignIn(@NonNull final AuthCredential credential) {
		firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, this::handleTask);
	}

	private <T> void handleTask(@NonNull final Task<T> task) {
		if (task.isSuccessful()) {
			navigateToMainActivity();
		} else {
			if (task.getException() instanceof FirebaseNetworkException) {
				alertDialog.show();
			} else {
				Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
				task.getException().printStackTrace();
			}
			progressLayout.setVisibility(View.GONE);
		}
	}

	private void navigateToMainActivity() {
		final Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra(MainActivity.INTERNET, internet.getValue());
		startActivity(intent);
		finish();
	}

	private void configureObservers() {
		internet = new MutableLiveData<>(Util.isNetworkAvailable(this));
		internet.observe(this, isConnected -> {
			if (Boolean.TRUE.equals(isConnected)) {
				alertDialog.dismiss();
			} else {
				alertDialog.show();
			}
		});
		final ConnectivityManager connectivityManager =
				(ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		Util.NETWORK_CAPABILITIES.stream()
				.map(new NetworkRequest.Builder()::addTransportType)
				.map(NetworkRequest.Builder::build)
				.forEach(request -> connectivityManager.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback() {
					@Override
					public void onAvailable(@NonNull final Network network) {
						super.onAvailable(network);
						internet.postValue(true);
					}

					@Override
					public void onLost(@NonNull final Network network) {
						super.onLost(network);
						internet.postValue(false);
					}
				}));
	}
}
