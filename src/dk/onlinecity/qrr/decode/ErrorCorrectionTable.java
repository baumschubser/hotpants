/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */
package dk.onlinecity.qrr.decode;

import dk.onlinecity.qrr.core.exceptions.DecodeException;

public class ErrorCorrectionTable
{
	private final static int L = 1;
	private final static int M = 0;
	private final static int Q = 3;
	private final static int H = 2;

	private static final int L_INDEX = 0;
	private static final int M_INDEX = 1;
	private static final int Q_INDEX = 2;
	private static final int H_INDEX = 3;

	final static int[] lookup(int version, int ecLevel) throws DecodeException
	{
		if (version > 10) throw new DecodeException("Version " + version + " is not supported");

		int[] error = DECODEBLOCKS[version - 1][getIndex(ecLevel)];
		return error;
	}

	final static int getP(int version, int ecLevel)
	{
		switch (version) {
		case 1:
			switch (ecLevel) {
			case L:
				return 3;
			case M:
				return 2;
			default:
				return 1;
			}
		case 2:
			return ecLevel == L ? 2 : 0;
		case 3:
			return ecLevel == L ? 1 : 0;
		default:
			return 0;
		}
	}

	private final static int getIndex(int ecLevel) throws DecodeException
	{
		switch (ecLevel) {
		case L:
			return L_INDEX;
		case M:
			return M_INDEX;
		case H:
			return H_INDEX;
		case Q:
			return Q_INDEX;
		default:
			throw new DecodeException("Unknown error correction level");
		}
	}

	final static String printErrLevel(int ecLevel)
	{
		switch (ecLevel) {
		case L:
			return "L";
		case M:
			return "M";
		case H:
			return "H";
		case Q:
			return "Q";
		default:
			return "ERROR IN ERROR CORRECTION LEVEL";
		}
	}

	// table 9 p.38
	/*
	 * [version - 1][L, M, Q, H][error correction code per block]
	 */
	private static final int[][][] DECODEBLOCKS = {
	/* version 1 */
	{ { 1, 26, 19 }, { 1, 26, 16 }, { 1, 26, 13 }, { 1, 26, 9 } },
	/* version 2 */
	{ { 1, 44, 34 }, { 1, 44, 28 }, { 1, 44, 22 }, { 1, 44, 16 } },
	/* version 3 */
	{ { 1, 70, 55 }, { 1, 70, 44 }, { 2, 35, 17 }, { 2, 35, 13 } },
	/* version 4 */
	{ { 1, 100, 80 }, { 2, 50, 32 }, { 2, 50, 24 }, { 4, 25, 9 } },
	/* version 5 */
	{ { 1, 134, 108 }, { 2, 67, 43 }, { 2, 33, 15, 2, 34, 16 }, { 2, 33, 11, 2, 34, 12 } },
	/* version 6 */
	{ { 2, 86, 68 }, { 4, 43, 27 }, { 4, 43, 19 }, { 4, 43, 15 } },
	/* version 7 */
	{ { 2, 98, 78 }, { 4, 49, 31 }, { 2, 32, 14, 4, 33, 15 }, { 4, 39, 13, 1, 40, 14 } },
	/* version 8 */
	{ { 2, 121, 97 }, { 2, 60, 38, 2, 61, 39 }, { 4, 40, 18, 2, 41, 19 }, { 4, 40, 14, 2, 41, 15 } },
	/* version 9 */
	{ { 2, 146, 116 }, { 3, 58, 36, 2, 59, 37 }, { 4, 36, 16, 4, 37, 17 }, { 4, 36, 12, 4, 37, 13 } },
	/* version 10 */
	{ { 2, 86, 68, 2, 87, 69 }, { 4, 69, 43, 1, 70, 44 }, { 6, 43, 19, 2, 44, 20 }, { 6, 43, 15, 2, 44, 16 } },
	/* version 11 */
	{ { 4, 101, 81 }, { 1, 80, 50, 4, 81, 51 }, { 4, 50, 22, 4, 51, 23 }, { 3, 36, 12, 8, 37, 13 } },
	/* version 12 */
	{ { 2, 116, 92, 2, 117, 93 }, { 6, 56, 36, 2, 59, 37 }, { 4, 46, 20, 6, 47, 21 }, { 7, 42, 14, 4, 43, 14 } },
	/* version 13 */
	{ { 4, 133, 107 }, { 8, 59, 37, 1, 60, 38 }, { 8, 44, 20, 4, 45, 21 }, { 12, 33, 11, 4, 34, 12, 11 } },
	/* version 14 */

	/* version 15 */
	/* version 16 */
	/* version 17 */
	/* version 18 */
	/* version 19 */
	/* version 20 */
	/* version 21 */
	/* version 22 */
	/* version 23 */
	/* version 24 */
	/* version 25 */
	/* version 26 */
	/* version 27 */
	/* version 28 */
	/* version 29 */
	/* version 30 */

	};
}
