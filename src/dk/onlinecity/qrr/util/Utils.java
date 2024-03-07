/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.util;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class Utils
{
	/**
	 * Implementation of Bresenham's line algorithm.
	 * 
	 * @param x0
	 *            Start point x coordinate.
	 * @param y0
	 *            Start point y coordinate.
	 * @param x1
	 *            End point x coordinate.
	 * @param y1
	 *            End point y coordinate.
	 * @return Vector of points that forms the line between (x0, y0) and (x1,
	 *         y1).
	 */
	public static Vector bresenhamLine(int x0, int y0, int x1, int y1, int xMin, int yMin, int xMax, int yMax)
	{
		Vector coordinates = new Vector();
		boolean steep = Math.abs(y1 - y0) > Math.abs(x1 - x0);
		int tmp = 0;

		if (steep) {
			// tmp = 0;
			// swap x0 and y0
			tmp = x0;
			x0 = y0;
			y0 = tmp;
			// swap x1 and x1
			tmp = x1;
			x1 = y1;
			y1 = tmp;
		}

		if (x0 > x1) {
			// tmp = 0;
			// swap x0 and x1
			tmp = x0;
			x0 = x1;
			x1 = tmp;
			// swap y0 and y1
			tmp = y0;
			y0 = y1;
			y1 = tmp;
		}

		int dX = x1 - x0;
		int dY = Math.abs(y1 - y0);
		int err = dX >> 1;
		int y = y0;
		int yStep = y0 < y1 ? 1 : -1;
		int[] coordinate;

		for (int x = x0; x < x1; x++) {
			coordinate = new int[2];
			if (steep) {
				coordinate = new int[] { y, x };
			} else {
				coordinate = new int[] { x, y };
			}

			if (xMin <= coordinate[0] && coordinate[0] < xMax && yMin <= coordinate[1] && coordinate[1] < yMax) coordinates.addElement(coordinate);

			err = err - dY;
			if (err < 0) {
				y += yStep;
				err += dX;
			}
		}
		return coordinates;
	}

	/**
	 * Removes duplicate values in an integer array.
	 * 
	 * @param array
	 *            Array to remove duplicates in.
	 * @return Array with no duplicate values.
	 */
	public final static int[] removeDuplicates(int[] array)
	{
		int l = array.length;
		Hashtable entries = new Hashtable(l);
		int newIndex = 0;

		for (int i = 0; i < l; i++)
			if (!entries.containsKey(array[i] + "")) entries.put(array[i] + "", (newIndex++) + "");

		int[] newArray = new int[entries.size()];

		String k, v;
		for (Enumeration e = entries.keys(); e.hasMoreElements();) {
			k = (String) e.nextElement();
			v = (String) entries.get(k);
			newArray[Integer.parseInt(v)] = Integer.parseInt(k);
		}

		return newArray;
	}
}
