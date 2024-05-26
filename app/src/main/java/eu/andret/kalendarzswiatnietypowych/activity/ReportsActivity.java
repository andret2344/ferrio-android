package eu.andret.kalendarzswiatnietypowych.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.adapter.SuggestionFixedAdapter;
import eu.andret.kalendarzswiatnietypowych.util.Downloader;
import java9.util.concurrent.CompletableFuture;

public class ReportsActivity extends UHCActivity {
	private FirebaseAuth firebaseAuth;
	private CompletableFuture<Void> future;

	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reports);
		firebaseAuth = FirebaseAuth.getInstance();

		final MaterialToolbar materialToolbar = findViewById(R.id.activity_reports_toolbar);
		setSupportActionBar(materialToolbar);
		retrieveSupportActionBar().ifPresent(actionBar ->
				actionBar.setDisplayHomeAsUpEnabled(true));

		final RecyclerView recyclerView = findViewById(R.id.activity_reports_list);

		future = CompletableFuture.supplyAsync(new Downloader.MissingFixedHolidaysDownloader(firebaseAuth.getUid()))
				.thenAccept(missingReport -> runOnUiThread(() -> {
					recyclerView.setAdapter(new SuggestionFixedAdapter(missingReport));
					findViewById(R.id.activity_reports_indicator).setVisibility(View.GONE);
				}));

		getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				if (future != null && !future.isDone()) {
					future.cancel(true);
				}
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
