package eu.andret.kalendarzswiatnietypowych.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.util.Objects;

/**
 * A single question/answer pair of the in-app FAQ. Both sides are string resources so the content
 * follows the existing {@code values-pl/} translation pipeline and stays available offline.
 */
public class FaqEntry {
	@StringRes
	private final int questionResId;
	@StringRes
	private final int answerResId;

	public FaqEntry(@StringRes final int questionResId, @StringRes final int answerResId) {
		this.questionResId = questionResId;
		this.answerResId = answerResId;
	}

	@StringRes
	public int getQuestionResId() {
		return questionResId;
	}

	@StringRes
	public int getAnswerResId() {
		return answerResId;
	}

	@Override
	public boolean equals(@Nullable final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final FaqEntry that = (FaqEntry) o;
		return questionResId == that.questionResId && answerResId == that.answerResId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(questionResId, answerResId);
	}

	@NonNull
	@Override
	public String toString() {
		return "FaqEntry{" +
				"questionResId=" + questionResId +
				", answerResId=" + answerResId +
				'}';
	}
}
