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
import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.fragment.FixedSuggestionFragment;
import eu.andret.kalendarzswiatnietypowych.fragment.FloatingSuggestionFragment;

public class SuggestionActivity extends BaseActivity implements FormResultHandler {

	private static final String TAG_FIXED = "fixed";
	private static final String TAG_FLOATING = "floating";

	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_suggestion);

		final MaterialToolbar materialToolbar = findViewById(R.id.activity_suggestion_toolbar);
		setSupportActionBar(materialToolbar);
		retrieveSupportActionBar().ifPresent(actionBar ->
				actionBar.setDisplayHomeAsUpEnabled(true));

		if (savedInstanceState == null) {
			final Fragment floatingFragment = FloatingSuggestionFragment.newInstance();
			getSupportFragmentManager()
					.beginTransaction()
					.add(R.id.activity_suggestion_frame_layout, FixedSuggestionFragment.newInstance(), TAG_FIXED)
					.add(R.id.activity_suggestion_frame_layout, floatingFragment, TAG_FLOATING)
					.hide(floatingFragment)
					.commit();
		}

		final TabLayout tabLayout = findViewById(R.id.activity_suggestion_bottom_navigation);
		tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(final TabLayout.Tab tab) {
				showFragment(tab.getPosition());
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
	}

	private void showFragment(final int position) {
		final Fragment fixed = getFixedFragment();
		final Fragment floating = getFloatingFragment();
		if (position == 0) {
			getSupportFragmentManager().beginTransaction().hide(floating).show(fixed).commit();
		} else {
			getSupportFragmentManager().beginTransaction().hide(fixed).show(floating).commit();
		}
	}

	@NonNull
	private Fragment getFixedFragment() {
		return getSupportFragmentManager().findFragmentByTag(TAG_FIXED);
	}

	@NonNull
	private Fragment getFloatingFragment() {
		return getSupportFragmentManager().findFragmentByTag(TAG_FLOATING);
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
