package eu.andret.kalendarzswiatnietypowych.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.util.ApiClient;
import eu.andret.kalendarzswiatnietypowych.adapter.ReportedHolidayAdapter;
import eu.andret.kalendarzswiatnietypowych.entity.ReportedHoliday;

public class ReportsFloatingFragment extends AuthenticatedFragment {
	@Nullable
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_reports_floating, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final RecyclerView recyclerView = view.findViewById(R.id.fragment_reports_floating_list);
		final ReportedHolidayAdapter adapter = new ReportedHolidayAdapter(requireContext());
		recyclerView.setAdapter(adapter);
		fetchAuthenticated(
				token -> getApiClient().getList(getApiClient().buildReportsPath(ApiClient.REPORT_TYPE_ERROR, ApiClient.HOLIDAY_TYPE_FLOATING), token, ReportedHoliday.class),
				reportedFloatingHolidays -> {
					adapter.submitList(reportedFloatingHolidays);
					view.findViewById(R.id.fragment_reports_floating_indicator).setVisibility(View.GONE);
				},
				ex -> {
					view.findViewById(R.id.fragment_reports_floating_indicator).setVisibility(View.GONE);
					handleApiError(ex);
				});
	}

	@NonNull
	public static ReportsFloatingFragment newInstance() {
		return new ReportsFloatingFragment();
	}
}
