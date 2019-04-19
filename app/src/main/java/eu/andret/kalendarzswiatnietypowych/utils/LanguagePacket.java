package eu.andret.kalendarzswiatnietypowych.utils;

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
	private final int id;
	private final Locale locale;
	private final int translated;
	@NonFinal
	@Setter
	private boolean downloaded;
	private final boolean update;
	@NonFinal
	@Setter
	private Date date;

	public LanguagePacket(int id, Locale locale, int translated, boolean downloaded, boolean update) {
		this.id = id;
		this.locale = locale;
		this.translated = translated;
		this.downloaded = downloaded;
		this.update = update;
	}

	@Override
	public int compareTo(LanguagePacket o) {
		return locale.getDisplayName().compareTo(o.locale.getDisplayName());
	}
}
