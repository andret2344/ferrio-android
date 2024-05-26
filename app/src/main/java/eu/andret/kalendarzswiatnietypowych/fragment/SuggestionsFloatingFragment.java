package eu.andret.kalendarzswiatnietypowych.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.adapter.SuggestionFloatingAdapter;
import eu.andret.kalendarzswiatnietypowych.util.Downloader;
import java9.util.concurrent.CompletableFuture;

public class SuggestionsFloatingFragment extends Fragment {
	@Nullable
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_suggestions_floating, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final Bundle arguments = getArguments();
		if (arguments == null) {
			return;
		}
		final String userId = arguments.getString("userId");
		final RecyclerView recyclerView = view.findViewById(R.id.fragment_suggestions_floating_list);
		CompletableFuture.supplyAsync(new Downloader.MissingFloatingHolidaysDownloader(userId))
				.thenAccept(missingFloatingHolidays -> requireActivity().runOnUiThread(() -> {
					recyclerView.setAdapter(new SuggestionFloatingAdapter(missingFloatingHolidays));
					view.findViewById(R.id.fragment_suggestions_floating_indicator).setVisibility(View.GONE);
				}));
	}

	@NonNull
	public static SuggestionsFloatingFragment newInstance(@Nullable final String userId) {
		final Bundle args = new Bundle();
		args.putString("userId", userId);
		final SuggestionsFloatingFragment fragment = new SuggestionsFloatingFragment();
		fragment.setArguments(args);
		return fragment;
	}
}
