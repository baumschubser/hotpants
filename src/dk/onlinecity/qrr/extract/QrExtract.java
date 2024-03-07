/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.extract;

import dk.onlinecity.qrr.QrCandidate;
import dk.onlinecity.qrr.QrCode;
import dk.onlinecity.qrr.core.exceptions.DecodeException;
import dk.onlinecity.qrr.detect.CoordinateValidation;
import dk.onlinecity.qrr.image.MonochromeImage;
import dk.onlinecity.qrr.perspective.CoordinateTranslator;

public class QrExtract
{
	private QrExtract()
	{

	}

	/**
	 * @param image
	 * @param qrCandidate
	 * @return
	 */
	public static QrCode extract(MonochromeImage image, QrCandidate qrCandidate) throws DecodeException
	{
		if (qrCandidate == null) throw new DecodeException("Candidate is null");

		int modules = qrCandidate.getTotalEstModules();
		int mult = qrCandidate.getMultiplier();
		int size = modules * mult;

		boolean[][] sampleGrid = extractSampleGrid(image, qrCandidate.getCoordinateTranslator(), size);
		boolean[][] grid = new boolean[modules][modules];

		CoordinateValidation cv = new CoordinateValidation(image.getWidth(), image.getHeight(), qrCandidate);
		if (!cv.validate()) return null;

		for (int gy = 0, y = 0; gy < size && y < modules; gy += mult, y++) {
			for (int gx = 0, x = 0; gx < size && x < modules; gx += mult, x++) {
				grid[x][y] = isBlackModule(sampleGrid, gx, gy, gx + mult, gy + mult);
			}
		}
		return new QrCode(grid);
	}

	/**
	 * @param image
	 * @param ct
	 * @param size
	 * @return
	 */
	private static boolean[][] extractSampleGrid(MonochromeImage image, CoordinateTranslator ct, int size)
	{
		boolean[][] sampleGrid = new boolean[size][size];

		int x, y, w = image.getWidth(), h = image.getHeight();

		for (int v = 0; v < size; v++) {
			for (int u = 0; u < size; u++) {
				x = (int) ct.getU(u, v);
				y = (int) ct.getV(u, v);
				if (0 <= x && x < w && 0 <= y && y < h) sampleGrid[u][v] = image.get(x, y);
			}
		}

		return sampleGrid;

	}

	/**
	 * A module is considered to be a block module if it consists of 60% black
	 * pixels.
	 * 
	 * @param qr
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 * @return
	 */
	private static boolean isBlackModule(boolean[][] qr, int x0, int y0, int x1, int y1)
	{
		int cx = x0 + ((x1 - x0) >> 1);
		int cy = y0 + ((y1 - y0) >> 1);

		double black = 0;
		double total = 0;
		for (int y = cy - 1; y < cy + 2; y++) {
			for (int x = cx - 1; x < cx + 2; x++) {
				if (qr[x][y]) black++;
				total++;
			}
		}

		return black >= .60 * total;
	}
}
