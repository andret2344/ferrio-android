package eu.andret.kalendarzswiatnietypowych.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.adapter.CustomFragmentAdapter;
import eu.andret.kalendarzswiatnietypowych.fragment.SuggestionsFixedFragment;
import eu.andret.kalendarzswiatnietypowych.fragment.SuggestionsFloatingFragment;

public class SuggestionsActivity extends BaseActivity {

	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_suggestions);

		final MaterialToolbar materialToolbar = findViewById(R.id.activity_suggestions_toolbar);
		setSupportActionBar(materialToolbar);
		retrieveSupportActionBar().ifPresent(actionBar ->
				actionBar.setDisplayHomeAsUpEnabled(true));

		final TabLayout tabLayout = findViewById(R.id.activity_suggestions_tab_layout);
		final ViewPager2 viewPager2 = findViewById(R.id.activity_suggestions_view_pager);

		final CustomFragmentAdapter adapter = new CustomFragmentAdapter(this);
		adapter.addFragment(SuggestionsFixedFragment::newInstance);
		adapter.addFragment(SuggestionsFloatingFragment::newInstance);
		viewPager2.setAdapter(adapter);

		new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
			switch (position) {
				case 0:
					tab.setText(R.string.fixed);
					break;
				case 1:
					tab.setText(R.string.floating);
					break;
				default:
					break;
			}
		}).attach();

		getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				finish();
			}
		});
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
