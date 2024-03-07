/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.decode;

final class Mode
{

	/* Table 2 p. 22 */
	/** Message terminator (0000). */
	static final int TERMINATOR = 0;
	/** Numeric mode constant (0001). */
	static final int NUMERIC = 1;
	/** Alphanumeric mode constant (0010). */
	static final int ALPHA = 2;
	/** Structured append mode constant (0011). */
	static final int STRUCTURED_APPEND = 3;
	/** Byte mode constant (0100). */
	static final int BYTE = 4;
	/** FNC1 1st position mode constant (0101). */
	static final int FNC1A = 5;
	/** ECI mode constant (0111). */
	static final int ECI = 7;
	/** Kanji mode constant (1000). */
	static final int KANJI = 8;
	/** FNC1 2nd position mode (1001). */
	static final int FNC1B = 9;

	static final int getWordsToRead(BitArray bits, int mode, int version)
	{
		// Table 3. p. 23
		switch (mode) {
		case TERMINATOR:
			return 0;
		case NUMERIC:
			if (version < 10) return bits.getNext(10);
			if (version < 27) return bits.getNext(12);
			if (version < 41) return bits.getNext(14);
			return -1;
		case ALPHA:
			if (version < 10) return bits.getNext(9);
			if (version < 27) return bits.getNext(11);
			if (version < 41) return bits.getNext(13);
			return -1;
		case STRUCTURED_APPEND:
			return -1;
		case BYTE:
			if (version < 10) return bits.getNext(8);
			if (version < 41) return bits.getNext(16);
			return -1;
		case FNC1A:
			return -1;
		case ECI:
			return -1;
		case KANJI:
			return -1;
		case FNC1B:
			return -1;
		default:
			return -1;
		}
	}
}
