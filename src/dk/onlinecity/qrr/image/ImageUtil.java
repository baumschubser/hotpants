/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.image;

import java.util.Enumeration;
import java.util.Hashtable;

import dk.onlinecity.qrr.perspective.CoordinateTranslator;
import dk.onlinecity.qrr.perspective.PerspectiveTransformer;

public class ImageUtil
{
	private MonochromeImage image;
	private int[] thresholdValues;

	public ImageUtil(int[] rgb, int w, int h, int gw, int gh)
	{
		int[] histogram = new int[256];
		int[][] gridHistograms = new int[gw * gh][256];
		generateHistogramsAndGreyscale(rgb, histogram, gridHistograms, w, h, gw, gh);
		threshold(histogram, gridHistograms);
		image = new MonochromeImage(rgb, w, h);
	}

	private void generateHistogramsAndGreyscale(int[] rgb, int[] histogram, int[][] gridHistograms, int w, int h, int gw, int gh)
	{
		int l = rgb.length;
		int x;
		int y;
		int gsx = w / gw;
		int gsy = h / gh;
		int gridHistogramIndex;

		for (int i = 0; i < l; i++) {
			rgb[i] = greyscale(rgb[i]);
			histogram[rgb[i]]++;
			x = (i % w);
			y = ((i - x) / w);
			gridHistogramIndex = (x / gsx) + (y / gsy) * gw;
			if (gridHistogramIndex < gridHistograms.length) gridHistograms[gridHistogramIndex][rgb[i]]++;
		}
	}

	private void threshold(int[] histogram, int[][] gridHistograms)
	{
		int l = gridHistograms.length;
		int threshold = Threshold.blackPointEstimate(histogram);
		int tIndex = 0;
		Hashtable uniqueThresholdValues = new Hashtable(l + 1);
		uniqueThresholdValues.put(threshold + "", (tIndex++) + "");

		for (int i = 0; i < l; i++) {
			threshold = Threshold.blackPointEstimate(gridHistograms[i]);
			// ensure we don't have any duplicate threshold values.
			if (!uniqueThresholdValues.containsKey(threshold + "")) uniqueThresholdValues.put(threshold + "", (tIndex++) + "");
		}
		thresholdValues = new int[uniqueThresholdValues.size()];
		String k, v;
		// Enumerate the hash table to get unique threshold values.
		for (Enumeration e = uniqueThresholdValues.keys(); e.hasMoreElements();) {
			k = (String) e.nextElement();
			v = (String) uniqueThresholdValues.get(k);
			thresholdValues[Integer.parseInt(v)] = Integer.parseInt(k);
		}
	}

	public MonochromeImage getMonochromeImage()
	{
		return image;
	}

	public int[] getThresholdValues()
	{
		return thresholdValues;
	}

	/**
	 * @param rgb
	 * @param w
	 * @param h
	 * @param tw
	 * @param th
	 * @return
	 */
	public static Thumbnail getThumbnail(int[] rgb, int w, int h, int tw, int th)
	{
		CoordinateTranslator thumbnailToImage = PerspectiveTransformer.getCoordinateTranslator(0, 0, tw, 0, 0, th, tw, th, 0, 0, w, 0, 0, h, w, h);
		CoordinateTranslator imageToThumbnail = PerspectiveTransformer.getCoordinateTranslator(0, 0, w, 0, 0, h, w, h, 0, 0, tw, 0, 0, th, tw, th);

		int[] thumbnail = new int[tw * th];

		int x, y;

		for (int ty = 0; ty < th; ty++) {
			for (int tx = 0; tx < tw; tx++) {
				x = (int) thumbnailToImage.getU(tx, ty);
				y = (int) thumbnailToImage.getV(tx, ty);
				thumbnail[tx + ty * tw] = rgb[x + y * w];
			}
		}
		return new Thumbnail(thumbnail, imageToThumbnail, tw, th);
	}

	public static final int greyscale(int p)
	{
		return (((p >> 16) & 0xff) + ((p >> 8) & 0xff) + (p & 0xff)) / 3;
	}
}
