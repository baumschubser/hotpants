/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.detect;

import java.util.Vector;

import dk.onlinecity.qrr.DetectInfo;
import dk.onlinecity.qrr.FatalException;
import dk.onlinecity.qrr.QrCandidate;
import dk.onlinecity.qrr.core.exceptions.QrrException;
import dk.onlinecity.qrr.image.ImageUtil;
import dk.onlinecity.qrr.image.MonochromeImage;
import dk.onlinecity.qrr.perspective.CoordinateTranslator;
import dk.onlinecity.qrr.perspective.PerspectiveTransformer;
import dk.onlinecity.qrr.util.Mext;
import dk.onlinecity.qrr.util.Utils;

/**
 * This class can detect QR-Codes.
 * 
 * @author Thomas Duerlund (td@onlinecity.dk)
 * 
 */
public class QrDetector
{
	private static final int X = 0;
	private static final int Y = 1;

	private static final double FINDER_PATTERN_OFFSET = 3.5;
	private static final double ALIGNMENT_PATTERN_OFFSET = 6.5;

	private MonochromeImage image;
	private int thresholdStep;
	private int[] thresholdCandidates;
	private DetectInfo info;

	public QrDetector(MonochromeImage image)
	{
		this.image = image;
	}

	public QrDetector(MonochromeImage image, int[] thresholdCandidates)
	{
		this.image = image;
		this.thresholdCandidates = thresholdCandidates;
	}

	public QrDetector(int[] pixels, int w, int h, int gw, int gh)
	{
		ImageUtil iu = new ImageUtil(pixels, w, h, gw, gh);
		this.image = iu.getMonochromeImage();
		thresholdCandidates = iu.getThresholdValues();
		image.setThreshold(thresholdCandidates[0]);
		thresholdStep = 0;
	}

	public MonochromeImage getMonoImage()
	{
		return image;
	}

	public final QrCandidate[] detectCandidates()
	{
		return detectCandidates(this.image);
	}

	public final QrCandidate[] detectCandidates(MonochromeImage image) throws FinderPatternException
	{
		this.image = image;
		Vector candidates = new Vector();
		info = DetectInfo.getInstance();

		FinderPattern[] finderPatterns = FinderPatternLocator.detect(image);
		findFinderPatternBorderPixels(finderPatterns);
		int provisionalVersion = estimateProvisionalVersion();
		int totalEstModules = (provisionalVersion - 1) * 4 + 21;

		int[] ul = estCenterCoordinates(image, finderPatterns[0].getX(), finderPatterns[0].getY());
		int[] ur = estCenterCoordinates(image, finderPatterns[1].getX(), finderPatterns[1].getY());
		int[] ll = estCenterCoordinates(image, finderPatterns[2].getX(), finderPatterns[2].getY());
		int[] lr = AlignmentPatternLocator.detect(image, provisionalVersion, getModuleSizeExtremes(finderPatterns));

		int multiplier = 6;

		CoordinateTranslator ct;
		if (lr != null) {
			ct = translateCoordinates(image.getWidth(), image.getHeight(), ul, ur, ll, lr, multiplier, totalEstModules, false);
			candidates.addElement(new QrCandidate(image.getThreshold(), totalEstModules, multiplier, ct));
		}

		ct = translateCoordinates(image.getWidth(), image.getHeight(), ul, ur, ll, LastCornerGuess.p3(image, ul, ur, ll), multiplier, totalEstModules, true);
		candidates.addElement(new QrCandidate(image.getThreshold(), totalEstModules, multiplier, ct));

		ct = translateCoordinates(image.getWidth(), image.getHeight(), ul, ur, ll, guessLowerRight(ul, ur, ll), multiplier, totalEstModules, true);
		candidates.addElement(new QrCandidate(image.getThreshold(), totalEstModules, multiplier, ct));

		int l = candidates.size();
		QrCandidate[] qrCandidates = new QrCandidate[l];
		for (int i = 0; i < l; i++) {
			qrCandidates[i] = (QrCandidate) candidates.elementAt(i);
		}

		return qrCandidates;
	}

	private void findFinderPatternBorderPixels(FinderPattern[] fp)
	{
		DetectInfo info = DetectInfo.getInstance();
		/* Finder pattern 0's "lower" border center and border edge coordinates. */
		int[][] fp0Lower = getCenterAndOuterPixels(image, fp[0].getX(), fp[0].getY(), fp[2].getX() - fp[0].getX(), fp[2].getY() - fp[0].getY());
		/* Finder pattern 1's "lower" border center and border edge coordinates. */
		int[][] fp1Lower = getCenterAndOuterPixels(image, fp[1].getX(), fp[1].getY(), fp[2].getX() - fp[0].getX(), fp[2].getY() - fp[0].getY());
		/* Finder pattern 0's "right" border center and border edge coordinates. */
		int[][] fp0Right = getCenterAndOuterPixels(image, fp[0].getX(), fp[0].getY(), fp[1].getX() - fp[0].getX(), fp[1].getY() - fp[0].getY());
		/* Finder pattern 2's "right" border center and border edge coordinates. */
		int[][] fp2Right = getCenterAndOuterPixels(image, fp[2].getX(), fp[2].getY(), fp[1].getX() - fp[0].getX(), fp[1].getY() - fp[0].getY());

		info.setFp0LowerBorderCenter(fp0Lower[0]);
		info.setFp0LowerBorderEdge(fp0Lower[1]);
		info.setFp1LowerBorderCenter(fp1Lower[0]);
		info.setFp1LowerBorderEdge(fp1Lower[1]);

		info.setFp0RightBorderCenter(fp0Right[0]);
		info.setFp0RightBorderEdge(fp0Right[1]);
		info.setFp2RightBorderCenter(fp2Right[0]);
		info.setFp2RightBorderEdge(fp2Right[1]);
	}

	/**
	 * Returns two coordinate sets, the center of the outer black square of a
	 * finder pattern, and the first white pixel outside the finder pattern.
	 * Direction vector is needed, so we need which way to look.
	 * 
	 * @param image
	 * @param fx
	 *            Finder pattern center x coordinate.
	 * @param fy
	 *            Finder pattern center y coordinate.
	 * @param dx
	 *            Direction vector x coordinate.
	 * @param dy
	 *            Direction vector y coordinate.
	 * @return
	 */
	public int[][] getCenterAndOuterPixels(MonochromeImage image, int fx, int fy, int dx, int dy)
	{
		/*
		 * We start at the center of a finder pattern, so we scan until we hit
		 * the second black pixel block and return the coordinates of its
		 * center, and edge.
		 */

		Vector line = Utils.bresenhamLine(fx, fy, fx + dx, fy + dy, 0, 0, image.getWidth(), image.getHeight());

		int i0 = 0;
		int i1 = 0;
		int state = 0;

		int fromIndex = 0;
		int toIndex = line.size();
		int iStep = 1;

		int[] startPoint = (int[]) line.elementAt(fromIndex);
		boolean stop = false;

		/*
		 * iterate through the points backwards, this might still give some
		 * problems.
		 */
		if (startPoint[0] != fx || startPoint[1] != fy) {
			fromIndex = line.size() - 1;
			toIndex = 0;
			iStep = -1;
		}

		for (int i = fromIndex; (iStep > 0 ? i < toIndex : i >= toIndex) && !stop; i += iStep) {
			int[] p = (int[]) line.elementAt(i);
			/* black pixel */
			if (image.get(p[0], p[1])) {
				if (state == 1) {
					// mark this spot
					i0 = i;
					state++;
				}
			} else { /* white pixel */
				if (state == 0) {
					state++;
				}
				if (state == 2) {
					// stop
					i1 = i;
					state++;
					stop = true;
				}
			}
		}

		int[] center = iStep < 0 ? (int[]) line.elementAt(((i0 - i1) >> 1) + i1 + 1) : (int[]) line.elementAt(((i1 - i0) >> 1) + i0);
		int[] edge = (int[]) line.elementAt(i1);
		return new int[][] { center, edge };
	}

	/**
	 * 
	 * @param w
	 *            Width.
	 * @param h
	 *            Height.
	 * @param p0
	 *            Upper left finder pattern center coordinates.
	 * @param p1
	 *            Upper right finder pattern center coordinates.
	 * @param p2
	 *            Lower left finder pattern center coordinates.
	 * @param p3
	 *            Lower right coordinates.
	 * @param multiplier
	 * @param totalEstModules
	 * @return
	 */
	private CoordinateTranslator translateCoordinates(int w, int h, int[] p0, int[] p1, int[] p2, int[] p3, int multiplier, int totalEstModules, boolean guessed)
	{
		int size = totalEstModules * multiplier;
		double finderPatternOffset = FINDER_PATTERN_OFFSET * (double) multiplier; // center
		// offset
		// System.out.println("guess:" + guessed);
		double lrOffset = guessed ? finderPatternOffset : ALIGNMENT_PATTERN_OFFSET * (double) multiplier;
		// double lrOffset = finderPatternOffset;

		CoordinateTranslator ct = PerspectiveTransformer.getCoordinateTranslator( /* */
		finderPatternOffset, finderPatternOffset, /* u0, v0 */
				size - finderPatternOffset, finderPatternOffset, /* u1, v1 */
				finderPatternOffset, size - finderPatternOffset, /* u2, v2 */
				size - lrOffset, size - lrOffset, /* u3, v3 */
				p0[X], p0[Y], /* x0, y0 */
				p1[X], p1[Y], /* x1, y1 */
				p2[X], p2[Y], /* x2, y2 */
				p3[X], p3[Y] /* x3, y3 */
		);

		/*
		 * Translate the coordinates.
		 */
		return ct;
	}

	public int getStep()
	{
		return thresholdStep;
	}

	public int getThreshold()
	{
		if (thresholdStep < thresholdCandidates.length) {
			return thresholdCandidates[thresholdStep];
		}

		return thresholdCandidates[thresholdCandidates.length - 1];
	}

	public void step() throws FatalException
	{
		if (thresholdStep < thresholdCandidates.length) {
			image.setThreshold(thresholdCandidates[thresholdStep++]);
		} else {
			throw new FatalException();
		}
	}

	/**
	 * Returns the minimum and maximum module sizes.
	 * 
	 * @param fp
	 *            Array of finder patterns.
	 * @return Minimum and maximum module sizes.
	 */
	private double[] getModuleSizeExtremes(FinderPattern[] fp)
	{
		DetectInfo info = DetectInfo.getInstance();
		double maxModuleSize = Double.MIN_VALUE;
		double minModuleSize = Double.MAX_VALUE;
		double moduleSize;
		for (int i = 0; i < 3; i++) {
			moduleSize = fp[i].getModuleSize();
			maxModuleSize = moduleSize > maxModuleSize ? moduleSize : maxModuleSize;
			minModuleSize = moduleSize < minModuleSize ? moduleSize : minModuleSize;
		}

		info.setMaxModuleSize(maxModuleSize);
		return new double[] { minModuleSize, maxModuleSize };
	}

	/**
	 * Estimates the provisional version number of a QR-Code by counting the
	 * modules in the timing patterns.
	 * 
	 * @return Estimated provisional version number.
	 */
	private int estimateProvisionalVersion()
	{
		DetectInfo info = DetectInfo.getInstance();
		int[] fp0 = info.getFp0LowerBorderCenter();
		int[] fp1 = info.getFp1LowerBorderCenter();
		Vector tp01 = Utils.bresenhamLine(fp0[0], fp0[1], fp1[0], fp1[1], 0, 0, image.getWidth(), image.getHeight());

		int shifts = 0;
		boolean black = true;
		int l = tp01.size();

		int[] p;

		for (int i = 0; i < l; i++) {
			p = (int[]) tp01.elementAt(i);

			if (image.get(p[X], p[Y])) {
				if (!black) {
					shifts++;
					black = true;
				}
			} else {
				if (black) {
					shifts++;
					black = false;
				}
			}
		}

		/* ((shift-- + 14 ) - 21) 1/4 + 1 */

		info.setProvisionalVersion((((shifts + 13) - 21) >> 2) + 1);
		return (((shifts + 13) - 21) >> 2) + 1;
	}

	/**
	 * Attempts to guess the lower right corner needed for the QR-Code
	 * extraction.
	 * 
	 * @param p0
	 * @param p1
	 * @param p2
	 * @return
	 */
	private int[] guessLowerRight(int[] p0, int[] p1, int[] p2)
	{
		int[] fp0ll = Mext.vectorIntersection(info.getFp0LowerBorderEdge(), info.getFp1LowerBorderEdge(), info.getFp0RightBorderEdge(), info.getFp2RightBorderEdge());

		// qrInfo.getFinderPattern0LowerLeft();
		int[] diagonal1 = new int[] { p2[X] - p1[X], p2[Y] - p1[Y] };
		int[] intersection = Mext.vectorIntersection(new int[] { p0[X], p0[Y] }, fp0ll, new int[] { p1[X], p1[Y] }, new int[] { p1[X] + diagonal1[X], p1[Y] + diagonal1[Y] });
		int[] diagonal0 = new int[] { intersection[X] - p0[X], intersection[Y] - p0[Y] };
		diagonal0 = new int[] { 2 * diagonal0[X], 2 * diagonal0[Y] };
		return new int[] { p0[X] + diagonal0[X], p0[Y] + diagonal0[Y] };
	}

	/**
	 * Given a point located inside a black pixel block, this returns the center
	 * of that black pixel block.
	 * 
	 * @param image
	 *            {@link MonochromeImage} containing the black pixel block.
	 * @param x
	 *            Starting point x coordinate.
	 * @param y
	 *            Starting point y coordinate.
	 * @return Center of a black pixel block.
	 * @throws QrrException
	 *             Thrown if the starting point is not a black pixel.
	 */
	private int[] estCenterCoordinates(MonochromeImage image, int x, int y) throws QrrException
	{
		int x1 = estVerticalCenterCoordinate(image, x, y);
		int y1 = estHorizontalCenterCoordinate(image, x1, y);
		x1 = estVerticalCenterCoordinate(image, x1, y1);
		return new int[] { x1, y1 };
	}

	/**
	 * Given a point located inside a black pixel block, this returns the
	 * vertical center of that black pixel block.
	 * 
	 * @param image
	 *            {@link MonochromeImage} containing the black pixel block.
	 * @param x
	 *            Starting point x coordinate.
	 * @param y
	 *            Starting point y coordinate.
	 * @return Vertical center of a black pixel block.
	 * @throws QrrException
	 *             Thrown if the starting point is not a black pixel.
	 */
	private int estVerticalCenterCoordinate(MonochromeImage image, int x, int y) throws QrrException
	{
		if (!image.get(x, y)) throw new QrrException();

		int xMax = image.getWidth();
		int count = 0;
		int xs = x;
		int start = x;

		// Scan left
		while (0 <= xs && image.get(xs, y)) {
			count++;
			xs--;
		}

		// We have reached the image edge, return the initial x.
		if (xs == -1) return x;

		// Scan right
		start = xs + 1;
		xs = x + 1;
		while (xs < xMax && image.get(xs, y)) {
			count++;
			xs++;
		}

		// We have reached the image edge, return the initial x.
		if (xs == xMax) return x;

		return start + (count >> 1);
	}

	/**
	 * Given a point located inside a black pixel block, this return the
	 * horizontal center of that black pixel block.
	 * 
	 * @param image
	 *            {@link MonochromeImage} containing the black pixel block.
	 * @param x
	 *            Starting point x coordinate.
	 * @param y
	 *            Starting point y coordinate.
	 * @return Horizontal center of a black pixel block.
	 * @throws QrrException
	 *             Thrown if the starting point is not a black pixel.
	 */
	private int estHorizontalCenterCoordinate(MonochromeImage image, int x, int y) throws QrrException
	{
		if (!image.get(x, y)) throw new QrrException();

		int yMax = image.getHeight();
		int count = 0;
		int ys = y;
		int start = y;

		// Scan up
		while (0 <= ys && image.get(x, ys)) {
			count++;
			ys--;
		}

		// We have reached the image edge, return the initial y.
		if (ys == -1) {
			return y;
		}

		// Scan down
		start = ys + 1;
		ys = y + 1;
		while (ys < yMax && image.get(x, ys)) {
			count++;
			ys++;
		}

		// We have reached the image edge, return the initial y.
		if (ys == yMax) {
			return y;
		}

		return start + (count >> 1);
	}
}
