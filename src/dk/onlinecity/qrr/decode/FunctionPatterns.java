/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.decode;

/**
 * This class represents the function patterns of a QR-Code.
 * 
 * @author Thomas Duerlund (td@onlinecity.dk)
 */
final class FunctionPatterns
{
	/**
	 * Alignment pattern center coordinates (table pp. 83-84).
	 */
	private static final int[][] ALIGNMENT_PATTERN_CENTERS = {
	/* 0 */{ -2, -2 },
	/* 1 */{ -1, -1 },
	/* 2 */{ 6, 18 },
	/* 3 */{ 6, 22 },
	/* 4 */{ 6, 26 },
	/* 5 */{ 6, 30 },
	/* 6 */{ 6, 34 },
	/* 7 */{ 6, 22, 38 },
	/* 8 */{ 6, 24, 42 },
	/* 9 */{ 6, 26, 46 },
	/* 10 */{ 6, 28, 50 },
	/* 11 */{ 6, 30, 54 },
	/* 12 */{ 6, 32, 58 },
	/* 13 */{ 6, 34, 62 },
	/* 14 */{ 6, 26, 46, 66 },
	/* 15 */{ 6, 26, 48, 70 },
	/* 16 */{ 6, 26, 50, 74 },
	/* 17 */{ 6, 30, 54, 78 },
	/* 18 */{ 6, 30, 56, 82 },
	/* 19 */{ 6, 30, 58, 86 },
	/* 20 */{ 6, 34, 62, 90 },
	/* 21 */{ 6, 28, 50, 72, 94 },
	/* 22 */{ 6, 26, 50, 74, 98 },
	/* 23 */{ 6, 30, 54, 78, 102 },
	/* 24 */{ 6, 28, 54, 80, 106 },
	/* 25 */{ 6, 32, 58, 84, 110 },
	/* 26 */{ 6, 30, 58, 86, 114 },
	/* 27 */{ 6, 34, 62, 90, 118 },
	/* 28 */{ 6, 26, 50, 74, 98, 122 },
	/* 29 */{ 6, 30, 54, 78, 102, 126 },
	/* 30 */{ 6, 26, 52, 78, 104, 130 },
	/* 31 */{ 6, 30, 56, 82, 108, 134 },
	/* 32 */{ 6, 34, 60, 86, 112, 138 },
	/* 33 */{ 6, 30, 58, 86, 114, 142 },
	/* 34 */{ 6, 34, 62, 90, 118, 146 },
	/* 35 */{ 6, 30, 54, 78, 102, 126, 150 },
	/* 36 */{ 6, 24, 50, 76, 102, 128, 154 },
	/* 37 */{ 6, 28, 54, 80, 106, 132, 158 },
	/* 38 */{ 6, 32, 58, 84, 110, 136, 162 },
	/* 39 */{ 6, 26, 54, 82, 110, 138, 166 },
	/* 40 */{ 6, 30, 58, 86, 114, 142, 170 } };

	/** QR-Code size. */
	private final int size;
	/** QR-Code version. */
	private final int version;
	/** Matrix of function patterns. */
	private boolean[][] functionPatterns;

	/**
	 * @param version
	 *            Version to create.
	 * @param size
	 *            Function pattern matrix size.
	 */
	FunctionPatterns(int version, int size)
	{
		this.size = size;
		this.version = version;
		functionPatterns = new boolean[size][size];
		generateFinderPatterns();
		generateAlignmentPatterns();
		generateTimingPatterns();
		generateVersionPatterns();
	}

	/**
	 * Returns true if a coordinate is a function pattern.
	 * 
	 * @param x
	 *            X coordinate.
	 * @param y
	 *            Y coordinate.
	 * @return True if the coordinate is a function pattern.
	 */
	final boolean isFunctionPattern(int x, int y)
	{
		return functionPatterns[x][y];
	}

	/**
	 * Generates the finder patterns and the format areas.
	 */
	private final void generateFinderPatterns()
	{
		// upper left, including format and error correction code areas
		for (int y = 0; y < 9; y++) {
			for (int x = 0; x < 9; x++) {
				functionPatterns[x][y] = true;
			}
		}

		// upper right, including format and error correction code areas.
		for (int y = 0; y < 9; y++) {
			for (int x = size - 8; x < size; x++) {
				functionPatterns[x][y] = true;
			}
		}

		// lower left
		for (int y = size - 8; y < size; y++) {
			for (int x = 0; x < 9; x++) {
				functionPatterns[x][y] = true;
			}
		}
	}

	/**
	 * Generates the timing patterns.
	 */
	private final void generateTimingPatterns()
	{
		for (int i = 0; i < size; i++) {
			functionPatterns[6][i] = true;
			functionPatterns[i][6] = true;
		}
	}

	/**
	 * Generates the alignment patterns.
	 */
	private final void generateAlignmentPatterns()
	{
		if (version < 2 || version > 40) {
			return;
		}

		try {
			int[] centers = ALIGNMENT_PATTERN_CENTERS[version];
			int l = centers.length;
			for (int i = 0; i < l; i++) {
				for (int j = 0; j < l; j++) {

					if (!functionPatterns[centers[i]][centers[j]]) {
						setAlignmentPattern(centers[i], centers[j]);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Generates an alignment pattern.
	 * 
	 * @param x
	 *            Alignment pattern center x coordinate.
	 * @param y
	 *            Alignment pattern center y coordinate.
	 */
	private final void setAlignmentPattern(int x, int y)
	{
		int x0 = x - 2;
		int y0 = y - 2;
		int x1 = x + 3;
		int y1 = y + 3;

		for (x = x0; x < x1; x++) {
			for (y = y0; y < y1; y++) {
				functionPatterns[x][y] = true;
			}
		}
	}

	/**
	 * Generates the version patterns.
	 */
	private final void generateVersionPatterns()
	{
		if (version < 7) return;

		int i0 = size - 11;
		int i1 = size - 8;
		int j0 = 0;
		int j1 = 6;

		// upper right area
		for (int y = j0; y < j1; y++) {
			for (int x = i0; x < i1; x++) {
				functionPatterns[x][y] = true;
			}
		}

		// lower left area
		for (int x = j0; x < j1; x++) {
			for (int y = i0; y < i1; y++) {
				functionPatterns[x][y] = true;
			}
		}
	}
}