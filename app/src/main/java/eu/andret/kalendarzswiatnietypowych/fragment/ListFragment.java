package eu.andret.kalendarzswiatnietypowych.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import eu.andret.kalendarzswiatnietypowych.adapter.ReportedHolidayAdapter;
import eu.andret.kalendarzswiatnietypowych.adapter.SuggestionAdapter;
import eu.andret.kalendarzswiatnietypowych.databinding.FragmentListBinding;
import eu.andret.kalendarzswiatnietypowych.entity.FixedHolidaySuggestion;
import eu.andret.kalendarzswiatnietypowych.entity.FloatingHolidaySuggestion;
import eu.andret.kalendarzswiatnietypowych.entity.HolidaySuggestion;
import eu.andret.kalendarzswiatnietypowych.entity.ReportedHoliday;
import eu.andret.kalendarzswiatnietypowych.util.ApiClient;

public class ListFragment extends AuthenticatedFragment {
	private static final String ARG_REPORT_TYPE = "report_type";
	private static final String ARG_HOLIDAY_TYPE = "holiday_type";

	@Nullable
	private FragmentListBinding binding;

	@Nullable
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater,
			@Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		binding = FragmentListBinding.inflate(inflater, container, false);
		return binding.getRoot();
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

		final ListFragmentViewModel viewModel = new ViewModelProvider(this).get(ListFragmentViewModel.class);

		if (ApiClient.REPORT_TYPE_SUGGESTION.equals(reportType)) {
			if (ApiClient.HOLIDAY_TYPE_FIXED.equals(holidayType)) {
				bindSuggestions(viewModel, holidayType, FixedHolidaySuggestion.class);
			} else {
				bindSuggestions(viewModel, holidayType, FloatingHolidaySuggestion.class);
			}
		} else {
			bindReports(viewModel, holidayType);
		}
	}

	@Override
	public void onDestroyView() {
		binding = null;
		super.onDestroyView();
	}

	private <T extends HolidaySuggestion> void bindSuggestions(
			@NonNull final ListFragmentViewModel viewModel,
			@NonNull final String holidayType,
			@NonNull final Class<T> clazz) {
		final SuggestionAdapter<T> adapter = new SuggestionAdapter<>();
		final FragmentListBinding b = binding;
		if (b == null) {
			return;
		}
		b.fragmentListRecyclerView.setAdapter(adapter);
		viewModel.getData().observe(getViewLifecycleOwner(), data -> {
			@SuppressWarnings("unchecked") final List<T> typed = (List<T>) data;
			adapter.submitList(typed);
			if (binding != null) {
				binding.fragmentListIndicator.setVisibility(View.GONE);
			}
		});
		if (!viewModel.isFetched()) {
			fetchAuthenticated(
					(token, cancel) -> getApiClient().getList(getApiClient().buildReportsUrl(
									ApiClient.REPORT_TYPE_SUGGESTION, holidayType),
							token, clazz, cancel),
					viewModel::setData,
					ex -> {
						viewModel.markFetched();
						if (binding != null) {
							binding.fragmentListIndicator.setVisibility(View.GONE);
						}
						handleApiError(ex);
					});
		}
	}

	private void bindReports(@NonNull final ListFragmentViewModel viewModel,
			@NonNull final String holidayType) {
		final ReportedHolidayAdapter adapter = new ReportedHolidayAdapter(requireContext());
		final FragmentListBinding b = binding;
		if (b == null) {
			return;
		}
		b.fragmentListRecyclerView.setAdapter(adapter);
		viewModel.getData().observe(getViewLifecycleOwner(), data -> {
			@SuppressWarnings("unchecked") final List<ReportedHoliday> typed = (List<ReportedHoliday>) data;
			adapter.submitList(typed);
			if (binding != null) {
				binding.fragmentListIndicator.setVisibility(View.GONE);
			}
		});
		if (!viewModel.isFetched()) {
			fetchAuthenticated(
					(token, cancel) -> getApiClient().getList(getApiClient().buildReportsUrl(
									ApiClient.REPORT_TYPE_ERROR, holidayType),
							token, ReportedHoliday.class, cancel),
					viewModel::setData,
					ex -> {
						viewModel.markFetched();
						if (binding != null) {
							binding.fragmentListIndicator.setVisibility(View.GONE);
						}
						handleApiError(ex);
					});
		}
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
