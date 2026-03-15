package eu.andret.kalendarzswiatnietypowych.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.adapter.ReportedHolidayAdapter;
import eu.andret.kalendarzswiatnietypowych.adapter.SuggestionAdapter;
import eu.andret.kalendarzswiatnietypowych.entity.FixedHolidaySuggestion;
import eu.andret.kalendarzswiatnietypowych.entity.FloatingHolidaySuggestion;
import eu.andret.kalendarzswiatnietypowych.entity.HolidaySuggestion;
import eu.andret.kalendarzswiatnietypowych.entity.ReportedHoliday;
import eu.andret.kalendarzswiatnietypowych.util.ApiClient;

public class ListFragment extends AuthenticatedFragment {
	private static final String ARG_REPORT_TYPE = "report_type";
	private static final String ARG_HOLIDAY_TYPE = "holiday_type";


	@Nullable
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater,
			@Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_list, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final Bundle args = requireArguments();
		final String reportType = args.getString(ARG_REPORT_TYPE);
		final String holidayType = args.getString(ARG_HOLIDAY_TYPE);
		if (reportType == null || holidayType == null) {
			return;
		}

		final RecyclerView recyclerView = view.findViewById(R.id.fragment_list_recycler_view);
		final View indicator = view.findViewById(R.id.fragment_list_indicator);

		if (ApiClient.REPORT_TYPE_SUGGESTION.equals(reportType)) {
			if (ApiClient.HOLIDAY_TYPE_FIXED.equals(holidayType)) {
				loadSuggestions(recyclerView, indicator, holidayType, FixedHolidaySuggestion.class);
			} else {
				loadSuggestions(recyclerView, indicator, holidayType, FloatingHolidaySuggestion.class);
			}
		} else {
			loadReports(recyclerView, indicator, holidayType);
		}
	}

	private <T extends HolidaySuggestion> void loadSuggestions(@NonNull final RecyclerView recyclerView,
			@NonNull final View indicator, @NonNull final String holidayType,
			@NonNull final Class<T> clazz) {
		final SuggestionAdapter<T> adapter = new SuggestionAdapter<>();
		recyclerView.setAdapter(adapter);
		fetchAuthenticated(
				token -> getApiClient().getList(getApiClient().buildReportsPath(
								ApiClient.REPORT_TYPE_SUGGESTION, holidayType),
						token, clazz),
				result -> {
					adapter.submitList(result);
					indicator.setVisibility(View.GONE);
				},
				ex -> {
					indicator.setVisibility(View.GONE);
					handleApiError(ex);
				});
	}

	private void loadReports(@NonNull final RecyclerView recyclerView,
			@NonNull final View indicator, @NonNull final String holidayType) {
		final ReportedHolidayAdapter adapter = new ReportedHolidayAdapter(requireContext());
		recyclerView.setAdapter(adapter);
		fetchAuthenticated(
				token -> getApiClient().getList(getApiClient().buildReportsPath(
								ApiClient.REPORT_TYPE_ERROR, holidayType),
						token, ReportedHoliday.class),
				result -> {
					adapter.submitList(result);
					indicator.setVisibility(View.GONE);
				},
				ex -> {
					indicator.setVisibility(View.GONE);
					handleApiError(ex);
				});
	}

	@NonNull
	public static ListFragment newInstance(@NonNull final String reportType,
			@NonNull final String holidayType) {
		final ListFragment fragment = new ListFragment();
		final Bundle args = new Bundle();
		args.putString(ARG_REPORT_TYPE, reportType);
		args.putString(ARG_HOLIDAY_TYPE, holidayType);
		fragment.setArguments(args);
		return fragment;
	}
}
