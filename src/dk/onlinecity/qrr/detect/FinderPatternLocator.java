/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.detect;

import java.util.Vector;


import dk.onlinecity.qrr.DetectInfo;
import dk.onlinecity.qrr.core.exceptions.QrrException;
import dk.onlinecity.qrr.image.MonochromeImage;
import dk.onlinecity.qrr.util.Mext;

/**
 * This class locates QR-Code finder patterns.
 * 
 * @author Thomas Duerlund (td@onlinecity)
 */
public class FinderPatternLocator
{
	/** Maximum module there can be for version 10 */
	private static final int MAX_MODULES = 57;

	/**
	 * Initiates the Finder pattern locator and starts searching for finder
	 * patterns.
	 * 
	 * @param image
	 *            {@link MonochromeImage} to search for finder patterns.
	 * @throws QrrException
	 *             Thrown if we can't find three or more finder patterns
	 *             candidates.
	 */
	public static FinderPattern[] detect(MonochromeImage image) throws FinderPatternException
	{
		Vector finderPatterns = new Vector();
		boolean[] rowScanned = new boolean[image.getHeight()];

		// minimum step if the QR-Code takes up 1/4 of the image.
		int ms = (int) (image.getWidth() / (MAX_MODULES * 4.0f));
		ms = ms < 1 ? 1 : ms;
		scanRows(image, finderPatterns, rowScanned, 0, ms, 0, ms);

		// Found less than three finder patterns, try harder.
		if (ms > 1 && finderPatterns.size() < 3) {
			scanRows(image, finderPatterns, rowScanned, 0, 1, 0, 1);
		}
		// We still haven't found enough finder patterns abort.
		if (finderPatterns.size() < 3) {
			throw new FinderPatternException();
		}

		// copy the finder patterns found into an array and ditch the vector.
		FinderPattern[] fp = new FinderPattern[finderPatterns.size()];
		finderPatterns.copyInto(fp);
		sortFinderPatterns(fp);
		orderFinderPatterns(fp);
		DetectInfo info = DetectInfo.getInstance();
		info.setFinderPattern(fp);
		return fp;
	}

	/**
	 * Scans a {@link MonochromeImage} row by row for occurrences of b:w:bbb:w:b
	 * pixel sequences.
	 * 
	 * @param image
	 *            {@link MonochromeImage} to scan.
	 * @param finderPatterns
	 *            TODO
	 * @param xSkip
	 *            Number of pixels to skip.
	 */
	private static void scanRows(MonochromeImage image, Vector finderPatterns, boolean[] scanned, int xStart, int xSkip, int yStart, int ySkip)
	{
		int w = image.getWidth();
		int h = image.getHeight();

		int cs = 3 * xSkip;

		for (int y = yStart; y < h; y += ySkip) {
			if (scanned[y]) continue;
			/* scan row if it consists of black and white pixels */
			for (int x = 0; x < w; x += xSkip) {
				try {
					if (!image.get(x, y) && !blackRowSegment(image, x, x + cs, y)) continue;
					scanRow(image, finderPatterns, y, x - xSkip < 1 ? 0 : x - xSkip, w);
					scanned[y] = true;
					break;
				} catch (ArrayIndexOutOfBoundsException e) {

				}
			}
		}
	}

	/**
	 * Returns true if all pixels in a segment of a row are black pixels.
	 * 
	 * @param x0
	 *            Segment start.
	 * @param x1
	 *            Segment end.
	 * @param y
	 *            Segment row.
	 * @return True if all pixels in the segment are black pixels.
	 */
	private static boolean blackRowSegment(MonochromeImage image, int x0, int x1, int y)
	{

		x0 = 0 <= x0 ? x0 : 0;
		int w = image.getWidth();

		x1 = x1 < w ? x0 : w;

		for (int x = x0; x < x1; x++) {
			if (!image.get(x, y)) return false;
		}
		return true;
	}

	/**
	 * Scans a row while looking for a pixel pattern of b:w:b:w:b.
	 * 
	 * @param finderPatterns
	 *            TODO
	 * @param y
	 *            Row y coordinate.
	 * @param x0
	 *            Search from x coordinate.
	 * @param x1
	 *            Search to x coordinate.
	 */
	private static void scanRow(MonochromeImage image, Vector finderPatterns, int y, int x0, int x1)
	{
		int state = 0;
		int px = 0; // pattern start coordinate
		int[] pattern = new int[5];
		boolean found = false;
		/*
		 * We scan a row looking for occurrences of b:w:bbb:w:b pixel blocks.
		 * When such a pixel pattern is found, we evaluate it.
		 */
		for (int x = x0; x < x1 && !found; x++) {
			/* black pixel found */
			if (image.get(x, y)) {
				/*
				 * We are in state 1 or 3, and now we're starting to count black
				 * pixels, change state.
				 */
				if (state == 1 || state == 3) {
					state++;
				}

				/*
				 * we are in state 0 and we haven't counted any pixels yet.
				 */
				if (state == 0 && pattern[state] == 0) {
					// System.out.println(y + ": px = " + x);
					px = x;
				}

				/* we are in state 0 or 2 or 4, keep counting. */
				if (state == 0 || state == 2 || state == 4) {
					pattern[state]++;
				}
				continue;
			}
			/* white pixel found */
			/*
			 * We are in state 0 or state 2, and now we're starting to count
			 * white pixels, change state.
			 */
			if ((state == 0 && pattern[0] != 0) || state == 2) {
				state++;

			}

			if (state == 4) {
				if (finderPatternProportions(pattern)) {
					evaluateFinderPattern(image, finderPatterns, px, y, pattern);
				}
				state = 3;
				px = pattern[0] + pattern[1] + px;
				/* shifts the pixels counts */
				pattern[0] = pattern[2];
				pattern[1] = pattern[3];
				pattern[2] = pattern[4];
				pattern[3] = 1;
				pattern[4] = 0;
			} else
			/* counting white pixels, "white states" are uneven */
			if (state % 2 == 1) {
				pattern[state]++;
			}
		}
	}

	/**
	 * Returns true if a b:w:b:w:b pixel block pattern has the proportions to be
	 * a possible finder pattern (b:w:bbb:w:b).
	 * 
	 * @param p
	 *            Pattern to verify.
	 * @return true if a b:w:b:w:b pixel block pattern has the proportions to be
	 *         a possible finder pattern (b:w:bbb:w:b).
	 */
	private static boolean finderPatternProportions(int[] p)
	{
		int shift = 8; // shift all calculations 8 bits to get better precision.
		int pixelCount = 0;
		for (int i = 0; i < 5; i++) {
			// int j = p[i];
			if (p[i] == 0) { // empty block, that's not good.
				return false;
			}
			pixelCount += p[i];
		}

		if (pixelCount < 7) { // not enough pixels to form a finder pattern.
			return false;
		}

		// estimate module size
		int ms = (pixelCount << shift) / 7;
		/*
		 * calculate a max variance, that gives us a difference between the
		 * estimated module size and the actual module size of 50%.
		 */
		int maxVar = ms >> 1;

		return Math.abs(ms - (p[0] << shift)) < maxVar && Math.abs(ms - (p[1] << shift)) < maxVar && Math.abs(3 * ms - (p[2] << shift)) < maxVar
				&& Math.abs(ms - (p[3] << shift)) < maxVar && Math.abs(ms - (p[4] << shift)) < maxVar;
	}

	/**
	 * Evaluates a possible finder pattern by first searching for a crossing
	 * finder pattern. If a crossing finder pattern is found, the newly found
	 * finder pattern is compared to all other found finder patterns. If there
	 * is no match the newly found finder pattern is added to the list of
	 * potential finder patterns, if there is match, the finder pattern is added
	 * to the match.
	 * 
	 * @param image
	 *            TODO
	 * @param finderPatterns
	 *            TODO
	 * @param x
	 *            Finder pattern x start coordinate.
	 * @param y
	 *            Finder pattern y coordinate.
	 * @param p
	 *            Finder pattern pixel count array.
	 * @return True if the found pattern is evaluated to be a potential finder
	 *         pattern.
	 */
	private static boolean evaluateFinderPattern(MonochromeImage image, Vector finderPatterns, int x, int y, int[] p)
	{
		boolean lookLeft = true;
		int cx = x + patternCenter(p); // center x coordinate.
		int cy = scanColFromCenter(image, cx, y, p); // center y coordinate.
		int minX = x + p[0] + p[1]; // search space start
		int maxX = minX + p[2]; // search space end
		int lx = cx - 1; // left of center coordinate.
		int rx = cx + 1; // right of center coordinate.

		/*
		 * Keep looking on each side of the center x coordinate until we find a
		 * crossing finder pattern or until we run out of search space.
		 */
		while (cy == Integer.MIN_VALUE) {
			lookLeft = !lookLeft; // switch direction
			if (lx <= minX && maxX <= rx) return false;

			if (lookLeft) {
				cy = (minX < lx) ? scanColFromCenter(image, lx--, cy, p) : cy;
				continue;
			}
			cy = (rx < maxX) ? scanColFromCenter(image, rx++, cy, p) : cy;
		}

		/*
		 * Found a new potential finder pattern, compare it to the other finder
		 * patterns.
		 */
		double estModuleSize = patternLenght(p) / 7.0;
		// FinderPattern fp;
		int l = finderPatterns.size();
		for (int i = 0; i < l; i++) {
			// fp = (FinderPattern) finderPatterns.elementAt(i);

			/*
			 * new found finder pattern is almost equal to another finder
			 * pattern, increase the other finder patterns density and quit
			 * looking.
			 */

			if (((FinderPattern) finderPatterns.elementAt(i)).absorb(estModuleSize, cx, cy)) return true;

			// if (fp.almostEqual(estModuleSize, cx, cy)) {
			// fp.incDensity();
			// return true;
			// }
		}

		/*
		 * new found finder pattern didn't match any of the already found finder
		 * patterns.
		 */
		finderPatterns.addElement(new FinderPattern(cx, cy, estModuleSize));
		return true;
	}

	/**
	 * Scans a column from the center of row finder pattern. If a finder
	 * pattern-like pattern is found, it's center y coordinate is returned,
	 * otherwise {@link Integer#MIN_VALUE} is returned.
	 * 
	 * @param image
	 *            TODO
	 * @param cx
	 *            Center x coordinate.
	 * @param cy
	 *            Center y coordinate.
	 * @param p
	 *            Pattern pixel count array.
	 * 
	 * @return Finder pattern center y coordinate if successful and
	 *         {@link Integer#MIN_VALUE} otherwise.
	 */
	private static int scanColFromCenter(MonochromeImage image, int cx, int cy, int[] p)
	{
		// System.out.println("scanColFromCenter cx:" + cx + " cy:" + cy);

		int w = image.getWidth();
		int h = image.getHeight();

		if (!(0 <= cx && cx < w && 0 <= cy && cy < h)) return Integer.MIN_VALUE;

		if (!image.get(cx, cy)) return Integer.MIN_VALUE;

		int[] pattern = new int[5];

		int minY = 0;
		int maxY = h - 1;
		int fromY = 0;

		int y = cy;
		while (y >= minY && image.get(cx, y)) {
			pattern[2]++;
			y--;
		}

		if (y < minY) return Integer.MIN_VALUE;
		while (y >= minY && !image.get(cx, y)) {
			pattern[1]++;
			y--;
		}

		if (pattern[1] == 0 || y < minY) return Integer.MIN_VALUE;
		while (y >= minY && image.get(cx, y)) {
			pattern[0]++;
			y--;
		}

		fromY = y + 1;
		y = cy + 1;

		if (pattern[0] == 0 || y > maxY) return Integer.MIN_VALUE;
		while (y <= maxY && image.get(cx, y)) {
			pattern[2]++;
			y++;
		}

		if (pattern[2] == 0 || y > maxY) return Integer.MIN_VALUE;
		while (y <= maxY && !image.get(cx, y)) {
			pattern[3]++;
			y++;
		}

		if (pattern[3] == 0 || y > maxY) return Integer.MIN_VALUE;
		while (y <= maxY && image.get(cx, y)) {
			pattern[4]++;
			y++;
		}

		if (finderPatternProportions(pattern)) return fromY + patternCenter(pattern);
		return Integer.MIN_VALUE;
	}

	/**
	 * Returns the length of b:w:b:w:b pattern.
	 * 
	 * @param p
	 *            Pattern to get length of.
	 * @return Length of pattern.
	 */
	private static int patternLenght(int[] p)
	{
		return p[0] + p[1] + p[2] + p[3] + p[4];
	}

	/**
	 * Returns the center coordinate for a finder pattern line.
	 * 
	 * @param p
	 *            Pattern array.
	 * @return Center coordinate.
	 */
	private static int patternCenter(int[] p)
	{
		return patternLenght(p) - p[4] - p[3] - (p[2] >> 1);
	}

	/**
	 * Sorts the finder patterns based on their density.
	 * 
	 * @param fp
	 */
	private static void sortFinderPatterns(FinderPattern[] fp)
	{
		int l = fp.length;

		// insertion sort
		for (int i = 1; i < l; i++) {
			FinderPattern value = fp[i];
			int j = i - 1;
			while (j >= 0 && fp[j].getSize() < value.getSize()) {
				fp[j + 1] = fp[j];
				j--;
			}
			fp[j + 1] = value;
		}
	}

	/**
	 * <p>
	 * Orders the finder patterns such that the finder pattern array contains:
	 * <ul>
	 * <li>Index 0 = Upper left finder pattern</li>
	 * <li>Index 1 = Upper right finder pattern</li>
	 * <li>Index 2 = Lower left finder pattern</li>
	 * </ul>
	 * </p>
	 * 
	 * @param fp
	 */
	private static void orderFinderPatterns(FinderPattern[] fp)
	{
		FinderPattern fpA = fp[0];
		FinderPattern fpB = fp[1];
		FinderPattern fpC = fp[2];

		double ab = finderPatternDistance(fpA, fpB);
		double ac = finderPatternDistance(fpA, fpC);
		double bc = finderPatternDistance(fpB, fpC);

		/* Vector ab is the longest => c is the upper left finder pattern. */
		if (ab > ac && ab > bc) {
			fp[0] = fpC;
			// guessing the rest
			fp[1] = fpA;
			fp[2] = fpB;
		}

		/* Vector ac is the longest => b is the upper left finder pattern. */
		if (ac > ab && ac > bc) {
			fp[0] = fpB;
			// guessing the rest
			fp[1] = fpA;
			fp[2] = fpC;
		}

		/* Vector bc is the longest => a is the upper left finder pattern. */
		if (bc > ab && bc > ac) {
			fp[0] = fpA;
			// guessing the rest
			fp[1] = fpB;
			fp[2] = fpC;
		}

		if (crossProductZ(vector(fp[0], fp[1]), vector(fp[0], fp[2])) < 0) {
			// swap b and c
			FinderPattern tmp = fp[1];
			fp[1] = fp[2];
			fp[2] = tmp;
		}
	}

	/**
	 * Returns the distance between two finder pattern centers.
	 * 
	 * @param fp0
	 *            Finder pattern 0
	 * @param fp1
	 *            Finder pattern 1
	 * @return Distance between finder pattern 0 and finder pattern 1 centers.
	 */
	private static double finderPatternDistance(FinderPattern fp0, FinderPattern fp1)
	{
		double x = fp0.getX() - fp1.getX();
		double y = fp0.getY() - fp1.getY();
		return Mext.vectorLength(x, y);
	}

	/**
	 * Returns the cross product z coordinate.
	 * 
	 * @param a
	 *            Vector a.
	 * @param b
	 *            Vector b.
	 * @return Cross product z coordinate.
	 */
	private static double crossProductZ(double[] a, double[] b)
	{
		return a[0] * b[1] - a[1] * b[0];
	}

	/**
	 * @param fp0
	 *            Finder pattern a.
	 * @param fp1
	 *            Finder pattern b.
	 * @return Vector between the finder pattern a and finder pattern b's center
	 *         coordinates.
	 */
	private static double[] vector(FinderPattern fp0, FinderPattern fp1)
	{
		int x = fp1.getX() - fp0.getX();
		int y = fp1.getY() - fp0.getY();
		return new double[] { x, y };
	}

	// private static void findEdgeCoordinates(FinderPattern[] fp)
	// {
	// // finder pattern 0's lower border center and border edge coordinates
	//
	// // int[][] fp0lower = getCe
	//
	// }

}