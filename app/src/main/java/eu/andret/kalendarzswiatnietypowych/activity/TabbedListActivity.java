package eu.andret.kalendarzswiatnietypowych.activity;

import android.content.Context;
import android.content.Intent;
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
import eu.andret.kalendarzswiatnietypowych.fragment.ListFragment;
import eu.andret.kalendarzswiatnietypowych.util.ApiClient;

public class TabbedListActivity extends BaseActivity {
	private static final String EXTRA_REPORT_TYPE = "report_type";

	@NonNull
	public static Intent createIntent(@NonNull final Context context,
			@NonNull final String reportType) {
		final Intent intent = new Intent(context, TabbedListActivity.class);
		intent.putExtra(EXTRA_REPORT_TYPE, reportType);
		return intent;
	}

	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tabbed_list);

		final String reportType = getIntent().getStringExtra(EXTRA_REPORT_TYPE);
		if (reportType == null) {
			finish();
			return;
		}
		final boolean isSuggestion = ApiClient.REPORT_TYPE_SUGGESTION.equals(reportType);

		final MaterialToolbar toolbar = findViewById(R.id.activity_tabbed_list_toolbar);
		toolbar.setTitle(isSuggestion ? R.string.my_suggestions : R.string.my_reports);
		setSupportActionBar(toolbar);
		retrieveSupportActionBar().ifPresent(actionBar ->
				actionBar.setDisplayHomeAsUpEnabled(true));

		final TabLayout tabLayout = findViewById(R.id.activity_tabbed_list_tab_layout);
		final ViewPager2 viewPager2 = findViewById(R.id.activity_tabbed_list_view_pager);

		final CustomFragmentAdapter adapter = new CustomFragmentAdapter(this);
		adapter.addFragment(() -> ListFragment.newInstance(reportType, ApiClient.HOLIDAY_TYPE_FIXED));
		adapter.addFragment(() -> ListFragment.newInstance(reportType, ApiClient.HOLIDAY_TYPE_FLOATING));
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
