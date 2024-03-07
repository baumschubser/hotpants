/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.image;

public class MonoImage
{
	private int[] pixels;
	private int width;
	private int height;
	private int threshold;

	MonoImage(int[] pixels, int width, int height, int threshold)
	{
		this.pixels = pixels;
		this.width = width;
		this.height = height;
		this.threshold = threshold;
	}

	public boolean get(int x, int y) throws ArrayIndexOutOfBoundsException
	{
		if (0 <= x && x < width && 0 <= y && y < height) return pixels[x + y * width] <= threshold;

		throw new ArrayIndexOutOfBoundsException();
	}

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return height;
	}

	public void setThreshold(int threshold)
	{
		this.threshold = threshold;
	}

	public int getThreshold()
	{
		return threshold;
	}
}
