package eu.andret.kalendarzswiatnietypowych.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;

public final class ShareCardRenderer {
	private static final int MAX_HOLIDAYS_DISPLAYED = 6;
	private static final DateTimeFormatter SHARE_DATE_FORMATTER =
			DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault());

	private ShareCardRenderer() {
	}

	public static void shareHoliday(@NonNull final Context context, @NonNull final Holiday holiday) {
		final View card = LayoutInflater.from(context).inflate(R.layout.share_card_holiday, null);
		final LocalDate date = LocalDate.of(LocalDate.now().getYear(), holiday.getMonth(), holiday.getDay());

		final TextView dateView = card.findViewById(R.id.share_card_date);
		final TextView nameView = card.findViewById(R.id.share_card_name);
		final TextView descView = card.findViewById(R.id.share_card_description);

		dateView.setText(SHARE_DATE_FORMATTER.format(date));
		nameView.setText(getNameWithFlag(holiday));

		if (!holiday.getDescription().isBlank()) {
			descView.setText(holiday.getDescription());
			descView.setVisibility(View.VISIBLE);
		}

		final Bitmap bitmap = renderViewToBitmap(card);
		shareImage(context, bitmap);
	}

	public static void shareDay(@NonNull final Context context, @NonNull final LocalDate date,
			@NonNull final List<Holiday> holidays) {
		final View card = LayoutInflater.from(context).inflate(R.layout.share_card_day, null);

		final TextView dateView = card.findViewById(R.id.share_card_date);
		final LinearLayout listLayout = card.findViewById(R.id.share_card_holidays_list);
		final TextView moreView = card.findViewById(R.id.share_card_more);

		dateView.setText(SHARE_DATE_FORMATTER.format(date));

		final int displayCount = Math.min(holidays.size(), MAX_HOLIDAYS_DISPLAYED);
		for (int i = 0; i < displayCount; i++) {
			final TextView item = new TextView(context);
			item.setText(context.getString(R.string.pointed_text, getNameWithFlag(holidays.get(i))));
			item.setTextColor(context.getColor(R.color.share_card_text));
			item.setTextSize(16);
			item.setSingleLine(true);
			item.setPadding(0, 6, 0, 6);
			listLayout.addView(item);
		}

		if (holidays.size() > MAX_HOLIDAYS_DISPLAYED) {
			moreView.setText(context.getString(R.string.see_more, holidays.size() - MAX_HOLIDAYS_DISPLAYED));
			moreView.setVisibility(View.VISIBLE);
		}

		final Bitmap bitmap = renderViewToBitmap(card);
		shareImage(context, bitmap);
	}

	@NonNull
	private static String getNameWithFlag(@NonNull final Holiday holiday) {
		if (holiday.getCountry() != null && !holiday.getCountry().isBlank()) {
			final Emoji emoji = EmojiManager.getForAlias(holiday.getCountry().toLowerCase(Locale.ROOT));
			if (emoji != null) {
				return holiday.getName() + " " + emoji.getUnicode();
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

	private static void shareImage(@NonNull final Context context, @NonNull final Bitmap bitmap) {
		final File dir = new File(context.getCacheDir(), "share_cards");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		final File file = new File(dir, "share.png");
		try (FileOutputStream out = new FileOutputStream(file)) {
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
		} catch (final IOException e) {
			return;
		} finally {
			bitmap.recycle();
		}

		final android.net.Uri uri = FileProvider.getUriForFile(
				context, context.getPackageName() + ".fileprovider", file);
		final Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("image/png");
		intent.putExtra(Intent.EXTRA_STREAM, uri);
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_via)));
	}
}
