/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr;

import dk.onlinecity.qrr.perspective.CoordinateTranslator;

/**
 * This class represents a QR-Code candidate. It holds the QR-Code candidate's
 * corner coordinates and threshold value.
 * 
 * @author Thomas Duerlund (td@onlinecity.dk)
 */
public final class QrCandidate
{
	/** Threshold value. */
	private final int threshold;
	/** Estimated number of modules. */
	private final int totalEstModules;
	/** Multiplier used to */
	private final int multiplier;
	/** Coordinate translator */
	private final CoordinateTranslator ct;
	/** Upper left corner. */
	private final int[] ul;
	/** Upper right corner. */
	private final int[] ur;
	/** Lower left corner. */
	private final int[] ll;
	/** Lower right corner. */
	private final int[] lr;

	public QrCandidate(int threshold, int totalEstModules, int mult, CoordinateTranslator ct)
	{
		this.threshold = threshold;
		this.totalEstModules = totalEstModules;
		this.multiplier = mult;
		this.ct = ct;

		int size = totalEstModules * mult;
		ul = new int[] { (int) ct.getU(0, 0), (int) ct.getV(0, 0) };
		ur = new int[] { (int) ct.getU(size, 0), (int) ct.getV(size, 0) };
		ll = new int[] { (int) ct.getU(0, size), (int) ct.getV(0, size) };
		lr = new int[] { (int) ct.getU(size, size), (int) ct.getV(size, size) };
	}

	public final int getThreshold()
	{
		return threshold;
	}

	public final int getTotalEstModules()
	{
		return totalEstModules;
	}

	public final int getMultiplier()
	{
		return multiplier;
	}

	public final CoordinateTranslator getCoordinateTranslator()
	{
		return ct;
	}

	/**
	 * Returns the upper left corner.
	 * 
	 * @return Upper left corner.
	 */
	public final int[] getUl()
	{
		return ul;
	}

	/**
	 * Returns the upper right corner.
	 * 
	 * @return Upper right corner.
	 */
	public final int[] getUr()
	{
		return ur;
	}

	/**
	 * Returns the lower left corner.
	 * 
	 * @return Lower left corner.
	 */
	public final int[] getLl()
	{
		return ll;
	}

	/**
	 * Returns the lower right corner.
	 * 
	 * @return Lower right corner.
	 */
	public final int[] getLr()
	{
		return lr;
	}

	public final void print()
	{
		System.out.println("Threshold:" + threshold);
		System.out.println(cStr("ul", getUl()));
		System.out.println(cStr("ur", getUr()));
		System.out.println(cStr("ll", getLl()));
		System.out.println(cStr("lr", getLr()));
		System.out.println("--");
	}

	private final String cStr(String name, int[] c)
	{
		return name + ": " + c[0] + ", " + c[1];
	}
}
