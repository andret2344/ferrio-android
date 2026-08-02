package eu.andret.kalendarzswiatnietypowych.adapter;

import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;
import java.util.Set;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.databinding.AdapterFaqBinding;
import eu.andret.kalendarzswiatnietypowych.entity.FaqEntry;

public class FaqAdapter extends ListAdapter<FaqEntry, FaqAdapter.ViewHolder> {

	private static final DiffUtil.ItemCallback<FaqEntry> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
		@Override
		public boolean areItemsTheSame(@NonNull final FaqEntry oldItem, @NonNull final FaqEntry newItem) {
			return oldItem.getQuestionResId() == newItem.getQuestionResId();
		}

		@Override
		public boolean areContentsTheSame(@NonNull final FaqEntry oldItem, @NonNull final FaqEntry newItem) {
			return oldItem.equals(newItem);
		}
	};

	/**
	 * Expansion is keyed by question resource id rather than adapter position so it survives a
	 * rebind without the entries having to carry view state.
	 */
	private final Set<Integer> expanded = new HashSet<>();

	public FaqAdapter() {
		super(DIFF_CALLBACK);
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final TextView question;
		private final TextView answer;
		private final ImageView chevron;
		private final View divider;

		public ViewHolder(@NonNull final AdapterFaqBinding binding) {
			super(binding.getRoot());
			question = binding.adapterFaqTextQuestion;
			answer = binding.adapterFaqTextAnswer;
			chevron = binding.adapterFaqChevron;
			divider = binding.adapterFaqDivider;
		}
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int viewType) {
		final AdapterFaqBinding binding = AdapterFaqBinding.inflate(
				LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
		return new ViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position) {
		final FaqEntry entry = getItem(position);
		final boolean isExpanded = expanded.contains(entry.getQuestionResId());

		viewHolder.question.setText(entry.getQuestionResId());
		// Answers carry inline markup (emphasis, line breaks and the privacy policy link), so they
		// are authored as HTML in strings.xml instead of being assembled in code.
		viewHolder.answer.setText(HtmlCompat.fromHtml(
				viewHolder.itemView.getContext().getString(entry.getAnswerResId()),
				HtmlCompat.FROM_HTML_MODE_COMPACT));
		viewHolder.answer.setMovementMethod(LinkMovementMethod.getInstance());
		viewHolder.answer.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
		viewHolder.divider.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
		viewHolder.chevron.setImageResource(isExpanded
				? R.drawable.baseline_expand_less_24
				: R.drawable.baseline_expand_more_24);
		viewHolder.chevron.setContentDescription(viewHolder.itemView.getContext().getString(isExpanded
				? R.string.content_description_faq_collapse
				: R.string.content_description_faq_expand));

		viewHolder.itemView.setOnClickListener(view -> {
			if (expanded.contains(entry.getQuestionResId())) {
				expanded.remove(entry.getQuestionResId());
			} else {
				expanded.add(entry.getQuestionResId());
			}
			notifyItemChanged(viewHolder.getBindingAdapterPosition());
		});
	}
}
