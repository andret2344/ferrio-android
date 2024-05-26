package eu.andret.kalendarzswiatnietypowych.util;

import android.text.Editable;
import android.text.TextWatcher;

@FunctionalInterface
public interface SimpleTextWatcher extends TextWatcher {
	void onChange();

	@Override
	default void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
		// empty
	}

	@Override
	default void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
		// empty
	}

	@Override
	default void afterTextChanged(final Editable s) {
		onChange();
	}
}
