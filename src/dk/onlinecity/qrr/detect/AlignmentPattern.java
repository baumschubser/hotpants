/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */
package dk.onlinecity.qrr.detect;

import dk.onlinecity.qrr.util.Mext;

/**
 * This class represents a alignment pattern. This class is used in the
 * detection part.
 * 
 * @author Thomas Duerlund (td@onlinecity.dk)
 */
public class AlignmentPattern
{
	private int x;
	private int y;
	private double moduleSize;
	private int denisty;

	public AlignmentPattern(int x, int y, double moduleSize)
	{
		this.x = x;
		this.y = y;
		this.moduleSize = moduleSize;
		denisty = 1;
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public void incDensity()
	{
		denisty++;
	}

	public int getDensity()
	{
		return denisty;
	}

	/**
	 * Returns the distance to the alignment pattern line.
	 * 
	 * @param x0
	 *            Alignment pattern line start x coordinate.
	 * @param y0
	 *            Alignment pattern line start y coordinate.
	 * @param x1
	 *            Alignment pattern line end x coordinate.
	 * @param y1
	 *            Alignment pattern line end y coordinate.
	 * @return Distance to the alignment pattern line.
	 */
	public double distToAlignmentPatternLine(int x0, int y0, int x1, int y1)
	{
		double[] a = new double[] { x1 - x0, y1 - y0 };
		double[] b = new double[] { x - x0, y - y0 };
		double scalar = Mext.dotProduct(a, b) / (Mext.dotProduct(a, a));
		// return the length of the vector b's projection onto a -> b.
		return Mext.vectorLength(scalar * a[0] - b[0], scalar * a[1] - b[1]);
	}

	public boolean almostEqual(double moduleSize, int x, int y)
	{
		return Math.abs(x - this.x) <= moduleSize && Math.abs(y - this.y) <= moduleSize && (Math.abs(moduleSize - this.moduleSize) <= 1.0);
	}
}
