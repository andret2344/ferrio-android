package eu.andret.kalendarzswiatnietypowych.util;

import android.graphics.Typeface;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Restricts an int to the {@link Typeface} style constants accepted by
 * {@link android.widget.TextView#setTypeface(Typeface, int)}, so lint can verify call sites.
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({Typeface.NORMAL, Typeface.BOLD, Typeface.ITALIC, Typeface.BOLD_ITALIC})
public @interface TypefaceStyle {
}
