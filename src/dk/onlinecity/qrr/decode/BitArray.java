/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.decode;

class BitArray
{
	private byte[] bytes;

	private int bitOffset;
	private int byteOffset;

	BitArray(byte[] bytes)
	{
		this.bytes = bytes;
		bitOffset = 0;
		byteOffset = 0;
	}

	final int getNext(int bits)
	{
		int res = 0;

		if (bitOffset > 0) {
			int bitsLeft = 8 - bitOffset;
			int toRead = bits < bitsLeft ? bits : bitsLeft;
			int bitsToNotRead = bitsLeft - toRead;
			int bitMask = (0xff >> (8 - toRead)) << bitsToNotRead;
			res = (bytes[byteOffset] & bitMask) >> bitsToNotRead;
			bits -= toRead;
			bitOffset += toRead;
			if (bitOffset == 8) {
				bitOffset = 0;
				byteOffset++;
			}
		}

		if (bits > 0) {
			while (bits >= 8) {
				res = (res << 8) | (bytes[byteOffset] & 0xff);
				byteOffset++;
				bits -= 8;
			}

			if (bits > 0) {
				int bitsToNotRead = 8 - bits;
				int bitmask = (0xff >> bitsToNotRead) << bitsToNotRead;
				res = (res << bits) | ((bytes[byteOffset] & bitmask) >> bitsToNotRead);
				bitOffset += bits;
			}
		}

		return res;
	}

	final int bitsLeft()
	{
		return 8 * (bytes.length - byteOffset) - bitOffset;
	}
}
