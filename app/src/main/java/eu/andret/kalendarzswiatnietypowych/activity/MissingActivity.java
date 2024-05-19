package eu.andret.kalendarzswiatnietypowych.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.fragment.MissingFixedFragment;

public class MissingActivity extends UHCActivity {
	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_missing);

		final MaterialToolbar materialToolbar = findViewById(R.id.activity_missing_toolbar);
		setSupportActionBar(materialToolbar);
		retrieveSupportActionBar().ifPresent(actionBar ->
				actionBar.setDisplayHomeAsUpEnabled(true));

		final BottomNavigationView bottomNavigationView = findViewById(R.id.activity_missing_bottom_navigation);
		bottomNavigationView.setOnItemSelectedListener(item -> {
			final Fragment selectedFragment = getFragment(item);
			if (selectedFragment == null) {
				return false;
			}
			final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.replace(R.id.activity_missing_frame_layout, selectedFragment);
			transaction.commit();
			return true;
		});

		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				finish();
			}
		});
	}

	@Nullable
	private static Fragment getFragment(@NonNull final MenuItem item) {
		if (item.getItemId() == R.id.item_1) {
			return MissingFixedFragment.newInstance();
		}
		if (item.getItemId() == R.id.item_2) {
			return MissingFixedFragment.newInstance();
		}
		return null;
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
