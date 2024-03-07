/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.detect;

import dk.onlinecity.qrr.QrCandidate;
import dk.onlinecity.qrr.util.Mext;

public class CoordinateValidation
{
	private int w;
	private int h;

	private double[] ul;
	private double[] ur;
	private double[] ll;
	private double[] lr;

	private double[] ulur;
	private double[] urll;
	private double[] lllr;
	private double[] lrul;

	public CoordinateValidation(int w, int h, QrCandidate qrCandidate)
	{
		this.w = w;
		this.h = h;

		ul = convertToDouble(qrCandidate.getUl());
		ur = convertToDouble(qrCandidate.getUr());
		ll = convertToDouble(qrCandidate.getLl());
		lr = convertToDouble(qrCandidate.getLr());
		ulur = vector(ul, ur);
		urll = vector(ur, ll);
		lllr = vector(ll, lr);
		lrul = vector(lr, ul);

	}

	private double[] convertToDouble(int[] ia)
	{
		return new double[] { ia[0], ia[1] };
	}

	public boolean validate()
	{
		return borderCheck() && checkAngles() && checkLenght();
	}

	/**
	 * Compares the longest side with the shortest side.
	 * 
	 * @return
	 */
	private boolean checkLenght()
	{
		double maxLen = Double.MIN_VALUE;
		double minLen = Double.MAX_VALUE;
		double[] lengths = new double[] { Mext.vectorLength(ulur), Mext.vectorLength(urll), Mext.vectorLength(lllr), Mext.vectorLength(lrul) };
		double l;
		for (int i = 0; i < lengths.length; i++) {
			if ((l = lengths[i]) == 0) {
				System.out.println("failed lenght check; lenght was 0");
				return false;
			}
			maxLen = maxLen < l ? l : maxLen;
			minLen = minLen > l ? l : minLen;
		}

		if ((maxLen - minLen) > 1.5 * minLen) {
			System.out.println("failed lenght check");
			return false;
		}
		return true;
	}

	private boolean checkAngles()
	{
		double a = Mext.angle(vector(ul, ur), vector(ul, ll));
		double b = Mext.angle(vector(ur, lr), vector(ur, ul));
		double c = Mext.angle(vector(ll, ul), vector(ll, lr));
		double d = Mext.angle(vector(lr, ur), vector(lr, ll));

		if (angleRange(a) && angleRange(b) && angleRange(c) && angleRange(d)) {
			return true;
		}
		System.out.println("failed angle test");
		return false;
	}

	private boolean angleRange(double a)
	{
		return 60 <= a && a <= 120;
	}

	private double[] vector(double[] a, double[] b)
	{
		return new double[] { b[0] - a[0], b[1] - a[1] };
	}

	private boolean borderCheck()
	{
		return borderCheck(ul) && borderCheck(ur) && borderCheck(lr) && borderCheck(ll);
	}

	private boolean borderCheck(double[] a)
	{
		return 0 <= a[0] && a[0] < w && 0 <= a[1] && a[1] < h;
	}
}
