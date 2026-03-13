package eu.andret.kalendarzswiatnietypowych.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.adapter.SuggestionFixedAdapter;
import eu.andret.kalendarzswiatnietypowych.entity.FixedHolidaySuggestion;
import eu.andret.kalendarzswiatnietypowych.util.ApiClient;

public class SuggestionsFixedFragment extends AuthenticatedFragment {
	@Nullable
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater,
			@Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_suggestions_fixed, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final RecyclerView recyclerView = view.findViewById(R.id.fragment_suggestions_fixed_list);
		final SuggestionFixedAdapter adapter = new SuggestionFixedAdapter();
		recyclerView.setAdapter(adapter);
		fetchAuthenticated(
				token -> getApiClient().getList(getApiClient().buildReportsPath(ApiClient.REPORT_TYPE_SUGGESTION, ApiClient.HOLIDAY_TYPE_FIXED), token, FixedHolidaySuggestion.class),
				fixedHolidaySuggestions -> {
					adapter.submitList(fixedHolidaySuggestions);
					view.findViewById(R.id.fragment_suggestions_fixed_indicator).setVisibility(View.GONE);
				},
				ex -> {
					view.findViewById(R.id.fragment_suggestions_fixed_indicator).setVisibility(View.GONE);
					handleApiError(ex);
				});
	}

	@NonNull
	public static SuggestionsFixedFragment newInstance() {
		return new SuggestionsFixedFragment();
	}
}
