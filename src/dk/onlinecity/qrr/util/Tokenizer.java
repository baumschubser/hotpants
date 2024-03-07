/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.util;

import java.util.Vector;

public class Tokenizer
{
	/**
	 * Returns a vector of tokens.
	 * 
	 * @param s
	 *            Input string.
	 * @param delim
	 *            Token delimiter.
	 * @return Vector tokens.
	 */
	public static Vector tokenize(String s, String delim)
	{
		Vector tokens = new Vector();
		int i;
		int l = s.length();
		while ((i = s.indexOf(delim)) >= 0) {
			tokens.addElement(s.substring(0, i));
			if (i + 1 > l) return tokens;
			s = s.substring(i + 1, l);
			l = s.length();
		}
		tokens.addElement(s);
		return tokens;
	}

	/**
	 * Splits a string in two.
	 * 
	 * @param s
	 *            Input string.
	 * @param delim
	 *            Split delimiter.
	 * @return Pair of strings.
	 */
	public static String[] split(String s, String delim)
	{
		int i = s.indexOf(delim);

		if (i <= 0) return new String[] { s, s };

		return new String[] { s.substring(0, i), s.substring(i + 1, s.length()) };
	}
}
