package eu.andret.kalendarzswiatnietypowych.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import eu.andret.kalendarzswiatnietypowych.util.Util;

public class HolidayDayTest {

	private static Holiday holiday(final String id, final boolean usual) {
		return new Holiday(id, 1, 1, "name-" + id, "desc", usual, "PL", null, false);
	}

	@Test
	public void getId_concatenatesMonthAndDay() {
		assertThat(new HolidayDay(3, 14).getId()).isEqualTo("314");
	}

	@Test
	public void getId_singleDigitsAreNotPadded() {
		// Documents current behavior: "1" + "1" → "11", same as "11" + alone — collisions are
		// avoided because the field is only used as a stable diff key within a fixed list.
		assertThat(new HolidayDay(1, 1).getId()).isEqualTo("11");
	}

	@Test
	public void getSeed_matchesUtilCalculateSeed() {
		assertThat(new HolidayDay(2, 29).getSeed()).isEqualTo(Util.calculateSeed(29, 2));
	}

	@Test
	public void compareTo_ordersByMonthFirst() {
		final HolidayDay january = new HolidayDay(1, 31);
		final HolidayDay february = new HolidayDay(2, 1);
		assertThat(january.compareTo(february)).isNegative();
		assertThat(february.compareTo(january)).isPositive();
	}

	@Test
	public void compareTo_ordersByDayWithinSameMonth() {
		final HolidayDay early = new HolidayDay(6, 5);
		final HolidayDay late = new HolidayDay(6, 20);
		assertThat(early.compareTo(late)).isNegative();
		assertThat(late.compareTo(early)).isPositive();
		assertThat(early.compareTo(new HolidayDay(6, 5))).isZero();
	}

	@Test
	public void getHolidaysList_includeUsualTrue_returnsAll() {
		final Holiday usual = holiday("fixed-1", true);
		final Holiday unusual = holiday("fixed-2", false);
		final HolidayDay day = new HolidayDay(1, 1, new ArrayList<>(Arrays.asList(usual, unusual)));

		assertThat(day.getHolidaysList(true)).containsExactly(usual, unusual);
	}

	@Test
	public void getHolidaysList_includeUsualFalse_filtersOutUsual() {
		final Holiday usual = holiday("fixed-1", true);
		final Holiday unusual = holiday("fixed-2", false);
		final HolidayDay day = new HolidayDay(1, 1, new ArrayList<>(Arrays.asList(usual, unusual)));

		assertThat(day.getHolidaysList(false)).containsExactly(unusual);
	}

	@Test
	public void countHolidays_matchesGetHolidaysListSize() {
		final HolidayDay day = new HolidayDay(1, 1, new ArrayList<>(Arrays.asList(
				holiday("fixed-1", true),
				holiday("fixed-2", false),
				holiday("fixed-3", false))));

		assertThat(day.countHolidays(true)).isEqualTo(day.getHolidaysList(true).size());
		assertThat(day.countHolidays(false)).isEqualTo(day.getHolidaysList(false).size());
		assertThat(day.countHolidays(false)).isEqualTo(2);
	}

	@Test
	public void addHoliday_appendsToBackingList() {
		final HolidayDay day = new HolidayDay(1, 1);
		final Holiday added = holiday("fixed-1", false);

		day.addHoliday(added);

		assertThat(day.getHolidays()).containsExactly(added);
	}

	@Test
	public void equals_sameMonthDayAndHolidays_areEqual() {
		final List<Holiday> list = Collections.singletonList(holiday("fixed-1", false));
		assertThat(new HolidayDay(2, 14, new ArrayList<>(list)))
				.isEqualTo(new HolidayDay(2, 14, new ArrayList<>(list)));
	}

	@Test
	public void equals_differentDay_notEqual() {
		assertThat(new HolidayDay(2, 14)).isNotEqualTo(new HolidayDay(2, 15));
	}

	@Test
	public void hashCode_equalObjects_haveEqualHashes() {
		assertThat(new HolidayDay(5, 1).hashCode()).isEqualTo(new HolidayDay(5, 1).hashCode());
	}
}
