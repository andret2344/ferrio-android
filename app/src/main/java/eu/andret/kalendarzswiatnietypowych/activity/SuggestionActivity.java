package eu.andret.kalendarzswiatnietypowych.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.fragment.FixedSuggestionFragment;
import eu.andret.kalendarzswiatnietypowych.fragment.FloatingSuggestionFragment;

public class SuggestionActivity extends BaseActivity implements FormResultHandler {
	private FirebaseAuth firebaseAuth;

	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_suggestion);
		firebaseAuth = FirebaseAuth.getInstance();

		final MaterialToolbar materialToolbar = findViewById(R.id.activity_suggestion_toolbar);
		setSupportActionBar(materialToolbar);
		retrieveSupportActionBar().ifPresent(actionBar ->
				actionBar.setDisplayHomeAsUpEnabled(true));

		final TabLayout tabLayout = findViewById(R.id.activity_suggestion_bottom_navigation);
		tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(final TabLayout.Tab tab) {
				final Fragment selectedFragment = getFragment(tab.getPosition());
				showFragment(selectedFragment);
			}

			@Override
			public void onTabUnselected(final TabLayout.Tab tab) {
				// empty
			}

			@Override
			public void onTabReselected(final TabLayout.Tab tab) {
				// empty
			}
		});

		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				finish();
			}
		});

		showFragment(FixedSuggestionFragment.newInstance());
	}

	private void showFragment(@Nullable final Fragment selectedFragment) {
		if (selectedFragment == null) {
			return;
		}
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.activity_suggestion_frame_layout, selectedFragment)
				.commit();
	}

	@Nullable
	private Fragment getFragment(final int itemId) {
		switch (itemId) {
			case 0:
				return FixedSuggestionFragment.newInstance();
			case 1:
				return FloatingSuggestionFragment.newInstance();
			default:
				return null;
		}
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			getOnBackPressedDispatcher().onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void showSuccessDialog() {
		new MaterialAlertDialogBuilder(this)
				.setTitle(R.string.suggestion_title)
				.setMessage(R.string.suggestion_message)
				.setPositiveButton(R.string.ok, (dialog, which) -> finish())
				.create()
				.show();
	}

	@Override
	public void showErrorDialog() {
		new MaterialAlertDialogBuilder(this)
				.setTitle(R.string.error_title)
				.setMessage(R.string.error_message)
				.setPositiveButton(R.string.ok, null)
				.create()
				.show();
	}

	@Override
	public void showBanDialog(@NonNull final String reason) {
		new MaterialAlertDialogBuilder(this)
				.setTitle(R.string.ban_title)
				.setMessage(getString(R.string.ban_message, reason))
				.setPositiveButton(R.string.ok, (dialog, which) -> finish())
				.create()
				.show();
	}
}
