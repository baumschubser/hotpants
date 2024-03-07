/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.decode;

/**
 * @author Thomas Duerlund (td@onlinecity.dk)
 */
final class BitMask
{
	private BitMask()
	{

	}

	final static boolean unmask(int x, int y, int mask)
	{
		// masks described on p. 58
		// NOTE: mask 1, 2 & 4: i & j are switched.

		int res = 0;
		switch (mask) {
		case 0: // 000
			res = (x + y) % 2;
			return res == 0;
		case 1: // 001
			// res = i % 2;
			res = y % 2;
			return res == 0;
		case 2: // 010
			// res = j % 3;
			res = x % 3;
			return res == 0;
		case 3: // 011
			res = (x + y) % 3;
			return res == 0;
		case 4: // 100
			int t1 = (int) Math.floor((double) y / 2);
			int t2 = (int) Math.floor((double) x / 3);
			res = (t1 + t2) % 2;
			return res == 0;
		case 5: // 101
			res = ((x * y) % 2) + ((x * y) % 3);
			return res == 0;
		case 6: // 110
			res = (((x * y) % 2) + ((x * y) % 3)) % 2;
			return res == 0;
		case 7: // 111
			res = (((x + y) % 2) + ((x * y) % 3)) % 2;
			return res == 0;
		}

		// we should never get here.
		return false;
	}
}
