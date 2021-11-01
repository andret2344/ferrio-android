package eu.andret.kalendarzswiatnietypowych.entity;

import java.util.Date;
import java.util.Locale;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.NonFinal;

@EqualsAndHashCode(of = "locale")
@ToString(exclude = "date")
@Value
@AllArgsConstructor(access = AccessLevel.NONE)
@NoArgsConstructor(access = AccessLevel.NONE)
public class LanguagePacket implements Comparable<LanguagePacket> {
	int id;
	Locale locale;
	int translated;
	@NonFinal
	@Setter
	boolean downloaded;
	boolean update;
	@NonFinal
	@Setter
	Date date;

	public LanguagePacket(final int id, final Locale locale, final int translated, final boolean downloaded, final boolean update) {
		this.id = id;
		this.locale = locale;
		this.translated = translated;
		this.downloaded = downloaded;
		this.update = update;
	}

	@Override
	public int compareTo(final LanguagePacket o) {
		return locale.getDisplayName().compareTo(o.locale.getDisplayName());
	}
}
