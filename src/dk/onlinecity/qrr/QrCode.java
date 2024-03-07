/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr;

/**
 * This class represents a QR-Code.
 * 
 * @author Thomas Duerlund (td@onlinecity.dk)
 * 
 */
public class QrCode
{
	private final boolean[][] matrix;
	private final int size;

	/**
	 * @param matrix
	 *            Boolean matrix.
	 */
	public QrCode(boolean[][] matrix)
	{
		this.matrix = matrix;
		this.size = matrix.length;
	}

	/**
	 * @param x
	 *            X coordinate.
	 * @param y
	 *            Y coordinate.
	 * @return Module at (x, y).
	 */
	public final boolean get(int x, int y)
	{
		return matrix[x][y];
	}

	/**
	 * @return QR-Code size.
	 */
	public final int size()
	{
		return size;
	}

	public void print()
	{
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				System.out.print(matrix[x][y] ? "X" : ".");
			}
			System.out.println();
		}
	}

	public int[] getPixels(int multiplier)
	{
		int size = this.size * multiplier;
		int[] pixels = new int[size * size];

		for (int y = 0, qy = 0; y < size; y += multiplier, qy++) {
			for (int x = 0, qx = 0; x < size; x += multiplier, qx++) {
				int color = matrix[qx][qy] ? 0x000000 : 0xffffff;
				for (int yy = y; yy < y + multiplier; yy++) {
					for (int xx = x; xx < x + multiplier; xx++) {
						pixels[xx + yy * size] = color;
					}
				}
			}
		}
		return pixels;
	}
}
