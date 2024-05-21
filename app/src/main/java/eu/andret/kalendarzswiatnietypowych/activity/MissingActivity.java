package eu.andret.kalendarzswiatnietypowych.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.fragment.MissingFixedFragment;
import eu.andret.kalendarzswiatnietypowych.fragment.MissingFloatingFragment;

public class MissingActivity extends UHCActivity {
	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_missing);

		final MaterialToolbar materialToolbar = findViewById(R.id.activity_missing_toolbar);
		setSupportActionBar(materialToolbar);
		retrieveSupportActionBar().ifPresent(actionBar ->
				actionBar.setDisplayHomeAsUpEnabled(true));

		final TabLayout tabLayout = findViewById(R.id.activity_missing_bottom_navigation);
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

		showFragment(MissingFixedFragment.newInstance());
	}

	private void showFragment(@Nullable final Fragment selectedFragment) {
		if (selectedFragment == null) {
			return;
		}
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.activity_missing_frame_layout, selectedFragment)
				.commit();
	}

	@Nullable
	private static Fragment getFragment(final int itemId) {
		switch (itemId) {
			case 0:
				return MissingFixedFragment.newInstance();
			case 1:
				return MissingFloatingFragment.newInstance();
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
}
