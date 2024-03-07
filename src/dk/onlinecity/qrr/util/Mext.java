/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.util;

/**
 * Mext is a static class of math library extensions.
 * 
 * @author Thomas Duerlund (td@onlinecity.dk)
 * 
 */
public class Mext
{
	/**
	 * Integer division.
	 * 
	 * @param a
	 *            Integer a
	 * @param b
	 *            Integer b
	 * @return a div b
	 */
	public static int div(int a, int b)
	{
		return (int) Math.floor((double) a / (double) b);
	}

	/**
	 * Returns the dot product of two vectors.
	 * 
	 * @param u0
	 *            Vector 0 u coordinate.
	 * @param v0
	 *            Vector 0 v coordinate.
	 * @param u1
	 *            Vector 1 u coordinate.
	 * @param v1
	 *            Vector 1 v coordinate.
	 * @return Dot product of vector 0 and vector 1.
	 */
	public static double dotProduct(double u0, double v0, double u1, double v1)
	{
		return (u0 * v0 + u1 * v1);
	}

	public static double dotProduct(double[] u, double[] v)
	{
		return (u[0] * v[0] + u[1] * v[1]);
	}

	/**
	 * Returns the length of a vector.
	 * 
	 * @param u
	 *            Vector u coordinate.
	 * @param v
	 *            Vector v coordinate.
	 * @return Length of a vector.
	 */
	public static double vectorLength(double u, double v)
	{
		return Math.sqrt(dotProduct(u, u, v, v));
	}

	public static double vectorLength(double[] v)
	{
		return Math.sqrt(dotProduct(v[0], v[0], v[1], v[1]));
	}

	/**
	 * Returns the intersection point of two vectors.
	 * 
	 * @param a0
	 *            Vector a's start coordinates.
	 * @param a1
	 *            Vector a's end coordinates.
	 * @param b0
	 *            Vector b's start coordinates.
	 * @param b1
	 *            Vector b's end coordinates.
	 * @return Intersection of vector a and b.
	 */
	public static int[] vectorIntersection(int[] a0, int[] a1, int[] b0, int[] b1)
	{
		double[] v0 = new double[] { a1[0] - a0[0], a1[1] - a0[1] };
		double[] v1 = new double[] { b1[0] - b0[0], b1[1] - b0[1] };
		// starting point vector.
		double[] v2 = new double[] { b0[0] - a0[0], b0[1] - a0[1] };

		double dp0 = -v2[1] * v1[0] + v2[0] * v1[1];
		double dp1 = -v0[1] * v1[0] + v0[0] * v1[1];
		double ratio = dp0 / dp1;
		double cx = a0[0] + v0[0] * ratio;
		double cy = a0[1] + v0[1] * ratio;

		return new int[] { Mext.round(cx), Mext.round(cy) };
	}

	public static int round(double a)
	{
		return (int) (a + 0.5);
	}

	public static int clamp(int i, int min, int max)
	{
		i = min < i ? min : i;
		i = i > max ? max : i;
		return i;
	}

	public static double angle(double[] a, double[] b)
	{
		return Math.toDegrees(MoreMath.acos(dotProduct(a, b) / (vectorLength(a) * vectorLength(b))));
	}
}
