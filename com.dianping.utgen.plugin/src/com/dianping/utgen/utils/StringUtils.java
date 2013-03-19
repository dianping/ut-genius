package com.dianping.utgen.utils;

import java.util.Set;

public class StringUtils {
	public static final String EMPTY = "";

	public static String join(Object[] array, char separator) {
		if (array == null) {
			return null;
		}

		return join(array, separator, 0, array.length);
	}

	public static String uncap(String str) {
		return str.substring(0, 1).toLowerCase() + str.substring(1);
	}

	public static String cap(String str) {
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

	public static String join(Set<String> set, String separator) {
		String[] bb = new String[set.size()];
		int i = 0;
		for (String str : set) {
			bb[i++] = str;
		}
		return join(bb, separator);
	}

	public static String join(Object[] array, String separator) {
		if (array == null) {
			return null;
		}
		return join(array, separator, 0, array.length);
	}

	public static String join(Object[] array, String separator, int startIndex,
			int endIndex) {
		if (array == null) {
			return null;
		}
		if (separator == null) {
			separator = EMPTY;
		}

		// endIndex - startIndex > 0: Len = NofStrings *(len(firstString) +
		// len(separator))
		// (Assuming that all Strings are roughly equally long)
		int bufSize = (endIndex - startIndex);
		if (bufSize <= 0) {
			return EMPTY;
		}

		bufSize *= ((array[startIndex] == null ? 16 : array[startIndex]
				.toString().length()) + separator.length());

		StringBuffer buf = new StringBuffer(bufSize);

		for (int i = startIndex; i < endIndex; i++) {
			if (i > startIndex) {
				buf.append(separator);
			}
			if (array[i] != null) {
				buf.append(array[i]);
			}
		}
		return buf.toString();
	}

	public static String join(Object[] array, char separator, int startIndex,
			int endIndex) {

		if (array == null) {
			return null;
		}

		int bufSize = (endIndex - startIndex);

		if (bufSize <= 0) {
			return EMPTY;
		}

		bufSize *= ((array[startIndex] == null ? 16 : array[startIndex]
				.toString().length()) + 1);

		StringBuffer buf = new StringBuffer(bufSize);

		for (int i = startIndex; i < endIndex; i++) {

			if (i > startIndex) {
				buf.append(separator);
			}

			if (array[i] != null) {
				buf.append(array[i]);
			}

		}
		return buf.toString();
	}

	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	public static String replace(String text, String repl, String with, int max) {

		if (isEmpty(text) || isEmpty(repl) || with == null || max == 0) {
			return text;
		}

		int start = 0;
		int end = text.indexOf(repl, start);
		if (end == -1) {
			return text;
		}

		int replLength = repl.length();
		int increase = with.length() - replLength;
		increase = (increase < 0 ? 0 : increase);
		increase *= (max < 0 ? 16 : (max > 64 ? 64 : max));

		StringBuffer buf = new StringBuffer(text.length() + increase);

		while (end != -1) {
			buf.append(text.substring(start, end)).append(with);
			start = end + replLength;
			if (--max == 0) {
				break;
			}

			end = text.indexOf(repl, start);

		}

		buf.append(text.substring(start));
		return buf.toString();
	}
}
