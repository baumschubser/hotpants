/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.detect;

import java.util.Vector;


import dk.onlinecity.qrr.image.MonochromeImage;
import dk.onlinecity.qrr.util.Mext;
import dk.onlinecity.qrr.util.Utils;

public class LastCornerGuess
{
	public static void mirrorVectors(int[] p0, int[] p1, int[] p2)
	{
		System.out.println();
		System.out.println("p0:" + p0[0] + ", " + p0[1]);
		System.out.println("p1:" + p1[0] + ", " + p1[1]);
		System.out.println("p2:" + p2[0] + ", " + p2[1]);

		int[] v01 = vec(p0, p1);
		int[] v02 = vec(p0, p2);

		System.out.println("vector p0->p1 a: " + v01[0] + ", " + v01[1]);
		System.out.println("vector p0->p2 b: " + v02[0] + ", " + v02[1]);

		// mirror v02 and "attach it" to p1

		double[] p3a = new double[] { v02[0] + p1[0], -v02[1] + p1[1] };
		double[] p3b = new double[] { v02[0] + p1[0], v02[1] + p1[1] };
		System.out.println("p3:" + p3a[0] + ", " + p3a[1]);
		System.out.println("p3:" + p3b[0] + ", " + p3b[1]);
		System.out.println();
	}

	public static int[] p3(MonochromeImage image, int[] p0, int[] p1, int[] p2)
	{
		int[] v01 = vec(p0, p1);
		int[] v10 = vec(p1, p0);

		int[] v02 = vec(p0, p2);
		int[] v20 = vec(p2, p0);

		int[] v12 = vec(p1, p2);
		int[] v21 = vec(p2, p1);

		double mult0 = 0.50;

		// System.out.println("v01:" + vecStr(v01));
		// System.out.println("v10:" + vecStr(v10));
		//
		// System.out.println("v02:" + vecStr(v02));
		// System.out.println("v20:" + vecStr(v20));
		//
		// System.out.println("v12:" + vecStr(v12));
		// System.out.println("v21:" + vecStr(v21));

		v01 = vecMult(v01, mult0);
		v10 = vecMult(v10, mult0);
		v02 = vecMult(v02, mult0);
		v20 = vecMult(v20, mult0);
		v12 = vecMult(v12, mult0);
		v21 = vecMult(v21, mult0);

		// System.out.println("---");
		//
		// System.out.println("v01:" + vecStr(v01));
		// System.out.println("v10:" + vecStr(v10));
		//
		// System.out.println("v02:" + vecStr(v02));
		// System.out.println("v20:" + vecStr(v20));
		//
		// System.out.println("v12:" + vecStr(v12));
		// System.out.println("v21:" + vecStr(v21));
		//
		// System.out.println("---");

		int[] urr0 = getEdge(image, p1, vecAdd(p1, v21));
		int[] urr1 = getEdge(image, p1, vecAdd(p1, v01));

		int[] url0 = getEdge(image, p1, vecAdd(p1, v10));
		int[] url1 = getEdge(image, p1, vecAdd(p1, v12));

		// System.out.println("url0:" + vecStr(urr0));
		// System.out.println("url1:" + vecStr(urr1));
		// System.out.println("urr0:" + vecStr(url0));
		// System.out.println("urr1:" + vecStr(url1));
		//
		//
		// System.out.println("--.");

		int[] lll0 = getEdge(image, p2, vecAdd(p2, v12));
		int[] lll1 = getEdge(image, p2, vecAdd(p2, v02));

		int[] llu0 = getEdge(image, p2, vecAdd(p2, v20));
		int[] llu1 = getEdge(image, p2, vecAdd(p2, v21));

		// System.out.println("lll0:" + vecStr(lll0));
		// System.out.println("lll1:" + vecStr(lll1));
		// System.out.println("llu0:" + vecStr(llu0));
		// System.out.println("llu1:" + vecStr(llu1));
		//
		// System.out.println("---");

		int[] lll = vec(lll0, lll1);
		int[] llu = vec(llu0, llu1);
		int[] urr = vec(urr0, urr1);
		int[] url = vec(url0, url1);

		lll = vecMult(lll, 8);
		llu = vecMult(llu, 8);

		urr = vecMult(urr, 8);
		url = vecMult(url, 8);

		// System.out.println("lll0:" + vecStr(lll0));
		// System.out.println("lll:" + vecStr(vecAdd(lll, lll0)));
		// System.out.println("llu0:" + vecStr(llu0));
		// System.out.println("llu:" + vecStr(vecAdd(llu, llu0)));
		// System.out.println("url0:" + vecStr(urr0));
		// System.out.println("url:" + vecStr(vecAdd(urr, urr0)));
		// System.out.println("urr0:" + vecStr(url0));
		// System.out.println("urr:" + vecStr(vecAdd(url, url0)));
		// System.out.println("---");

		int[] ul = Mext.vectorIntersection(llu0, vecAdd(llu, llu0), url0, vecAdd(url, url0));
		int[] ur = Mext.vectorIntersection(llu0, vecAdd(llu, llu0), urr0, vecAdd(urr, urr0));

		int[] ll = Mext.vectorIntersection(lll0, vecAdd(lll, lll0), url0, vecAdd(url, url0));
		int[] lr = Mext.vectorIntersection(lll0, vecAdd(lll, lll0), urr0, vecAdd(urr, urr0));

		System.out.println("UL:" + vecStr(ul));
		System.out.println("UR:" + vecStr(ur));
		System.out.println("LL:" + vecStr(ll));
		System.out.println("LR:" + vecStr(lr));

		System.out.println("---");

		int[] cross = Mext.vectorIntersection(ul, lr, ur, ll);

		System.out.println("p3 intersection guess:" + vecStr(cross));
		System.out.println("---");
		return cross;
		// return new int[] { 424, 174 };
	}

	private static int[] getEdge(MonochromeImage image, int[] v, int[] u)
	{
		// we start at the center of a finder pattern a scans out to the edge

		Vector line = Utils.bresenhamLine(v[0], v[1], u[0], u[1], 0, 0, image.getWidth(), image.getHeight());

		int[] p = (int[]) line.elementAt(0);
		int state = 0;
		int index = 0;

		int from = 0;
		int to = line.size();
		int step = 1;

		// iterate backwards
		if (p[0] != v[0] || p[1] != v[1]) {
			from = line.size() - 1;
			to = 0;
			step = -1;
		}

		for (int i = from; (step > 0 ? i < to : i >= to); i += step) {
			p = (int[]) line.elementAt(i);
			// System.out.print("(" + vecStr(p) + ") ");

			if (image.get(p)) { /* black pixel */
				if (state == 1) {
					state++;
				}
			} else { /* white pixel */
				if (state == 0) {
					state++;
				}
				if (state == 2) {
					// stop
					index = i;
					// System.out.println("stop at index:" + i);
					// System.out.println(vecStr(p));
					break;
				}
			}
		}

		// (261, 134)
		// System.out.println();

		p = (int[]) line.elementAt(index);

		return p;
	}

	private static int[] vec(int[] a, int[] b)
	{
		return new int[] { b[0] - a[0], b[1] - a[1] };
	}

	private static int[] vecMult(int[] a, double mult)
	{
		return new int[] { (int) ((double) a[0] * mult), (int) ((double) a[1] * mult) };
	}

	private static int[] vecAdd(int[] a, int[] b)
	{
		return new int[] { a[0] + b[0], a[1] + b[1] };
	}

	private static void vecPrint(int[] a)
	{
		System.out.println(a[0] + ", " + a[1]);
	}

	private static String vecStr(int[] a)
	{
		return a[0] + ", " + a[1];
	}
}
