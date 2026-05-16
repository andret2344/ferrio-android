package eu.andret.kalendarzswiatnietypowych.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayoutMediator;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.adapter.CustomFragmentAdapter;
import eu.andret.kalendarzswiatnietypowych.databinding.ActivitySuggestionBinding;
import eu.andret.kalendarzswiatnietypowych.fragment.FixedSuggestionFragment;
import eu.andret.kalendarzswiatnietypowych.fragment.FloatingSuggestionFragment;
import eu.andret.kalendarzswiatnietypowych.util.ReviewHelper;

public class SuggestionActivity extends BaseActivity implements FormResultHandler {

	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final ActivitySuggestionBinding binding = ActivitySuggestionBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		setSupportActionBar(binding.activitySuggestionToolbar);
		retrieveSupportActionBar().ifPresent(actionBar ->
				actionBar.setDisplayHomeAsUpEnabled(true));

		final CustomFragmentAdapter adapter = new CustomFragmentAdapter(this);
		adapter.addFragment(FixedSuggestionFragment::newInstance);
		adapter.addFragment(FloatingSuggestionFragment::newInstance);
		binding.activitySuggestionViewPager.setAdapter(adapter);

		new TabLayoutMediator(binding.activitySuggestionTabLayout, binding.activitySuggestionViewPager, (tab, position) ->
				tab.setText(position == 0 ? R.string.fixed : R.string.floating)).attach();

		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
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

	@Override
	public void showSuccessDialog() {
		new MaterialAlertDialogBuilder(this)
				.setTitle(R.string.suggestion_title)
				.setMessage(R.string.suggestion_message)
				.setPositiveButton(R.string.ok, (dialog, which) ->
						ReviewHelper.requestReview(this, this::finish))
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
