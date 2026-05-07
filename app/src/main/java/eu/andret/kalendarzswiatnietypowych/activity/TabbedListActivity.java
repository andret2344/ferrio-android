package eu.andret.kalendarzswiatnietypowych.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.tabs.TabLayoutMediator;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.adapter.CustomFragmentAdapter;
import eu.andret.kalendarzswiatnietypowych.databinding.ActivityTabbedListBinding;
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
		final ActivityTabbedListBinding binding = ActivityTabbedListBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		final String reportType = getIntent().getStringExtra(EXTRA_REPORT_TYPE);
		if (reportType == null) {
			finish();
			return;
		}
		final boolean isSuggestion = ApiClient.REPORT_TYPE_SUGGESTION.equals(reportType);

		binding.activityTabbedListToolbar.setTitle(isSuggestion ? R.string.my_suggestions : R.string.my_reports);
		setSupportActionBar(binding.activityTabbedListToolbar);
		retrieveSupportActionBar().ifPresent(actionBar ->
				actionBar.setDisplayHomeAsUpEnabled(true));

		final CustomFragmentAdapter adapter = new CustomFragmentAdapter(this);
		adapter.addFragment(() -> ListFragment.newInstance(reportType, ApiClient.HOLIDAY_TYPE_FIXED));
		adapter.addFragment(() -> ListFragment.newInstance(reportType, ApiClient.HOLIDAY_TYPE_FLOATING));
		binding.activityTabbedListViewPager.setAdapter(adapter);

		new TabLayoutMediator(binding.activityTabbedListTabLayout, binding.activityTabbedListViewPager, (tab, position) -> {
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
