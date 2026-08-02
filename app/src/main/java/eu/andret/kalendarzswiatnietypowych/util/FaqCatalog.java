package eu.andret.kalendarzswiatnietypowych.util;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.entity.FaqEntry;

/**
 * Single source of the FAQ content. The list is bundled with the app rather than fetched from
 * api.ferrio.app on purpose: the entries change far less often than the app ships, the calendar is
 * usable offline, and the AI disclosure in particular must never depend on a successful network
 * call. Everything that reads the FAQ goes through {@link #getEntries()}, so swapping the backing
 * source later is a change in this class only.
 *
 * <p>Order follows the user's path: what is displayed, where the content comes from, how to change
 * it, account, then technical questions.</p>
 */
public final class FaqCatalog {
	private static final List<FaqEntry> ENTRIES = Collections.unmodifiableList(Arrays.asList(
			new FaqEntry(R.string.faq_question_hidden_holidays, R.string.faq_answer_hidden_holidays),
			new FaqEntry(R.string.faq_question_view_modes, R.string.faq_answer_view_modes),
			new FaqEntry(R.string.faq_question_missing_holiday, R.string.faq_answer_missing_holiday),
			new FaqEntry(R.string.faq_question_who_writes, R.string.faq_answer_who_writes),
			new FaqEntry(R.string.faq_question_report_error, R.string.faq_answer_report_error),
			new FaqEntry(R.string.faq_question_suggest, R.string.faq_answer_suggest),
			new FaqEntry(R.string.faq_question_statuses, R.string.faq_answer_statuses),
			new FaqEntry(R.string.faq_question_rejected, R.string.faq_answer_rejected),
			new FaqEntry(R.string.faq_question_account_types, R.string.faq_answer_account_types),
			new FaqEntry(R.string.faq_question_delete_account, R.string.faq_answer_delete_account),
			new FaqEntry(R.string.faq_question_offline, R.string.faq_answer_offline),
			new FaqEntry(R.string.faq_question_data_freshness, R.string.faq_answer_data_freshness),
			new FaqEntry(R.string.faq_question_widget, R.string.faq_answer_widget)));

	private FaqCatalog() {
	}

	@NonNull
	public static List<FaqEntry> getEntries() {
		return ENTRIES;
	}
}
