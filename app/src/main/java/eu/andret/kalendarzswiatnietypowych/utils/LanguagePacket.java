package eu.andret.kalendarzswiatnietypowych.utils;

import java.util.Date;
import java.util.Locale;

public class LanguagePacket implements Comparable<LanguagePacket> {
	private final int id;
	private final Locale locale;
	private final int translated;
	private boolean downloaded;
	private final boolean update;
	private Date date;

	public LanguagePacket(int id, Locale locale, int translated, boolean downloaded, boolean update) {
		this.id = id;
		this.locale = locale;
		this.translated = translated;
		this.downloaded = downloaded;
		this.update = update;
	}

	public int getId() {
		return id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Locale getLocale() {
		return locale;
	}

	public int getTranslated() {
		return translated;
	}

	public boolean isDownloaded() {
		return downloaded;
	}

	public void setDownloaded(boolean downloaded) {
		this.downloaded = downloaded;
	}

	public boolean isUpdate() {
		return update;
	}

	@Override
	public String toString() {
		return "LangPacket [id=" + id + ", locale=" + locale + ", translated=" + translated + ", downloaded=" + downloaded + ", update=" + update + "]";
	}

	@Override
	public int compareTo(LanguagePacket o) {
		return locale.getDisplayName().compareTo(o.locale.getDisplayName());
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + (locale == null ? 0 : locale.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		LanguagePacket other = (LanguagePacket) obj;
		if ((locale == null && other.locale != null) || (locale != null && other.locale == null)) {
			return false;
		}
		return locale.equals(other.locale);
	}
}
