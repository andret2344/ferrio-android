package eu.andret.kalendarzswiatnietypowych.activity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.Locale;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.entity.MissingFixedHoliday;
import eu.andret.kalendarzswiatnietypowych.util.Downloader;
import java9.util.concurrent.CompletableFuture;

public class ReportsActivity extends AppCompatActivity {
	private FirebaseAuth firebaseAuth;

	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reports);
		firebaseAuth = FirebaseAuth.getInstance();
		final RecyclerView recyclerView = findViewById(R.id.activity_reports_list);

		CompletableFuture.supplyAsync(new Downloader.ReportsDownloader(firebaseAuth.getUid()))
				.thenAccept(missingReport -> {
					Log.d("ReportsActivity", "Missing reports: " + missingReport);
					recyclerView.setAdapter(new ReportAdapter(this, missingReport.getFixed()));
				})
				.join();
	}

	public static class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {
		private final Context context;
		private final List<MissingFixedHoliday> holidays;

		public static class ViewHolder extends RecyclerView.ViewHolder {
			private final TextView textId;

			public ViewHolder(final View view) {
				super(view);
				textId = view.findViewById(R.id.adapter_report_text_id);
			}
		}

		public ReportAdapter(final Context context, final List<MissingFixedHoliday> holidays) {
			this.context = context;
			this.holidays = holidays;
		}

		@NonNull
		@Override
		public ViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int viewType) {
			final View view = LayoutInflater.from(viewGroup.getContext())
					.inflate(R.layout.adapter_report, viewGroup, false);
			return new ViewHolder(view);
		}

		@Override
		public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position) {
			viewHolder.textId.setText(String.format(Locale.getDefault(), "#%d", holidays.get(position).getId()));
		}

		@Override
		public int getItemCount() {
			return holidays.size();
		}
	}
}
