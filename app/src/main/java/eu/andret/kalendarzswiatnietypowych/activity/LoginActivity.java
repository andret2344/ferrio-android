package eu.andret.kalendarzswiatnietypowych.activity;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.persistance.UpdateDataWorker;
import eu.andret.kalendarzswiatnietypowych.util.Util;

public class LoginActivity extends AppCompatActivity {
	private GoogleSignInClient googleSignInClient;
	private FirebaseAuth firebaseAuth;
	private MutableLiveData<Boolean> internet;
	private AlertDialog alertDialog;

	private final ActivityResultLauncher<Intent> activityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
		if (result.getResultCode() == RESULT_OK) {
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

		alertDialog = new MaterialAlertDialogBuilder(this)
				.setTitle(R.string.no_internet_connection)
				.setCancelable(true)
				.setMessage(R.string.no_internet)
				.setPositiveButton(R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss())
				.create();

		configureObservers();

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
			if (task.getException() instanceof FirebaseNetworkException) {
				alertDialog.show();
			} else {
				Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
			}
			updateUI(null);
		}
	}

	private void updateUI(@Nullable final FirebaseUser user) {
		if (user != null) {
			final Intent intent = new Intent(this, MainActivity.class);
			intent.putExtra(MainActivity.INTERNET, internet.getValue());
			startActivity(intent);
			finish();
		}
	}

	private void configureObservers() {
		internet = new MutableLiveData<>(Util.isNetworkAvailable(this));
		internet.observe(this, isConnected -> {
			if (Boolean.TRUE.equals(isConnected) && alertDialog != null) {
				final WorkRequest updateDataRequest = new OneTimeWorkRequest.Builder(UpdateDataWorker.class).build();
				WorkManager.getInstance(this).enqueue(updateDataRequest);
				alertDialog.dismiss();
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
