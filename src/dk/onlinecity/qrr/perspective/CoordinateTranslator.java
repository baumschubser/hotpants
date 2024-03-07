/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.perspective;

/**
 * The <code>CoordinateTranslator</code> translates (x, y) coordinates to (u, v)
 * in the 2D coordinate system, using a 3x3 transition matrix. The constructor
 * takes this transition matrix as input. The creation of the transition matrix
 * is handled by the {@link PerspectiveTransformer}.
 * 
 * The <code>CoordinateTranslator</code> takes a 3 x 3 transition matrix as
 * input.
 * 
 * @author Thomas Duerlund (td@onlinecity.dk)
 * 
 */
public class CoordinateTranslator
{
	private double[] c;
	private int c20;
	private int c21;

	public CoordinateTranslator(double[] coefficients)
	{
		this(coefficients, 0, 0);
	}

	/**
	 * Constructs a new CoordinateTranslator.
	 * 
	 * @param coefficients
	 *            Transition matrix coefficients.
	 */
	public CoordinateTranslator(double[] coefficients, int w, int h)
	{
		c = coefficients;
		c20 = coefficients.length - 2;
		c21 = coefficients.length - 1;
	}

	/**
	 * Returns the u coordinate translated through the input transition matrix.
	 * 
	 * @param x
	 *            X-coordinate.
	 * @param y
	 *            Y-coordinate.
	 * @return U-coordinate.
	 */
	public double getU(int x, int y)
	{
		return calc(c[0], c[1], c[2], x, y);
	}

	/**
	 * Returns the v coordinate translated through the input transition matrix.
	 * 
	 * @param x
	 *            X-coordinate.
	 * @param y
	 *            Y-coordinate.
	 * @return V-coordinate.
	 */
	public double getV(int x, int y)
	{
		return calc(c[3], c[4], c[5], x, y);
	}

	public int[] get(int x, int y)
	{
		return new int[] { (int) getU(x, y), (int) getV(x, y) };
	}

	public int[] get(int[] a)
	{
		return new int[] { (int) getU(a[0], a[1]), (int) getV(a[0], a[1]) };
	}

	private double calc(double cX0, double cX1, double cX2, int x, int y)
	{
		double d = (cX0 * x + cX1 * y + cX2) / (c[c20] * x + c[c21] * y + 1);
		return d;
	}
}
