package eu.andret.kalendarzswiatnietypowych.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import androidx.core.util.Pair;

import org.assertj.core.api.ThrowableAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.Locale;

public class UtilTest {

	private Locale originalLocale;

	@Before
	public void saveLocale() {
		originalLocale = Locale.getDefault();
	}

	@After
	public void restoreLocale() {
		Locale.setDefault(originalLocale);
	}

	@Test
	public void calculateIndex_feb29_alwaysReturnsFixedSlot() {
		assertThat(Util.calculateIndex(2, 29)).isEqualTo(59);
	}

	@Test
	public void calculateIndex_feb30_alwaysReturnsFixedSlot() {
		assertThat(Util.calculateIndex(2, 30)).isEqualTo(60);
	}

	@Test
	public void calculateIndex_jan1_isZero() {
		assertThat(Util.calculateIndex(1, 1)).isZero();
	}

	@Test
	public void calculateIndex_dec31_is366_regardlessOfLeap() {
		assertThat(Util.calculateIndex(12, 31)).isEqualTo(366);
	}

	@Test
	public void calculateIndex_feb28_is58() {
		assertThat(Util.calculateIndex(2, 28)).isEqualTo(58);
	}

	@Test
	public void calculateIndex_mar1_is61() {
		assertThat(Util.calculateIndex(3, 1)).isEqualTo(61);
	}

	@Test
	public void calculateDates_feb30Slot_returnsFebruary30() {
		final Pair<Month, Integer> date = Util.calculateDates(61);
		assertThat(date.first).isEqualTo(Month.FEBRUARY);
		assertThat(date.second).isEqualTo(30);
	}

	@Test
	public void calculateDates_feb29Slot_returnsFebruary29() {
		final Pair<Month, Integer> date = Util.calculateDates(60);
		assertThat(date.first).isEqualTo(Month.FEBRUARY);
		assertThat(date.second).isEqualTo(29);
	}

	@Test
	public void calculateDates_firstId_returnsJanuary1() {
		final Pair<Month, Integer> date = Util.calculateDates(1);
		assertThat(date.first).isEqualTo(Month.JANUARY);
		assertThat(date.second).isEqualTo(1);
	}

	@Test
	public void calculateDates_lastId_returnsDecember31() {
		final Pair<Month, Integer> date = Util.calculateDates(367);
		assertThat(date.first).isEqualTo(Month.DECEMBER);
		assertThat(date.second).isEqualTo(31);
	}

	@Test
	public void roundTrip_everyRealDay_plusLeapDays() {
		for (int month = 1; month <= 12; month++) {
			final int daysInMonth = YearMonth.of(2024, month).lengthOfMonth();
			for (int day = 1; day <= daysInMonth; day++) {
				final int index = Util.calculateIndex(month, day);
				final Pair<Month, Integer> back = Util.calculateDates(index + 1);
				assertThat(back.first).as("month mismatch for " + month + "/" + day).isEqualTo(Month.of(month));
				assertThat(back.second).as("day mismatch for " + month + "/" + day).isEqualTo(day);
			}
		}
		// Feb 30 is a synthetic slot tracked separately.
		final Pair<Month, Integer> feb30 = Util.calculateDates(Util.calculateIndex(2, 30) + 1);
		assertThat(feb30.first).isEqualTo(Month.FEBRUARY);
		assertThat(feb30.second).isEqualTo(30);
	}

	@Test
	public void calculateSeed_isStableAndOrderSensitive() {
		assertThat(Util.calculateSeed(1, 2)).isEqualTo(102L);
		assertThat(Util.calculateSeed(2, 1)).isEqualTo(201L);
	}

	@Test
	public void calculateDates_neverThrowsForValidIdRange() {
		for (int id = 1; id <= 367; id++) {
			final Pair<Month, Integer> date = Util.calculateDates(id);
			assertThat(date).isNotNull();
			// Every returned (month, day) must form a valid calendar date in *some* year,
			// or be the synthetic Feb 30 slot.
			if (date.first == Month.FEBRUARY && date.second == 30) {
				continue;
			}
			final ThrowableAssert.ThrowingCallable throwingCallable =
					() -> LocalDate.of(2024, date.first, date.second);
			assertThatCode(throwingCallable).doesNotThrowAnyException();
		}
	}

	@Test
	public void sha256_emptyString_matchesKnownDigest() {
		assertThat(Util.sha256(""))
				.isEqualTo("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
	}

	@Test
	public void sha256_abc_matchesKnownDigest() {
		assertThat(Util.sha256("abc"))
				.isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
	}

	@Test
	public void sha256_isDeterministic() {
		assertThat(Util.sha256("ferrio")).isEqualTo(Util.sha256("ferrio"));
	}

	@Test
	public void sha256_differentInputs_produceDifferentDigests() {
		assertThat(Util.sha256("a")).isNotEqualTo(Util.sha256("b"));
	}

	@Test
	public void sha256_nonAsciiInput_producesValidDigest() {
		// Regression: non-ASCII must not blow up and must produce a well-formed digest.
		assertThat(Util.sha256("świąt")).hasSize(64).matches("[0-9a-f]{64}");
	}

	@Test
	public void sha256_output_isAlways64HexChars() {
		assertThat(Util.sha256("anything")).hasSize(64).matches("[0-9a-f]{64}");
	}

	@Test
	public void getLanguageCode_polishLocale_returnsPl() {
		Locale.setDefault(new Locale("pl", "PL"));
		assertThat(Util.getLanguageCode()).isEqualTo("pl");
	}

	@Test
	public void getLanguageCode_englishLocale_returnsEn() {
		Locale.setDefault(Locale.ENGLISH);
		assertThat(Util.getLanguageCode()).isEqualTo("en");
	}

	@Test
	public void getLanguageCode_unsupportedLocale_fallsBackToEn() {
		Locale.setDefault(Locale.JAPANESE);
		assertThat(Util.getLanguageCode()).isEqualTo("en");
	}
}
