package eu.andret.kalendarzswiatnietypowych.activity;

import android.R;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import eu.andret.kalendarzswiatnietypowych.adapter.FaqAdapter;
import eu.andret.kalendarzswiatnietypowych.databinding.ActivityFaqBinding;
import eu.andret.kalendarzswiatnietypowych.util.FaqCatalog;

public class FaqActivity extends BaseActivity {

	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final ActivityFaqBinding binding = ActivityFaqBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		setSupportActionBar(binding.activityFaqToolbar);
		retrieveSupportActionBar().ifPresent(actionBar ->
				actionBar.setDisplayHomeAsUpEnabled(true));

		final FaqAdapter adapter = new FaqAdapter();
		binding.activityFaqList.setLayoutManager(new LinearLayoutManager(this));
		binding.activityFaqList.setAdapter(adapter);
		adapter.submitList(FaqCatalog.getEntries());

		getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				finish();
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
		if (item.getItemId() == R.id.home) {
			getOnBackPressedDispatcher().onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
