/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.detect;

import java.util.Vector;


import dk.onlinecity.qrr.DetectInfo;
import dk.onlinecity.qrr.image.MonochromeImage;
import dk.onlinecity.qrr.util.Mext;
import dk.onlinecity.qrr.util.Utils;

final class AlignmentPatternLocator
{
	private AlignmentPatternLocator()
	{

	}

	final static int[] detect(MonochromeImage image, int provisionalVersion, double[] moduleSizeExtremes)
	{
		// All QR codes except version 1 has at least one alignment pattern.
		if (provisionalVersion < 2) return null;

		double minModuleSize = moduleSizeExtremes[0];
		double maxModuleSize = moduleSizeExtremes[1];
		int ms = (int) minModuleSize >> 1;
		ms = ms < 1 ? 1 : ms;

		// generate a vector of coordinates to search for alignment patters
		Vector apl = getAlignmentPatternSearchLine(0, 0, image.getWidth(), image.getHeight());
		Vector alignmentPatterns = new Vector();

		scan(image, apl, alignmentPatterns, Mext.round(maxModuleSize * 14));

		if (alignmentPatterns.size() < 0) {
			return null;
		}

		int[] apl0 = (int[]) apl.elementAt(0);
		int[] apl1 = (int[]) apl.elementAt(apl.size() - 1);

		int l = alignmentPatterns.size();
		double minDist = Double.MAX_VALUE;
		int x = 0;
		int y = 0;
		double dist;

		AlignmentPattern ap;

		for (int i = 0; i < l; i++) {
			ap = (AlignmentPattern) alignmentPatterns.elementAt(i);
			dist = ap.distToAlignmentPatternLine(apl0[0], apl0[1], apl1[0], apl1[1]);

			if (dist > minDist) continue;

			// if (minDist > dist) {
			minDist = dist;
			x = ap.getX();
			y = ap.getY();
			// }
		}
		return new int[] { x, y };
	}

	/**
	 * @param xMin
	 *            Minimum x value.
	 * @param yMin
	 *            Minimum y value.
	 * @param xMax
	 *            Maximum x value.
	 * @param yMax
	 *            Maximum y value.
	 * @return
	 */
	private static Vector getAlignmentPatternSearchLine(int xMin, int yMin, int xMax, int yMax)
	{
		DetectInfo info = DetectInfo.getInstance();
		FinderPattern[] fp = info.getFinderPatterns();

		/* Find the lower right corner of finder pattern 0. */
		int[] fp0LowerRightCorner = Mext.vectorIntersection(info.getFp0LowerBorderEdge(), info.getFp1LowerBorderEdge(), info.getFp0RightBorderEdge(), info.getFp2RightBorderEdge());

		/*
		 * Find the alignment pattern line start point, by finding the
		 * intersection of vector a and b, where vector a is
		 * "finder pattern 2 center" -> "finder pattern 1 center" and vector b
		 * is "finder pattern 0 center" ->
		 * "lower left corner of finder pattern 0.". Then create a vector c
		 * ("finder pattern 0 center" -> "intersection point") and multiply it
		 * with a arbitrary scalar, and then finder pattern 0's center is added,
		 * and we have our alignment pattern line start point.
		 */
		int[] intersection = Mext.vectorIntersection(new int[] { fp[0].getX(), fp[0].getY() }, fp0LowerRightCorner, new int[] { fp[1].getX(), fp[1].getY() },
				new int[] { fp[2].getX(), fp[2].getY() });
		int[] c = new int[] { intersection[0] - fp[0].getX(), intersection[1] - fp[0].getY() };
		int[] apl0 = new int[] { fp[0].getX() + (int) (1.4 * c[0]), fp[0].getY() + (int) (1.4 * c[1]) };
		/*
		 * Find the alignment pattern line end point, by finding multiplying the
		 * vector "finder pattern 0 center" -> "intersection point" with a
		 * scalar, and the adding it to the alignment pattern line start point.
		 */
		double s = 1; // scalar
		int[] apl1 = new int[] { apl0[0] + (int) (s * c[0]), apl0[1] + (int) (s * c[1]) };

		return Utils.bresenhamLine(apl0[0], apl0[1], apl1[0], apl1[1], xMin, yMin, xMax, yMax);
	}

	/**
	 * Scans the image for alignment patterns.
	 * 
	 * @param image
	 *            {@link MonochromeImage} image to scan.
	 * @param apl
	 *            Alignment pattern line.
	 * @param r
	 *            Scan "radius".
	 */
	private static void scan(MonochromeImage image, Vector apl, Vector alignmentPatterns, int r)
	{
		int w = image.getWidth();
		int l = apl.size();
		int[] p;
		int x0, x1;
		for (int i = 0; i < l; i++) {
			p = (int[]) apl.elementAt(i);
			x0 = 0 <= p[0] - r ? p[0] - r : 0;
			x1 = p[0] + r < w ? p[0] + r : w;
			scanRow(image, alignmentPatterns, x0, x1, p[1], 0, image.getHeight());
		}
	}

	/**
	 * Scans a "region row" of a {@link MonochromeImage} for occurrences of
	 * w:b:w pixel patterns.
	 * 
	 * @param image
	 *            {@link MonochromeImage} to scan.
	 * @param x0
	 *            Region start x coordinate.
	 * @param x1
	 *            Region stop x coordinate.
	 * @param y
	 *            Row to scan.
	 */
	private static void scanRow(MonochromeImage image, Vector alignmentPatterns, int x0, int x1, int y, int y0, int y1)
	{
		int state = 0; //
		int px = x0; // pattern start coordinate
		int[] pattern = new int[3];

		for (int x = x0; x < x1; x++) {
			/* white pixel found */

			if (!image.get(x, y)) {
				/* black pixel found */
				/* we just came from state 1, change state. */
				if (state == 1) state++;
				/* we are in state 0 or 2, keep counting. */
				if (state == 0 || state == 2) {
					pattern[state]++;
				}
				continue;
			}

			/* found black pixel */

			/*
			 * we are in state 0, and now we're starting to count black pixels,
			 * change state.
			 */
			if (state == 0) {
				state++;
			}

			/* We are in state 1, keep counting. */
			if (state == 1) {
				pattern[state]++;
			}

			/*
			 * We are in state 2, and now we're starting to count black pixels
			 * again, evaluate pattern found so far.
			 */
			if (state == 2) {
				if (alignmentPatternProportions(pattern)) {
					evaluateAlignmentPattern(image, alignmentPatterns, px, y, y0, y1, pattern);
					// drawLine(px, x, y, 0xff0000, 0xfff000);
				}

				state = 1;
				px = pattern[0] + pattern[1] + px;
				pattern[0] = pattern[2];
				pattern[1] = 1;
				pattern[2] = 0;
			}
		}
	}

	/**
	 * Compares two alignment patterns and returns true if they are similar.
	 * 
	 * @param p0
	 *            Pattern 0.
	 * @param p1
	 *            Pattern 1.
	 * @return True if the two patterns are similar.
	 */
	private static boolean comparePatterns(int[] p0, int[] p1)
	{
		int shift = 8;

		int pixelCountP0 = patternLength(p0);
		int estMsP0 = (pixelCountP0 << shift) / 2;
		int maxVar = estMsP0 >> 1;

		return Math.abs((p0[0] << shift) - (p1[0] << shift)) < maxVar && Math.abs((p0[0] << shift) - (p1[1] << shift)) < maxVar
				&& Math.abs((p0[0] << shift) - (p1[2] << shift)) < maxVar &&

				Math.abs((p0[1] << shift) - (p1[0] << shift)) < maxVar && Math.abs((p0[1] << shift) - (p1[1] << shift)) < maxVar
				&& Math.abs((p0[1] << shift) - (p1[2] << shift)) < maxVar &&

				Math.abs((p0[2] << shift) - (p1[0] << shift)) < maxVar && Math.abs((p0[2] << shift) - (p1[1] << shift)) < maxVar
				&& Math.abs((p0[2] << shift) - (p1[2] << shift)) < maxVar;

	}

	private static boolean alignmentPatternProportions(int[] p)
	{
		int shift = 8;
		int pixelCount = 0;
		for (int i = 0; i < 3; i++) {
			int j = p[i];
			if (j == 0) return false;
			pixelCount += p[i];
		}

		if (pixelCount < 3) {
			return false;
		}

		int ms = (pixelCount << shift) / 3;
		int mv = ms >> 1;

		return Math.abs(ms - (p[0] << shift)) < mv && Math.abs(ms - (p[1] << shift)) < mv && Math.abs(ms - (p[2] << shift)) < mv;
	}

	/**
	 * Evaluates a possible alignment pattern. If pattern is found to be a
	 * possible alignment pattern it is compared to all previous found alignment
	 * pattern candidates, and appended if there are enough similarities.
	 * 
	 * @param x
	 * @param y
	 * @param y0
	 * @param y1
	 * @param p
	 * @return
	 */
	private static boolean evaluateAlignmentPattern(MonochromeImage image, Vector alignmentPatterns, int x, int y, int y0, int y1, int[] p)
	{
		double estMsSize = patternLength(p) / 3;
		if (!blackBorder(image, p, estMsSize, x, y)) {
			return false;
		}
		boolean failed = false;
		boolean lookLeft = true;
		int cx = x + patternCenter(p);
		int cy = scanColFromCenter(image, cx, y, y0, y1, p);
		int minX = x + p[0];
		int maxX = minX + p[1];
		int lx = cx - 1;
		int rx = cx + 1;

		while (cy == Integer.MIN_VALUE && !failed) {
			// failed
			if (lx <= minX && maxX <= rx) return false;

			if (lookLeft) {
				cy = minX < lx ? scanColFromCenter(image, lx--, cy, y0, y1, p) : cy;
			} else {
				cy = rx < maxX ? scanColFromCenter(image, rx++, cy, y0, y1, p) : cy;
			}
			lookLeft = !lookLeft;
		}

		int l = alignmentPatterns.size();
		AlignmentPattern ap;
		for (int i = 0; i < l; i++) {
			ap = (AlignmentPattern) alignmentPatterns.elementAt(i);
			// alignment pattern with close recemplence found, increase its
			// density.
			if (ap.almostEqual(estMsSize, cx, cy)) {
				ap.incDensity();
				return true;
			}
		}

		// if we get here, we have found a new alignment pattern candidate
		alignmentPatterns.addElement(new AlignmentPattern(cx, cy, estMsSize));
		return true;
	}

	/**
	 * 
	 * @param image
	 * @param cx
	 * @param cy
	 * @param y0
	 * @param y1
	 * @param p
	 * @return
	 */
	private static int scanColFromCenter(MonochromeImage image, int cx, int cy, int y0, int y1, int[] p)
	{
		int w = image.getWidth();
		int h = image.getHeight();

		if (!(0 <= cx && cx < w && 0 <= cy && cy < h)) {
			return Integer.MIN_VALUE;
		}

		if (!image.get(cx, cy)) return Integer.MIN_VALUE;

		y0 = y0 < 0 ? 0 : y0;
		y1 = y1 > image.getWidth() ? image.getWidth() : y1;

		int y = cy;
		int fromY = 0;

		int[] pattern = new int[3];

		// scan up from center until we hit a white pixel
		while (y >= y0 && image.get(cx, y)) {
			pattern[1]++;
			y--;
		}

		if (y < y0) return Integer.MIN_VALUE;

		// keep scanning up until we hit a black pixel
		while (y >= y0 && !image.get(cx, y)) {
			pattern[0]++;
			y--;
		}

		if (y < y0) return Integer.MIN_VALUE;

		// save y value
		fromY = y + 1;
		y = cy + 1;

		// abort if we didn't count any white pixels
		if (pattern[0] == 0) return Integer.MIN_VALUE;

		// scan down from center until we hit a white pixel or hits image edge.
		while (y < y1 && image.get(cx, y)) {
			pattern[1]++;
			y++;
		}

		if (y >= y1) return Integer.MIN_VALUE;

		// keep scanning down until we hit a black pixel
		while (y < y1 && !image.get(cx, y)) {
			pattern[2]++;
			y++;
		}

		if (y >= y1) return Integer.MIN_VALUE;

		if (alignmentPatternProportions(pattern) && comparePatterns(p, pattern)) {
			return fromY + patternCenter(pattern);
		}

		return Integer.MIN_VALUE;
	}

	/**
	 * Returns true if a possible alignment pattern center is surrounded by at
	 * least one black module of the estimated module size.
	 * 
	 * @param p
	 *            Pattern.
	 * @param estMsSize
	 *            Estimated module size.
	 * @param x
	 *            Pattern start x coordinate.
	 * @param y
	 *            Pattern y coordinate.
	 * @return True if the possible alignment pattern center is surrounded by at
	 *         least one black module of the estimated module size.
	 */
	private static boolean blackBorder(MonochromeImage image, int[] p, double estMsSize, int x, int y)
	{
		int leftBlackBlock = 0;
		int rightBlockBlock = 0;
		int xx = x - 1;
		while (xx >= 0 && image.get(xx, y)) {
			leftBlackBlock++;
			xx--;
		}

		if (leftBlackBlock < estMsSize) {
			return false;
		}

		xx = x + patternLength(p);

		while (xx < image.getWidth() && image.get(xx, y)) {
			rightBlockBlock++;
			xx++;
		}

		if (rightBlockBlock < estMsSize) {
			return false;
		}

		return true;
	}

	private static int patternLength(int[] p)
	{
		return p[0] + p[1] + p[2];
	}

	private static int patternCenter(int[] p)
	{
		return patternLength(p) - p[2] - (p[1] >> 1);
	}
}
