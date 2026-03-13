package eu.andret.kalendarzswiatnietypowych.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.adapter.SuggestionFloatingAdapter;
import eu.andret.kalendarzswiatnietypowych.entity.FloatingHolidaySuggestion;
import eu.andret.kalendarzswiatnietypowych.util.ApiClient;

public class SuggestionsFloatingFragment extends AuthenticatedFragment {
	@Nullable
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater,
			@Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_suggestions_floating, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final RecyclerView recyclerView = view.findViewById(R.id.fragment_suggestions_floating_list);
		final SuggestionFloatingAdapter adapter = new SuggestionFloatingAdapter();
		recyclerView.setAdapter(adapter);
		fetchAuthenticated(
				token -> getApiClient().getList(getApiClient().buildReportsPath(ApiClient.REPORT_TYPE_SUGGESTION, ApiClient.HOLIDAY_TYPE_FLOATING), token, FloatingHolidaySuggestion.class),
				floatingHolidaySuggestions -> {
					adapter.submitList(floatingHolidaySuggestions);
					view.findViewById(R.id.fragment_suggestions_floating_indicator).setVisibility(View.GONE);
				},
				ex -> {
					view.findViewById(R.id.fragment_suggestions_floating_indicator).setVisibility(View.GONE);
					handleApiError(ex);
				});
	}

	@NonNull
	public static SuggestionsFloatingFragment newInstance() {
		return new SuggestionsFloatingFragment();
	}
}
