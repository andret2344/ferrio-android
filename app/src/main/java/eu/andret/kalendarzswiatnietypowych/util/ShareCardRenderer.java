package eu.andret.kalendarzswiatnietypowych.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.util.Pair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Month;
import java.util.List;
import java.util.concurrent.Executor;

import eu.andret.kalendarzswiatnietypowych.FerrioApplication;
import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.databinding.ShareCardDayBinding;
import eu.andret.kalendarzswiatnietypowych.databinding.ShareCardHolidayBinding;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;

public final class ShareCardRenderer {
	private static final String TAG = "Ferrio-ShareCardRenderer";
	private static final int MAX_HOLIDAYS_DISPLAYED = 6;

	private ShareCardRenderer() {
	}

	public static void shareHoliday(@NonNull final Context context,
			@NonNull final Holiday holiday) {
		final ShareCardHolidayBinding binding = ShareCardHolidayBinding.inflate(LayoutInflater.from(context));
		final Pair<Month, Integer> pair = new Pair<>(Month.of(holiday.getMonth()), holiday.getDay());

		binding.shareCardDate.setText(Util.getFormattedDateWithYear(pair));
		binding.shareCardName.setText(getNameWithFlag(holiday));

		if (!holiday.getDescription().isBlank()) {
			binding.shareCardDescription.setText(holiday.getDescription());
			binding.shareCardDescription.setVisibility(View.VISIBLE);
		}

		final Bitmap bitmap = renderViewToBitmap(binding.getRoot());
		encodeAndShareAsync(context, bitmap);
	}

	public static void shareDay(@NonNull final Context context,
			@NonNull final Pair<Month, Integer> pair,
			@NonNull final List<Holiday> holidays) {
		final ShareCardDayBinding binding = ShareCardDayBinding.inflate(LayoutInflater.from(context));

		binding.shareCardDate.setText(Util.getFormattedDateWithYear(pair));

		final int displayCount = Math.min(holidays.size(), MAX_HOLIDAYS_DISPLAYED);
		for (int i = 0; i < displayCount; i++) {
			final TextView item = new TextView(context);
			item.setText(context.getString(R.string.pointed_text, getNameWithFlag(holidays.get(i))));
			item.setTextColor(context.getColor(R.color.share_card_text));
			item.setTextSize(16);
			item.setSingleLine(true);
			item.setPadding(0, 6, 0, 6);
			binding.shareCardHolidaysList.addView(item);
		}

		if (holidays.size() > MAX_HOLIDAYS_DISPLAYED) {
			final int moreCount = holidays.size() - MAX_HOLIDAYS_DISPLAYED;
			binding.shareCardMore.setText(context.getResources().getQuantityString(R.plurals.see_more, moreCount, moreCount));
			binding.shareCardMore.setVisibility(View.VISIBLE);
		}

		final Bitmap bitmap = renderViewToBitmap(binding.getRoot());
		encodeAndShareAsync(context, bitmap);
	}

	@NonNull
	private static String getNameWithFlag(@NonNull final Holiday holiday) {
		if (holiday.getCountry() != null && !holiday.getCountry().isBlank()) {
			final String flag = Util.getCountryFlag(holiday.getCountry());
			if (flag != null) {
				return holiday.getName() + " " + flag;
			}
		}
		return holiday.getName();
	}

	@NonNull
	private static Bitmap renderViewToBitmap(@NonNull final View view) {
		final int scale = 3;
		final int widthPx = (int) (380 * view.getResources().getDisplayMetrics().density);
		final int widthSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY);
		final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

		view.measure(widthSpec, heightSpec);
		final int measuredWidth = view.getMeasuredWidth();
		final int measuredHeight = view.getMeasuredHeight();
		view.layout(0, 0, measuredWidth, measuredHeight);

		final Bitmap bitmap = Bitmap.createBitmap(
				measuredWidth * scale, measuredHeight * scale, Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas(bitmap);
		canvas.scale(scale, scale);
		view.draw(canvas);
		return bitmap;
	}

	/**
	 * PNG-encoding a 1140-wide ARGB_8888 bitmap at quality 100 plus the file write is the
	 * dominant cost in the share pipeline; on low-end devices it's enough to drop a frame.
	 * Inflate / measure / layout / draw stay on the UI thread (Material inflation is not
	 * thread-safe), but encode + write + URI build run on {@link FerrioApplication#IO_EXECUTOR}
	 * and the {@code startActivity} chooser is posted back to the main looper.
	 */
	private static void encodeAndShareAsync(@NonNull final Context context,
			@NonNull final Bitmap bitmap) {
		final Context appContext = context.getApplicationContext();
		final Executor mainExecutor = ContextCompat.getMainExecutor(appContext);
		FerrioApplication.IO_EXECUTOR.execute(() -> {
			final Uri uri;
			try {
				final File dir = new File(appContext.getCacheDir(), "share_cards");
				if (!dir.exists()) {
					dir.mkdirs();
				}
				final File file = new File(dir, "share.png");
				try (final FileOutputStream out = new FileOutputStream(file)) {
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
				}
				uri = FileProvider.getUriForFile(
						appContext, appContext.getPackageName() + ".fileprovider", file);
			} catch (final IOException ex) {
				Log.e(TAG, "Failed to write share card", ex);
				mainExecutor.execute(bitmap::recycle);
				return;
			}
			mainExecutor.execute(() -> {
				final Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("image/png");
				intent.putExtra(Intent.EXTRA_STREAM, uri);
				intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_via)));
				bitmap.recycle();
			});
		});
	}
}
