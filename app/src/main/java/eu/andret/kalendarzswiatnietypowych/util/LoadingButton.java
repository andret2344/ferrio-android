package eu.andret.kalendarzswiatnietypowych.util;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec;
import com.google.android.material.progressindicator.IndeterminateDrawable;

public final class LoadingButton {
	private LoadingButton() {
	}

	/**
	 * Puts the button into a loading state: disabled, an indeterminate circular spinner in place of
	 * the icon and the given loading text. Returns a {@link Runnable} that restores the button to
	 * its exact pre-loading state (text, icon and enabled flag).
	 */
	@NonNull
	public static Runnable start(@NonNull final MaterialButton button, @StringRes final int loadingText) {
		final CharSequence originalText = button.getText();
		final Drawable originalIcon = button.getIcon();
		final boolean originalEnabled = button.isEnabled();

		final CircularProgressIndicatorSpec spec = new CircularProgressIndicatorSpec(button.getContext(), null, 0,
				com.google.android.material.R.style.Widget_Material3_CircularProgressIndicator_ExtraSmall);
		spec.indicatorColors = new int[]{button.getCurrentTextColor()};
		final IndeterminateDrawable<CircularProgressIndicatorSpec> spinner =
				IndeterminateDrawable.createCircularDrawable(button.getContext(), spec);

		button.setEnabled(false);
		button.setIcon(spinner);
		button.setText(loadingText);

		return () -> {
			button.setIcon(originalIcon);
			button.setText(originalText);
			button.setEnabled(originalEnabled);
		};
	}
}
