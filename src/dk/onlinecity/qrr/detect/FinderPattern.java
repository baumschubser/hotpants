/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.detect;

/**
 * Finder pattern implementation.
 * 
 * @author Thomas Duerlund (td@onlinecity.dk)
 */
public class FinderPattern
{
	/** Center x coordinate. */
	private int x;
	/** Center y coordinate. */
	private int y;
	/** Guessed module size. */
	private double ms;
	/** Number of b:w:bbb:w:b pixel lines this finder pattern consists of. */
	private int size;
	/** Max module size diviation. */
	private double msd = 1.0;

	/**
	 * Constructs a new <code>FinderPatternImp</code>.
	 * 
	 * @param x
	 *            Center x coordinate.
	 * @param y
	 *            Center y coordinate.
	 * @param ms
	 *            Module size.
	 */
	public FinderPattern(int x, int y, double ms)
	{
		this.x = x;
		this.y = y;
		this.ms = ms;
		size = 1;
	}

	/**
	 * Returns the center x coordinate.
	 * 
	 * @return Center x coordinate.
	 */
	public int getX()
	{
		return x;
	}

	/**
	 * Returns the center y coordinate.
	 * 
	 * @return Center y coordinate.
	 */
	public int getY()
	{
		return y;
	}

	/**
	 * Returns the average module size.
	 * 
	 * @return Module size.
	 */
	public double getModuleSize()
	{
		return ms;
	}

	/**
	 * Returns this finder patterns density.
	 * 
	 * @return
	 */
	public int getSize()
	{
		return size;
	}

	/**
	 * Compares this finder patterns with another potential finder patterns
	 * module size and center coordinates.
	 * 
	 * @param ms
	 *            Other finder patterns module size.
	 * @param x
	 *            Other finder patterns center x coordinate.
	 * @param y
	 *            Other finder patterns center y coordinate.
	 * @return Returns true if this finder pattern and the other finder pattern
	 *         are almost equal.
	 */
	private boolean almostEqual(double ms, int x, int y)
	{
		return Math.abs(x - this.x) <= ms && Math.abs(y - this.y) <= ms && (Math.abs(ms - this.ms) <= msd);
	}

	/**
	 * Absorbes a finder pattern if it has similiar properties as this finder
	 * pattern.
	 * 
	 * @param ms
	 *            Other finder pattern module size.
	 * @param x
	 *            Other finder pattern center x coordinate.
	 * @param y
	 *            Other finter pattern center y coodrinate.
	 * @return True if the other finder pattern is absorbed into this one.
	 */
	public boolean absorb(double ms, int x, int y)
	{
		if (!almostEqual(ms, x, y)) return false;

		size++;

		return true;
	}
}
