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
import eu.andret.kalendarzswiatnietypowych.adapter.SuggestionFixedAdapter;
import eu.andret.kalendarzswiatnietypowych.util.Downloader;
import java9.util.concurrent.CompletableFuture;

public class SuggestionsFixedFragment extends Fragment {
	@Nullable
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_suggestions_fixed, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final Bundle arguments = getArguments();
		if (arguments == null) {
			return;
		}
		final String userId = arguments.getString("userId");
		final RecyclerView recyclerView = view.findViewById(R.id.fragment_suggestions_fixed_list);
		CompletableFuture.supplyAsync(new Downloader.MissingFixedHolidaysDownloader(userId))
				.thenAccept(missingFixedHolidays -> requireActivity().runOnUiThread(() -> {
					recyclerView.setAdapter(new SuggestionFixedAdapter(missingFixedHolidays));
					view.findViewById(R.id.fragment_suggestions_fixed_indicator).setVisibility(View.GONE);
				}));
	}

	@NonNull
	public static SuggestionsFixedFragment newInstance(@Nullable final String userId) {
		final Bundle args = new Bundle();
		args.putString("userId", userId);
		final SuggestionsFixedFragment fragment = new SuggestionsFixedFragment();
		fragment.setArguments(args);
		return fragment;
	}
}
