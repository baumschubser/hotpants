/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.decode;

import dk.onlinecity.qrr.QrCode;
import dk.onlinecity.qrr.core.exceptions.DecodeException;

/**
 * This class parses a QR-Codes raw data.
 * 
 * @author Thomas Duerlund (td@onlinecity.dk)
 */
class QrCodeParser
{
	/** Total number of code words to read. */
	private static final int[] TOTAL_CODEWORDS = { 0, 26, 44, 70, 100, 134, 172, 196, 242, 292, 346, 404, 466, 532, 581, 655, 733, 815, 901, 991, 1085, 1156, 1258, 1364, 1474,
			1588, 1706, 1828, 1921, 2051, 2185, 2323, 2465, 2611, 2761, 2876, 3034, 3196, 3362, 3532, 3706 };

	/** Version information error correction lookup table. Version = index + 7; */
	private static final int[] VERSION_EC = { 0x07c94, 0x085bc, 0x09a99, 0x0a4d3, 0x0bbf6, 0x0c762, 0x0d847, 0x0e60d, 0x0f928, 0x10b78, 0x1145d, 0x12a17, 0x13532, 0x149a6,
			0x15683, 0x168c9, 0x177ec, 0x18ec4, 0x191e1, 0x1afab, 0x1b08e, 0x1cc1a, 0x1d33f, 0x1ed75, 0x1f250, 0x209d5, 0x216f0, 0x228ba, 0x2379f, 0x24b0b, 0x2542e, 0x26a64,
			0x27541, 0x28c69 };

	/** Format information error correction lookup table. */
	private static final int[] FORMAT_EC = { 0x5412, 0x5125, 0x5e7c, 0x5b4b, 0x45f9, 0x40ce, 0x4f97, 0x4aa0, 0x77c4, 0x72f3, 0x7daa, 0x789d, 0x662f, 0x6318, 0x6c41, 0x6976,
			0x1689, 0x13be, 0x1ce7, 0x19d0, 0x0762, 0x0255, 0x0d0c, 0x083b, 0x355f, 0x3068, 0x3f31, 0x3a06, 0x24b4, 0x2183, 0x2eda, 0x2bed };

	private QrCodeParser()
	{

	}

	/**
	 * Reads the data stored in the QR-Code.
	 * 
	 * @param qrCode
	 *            QR-Code to read from.
	 * @param mask
	 *            Bitmask to subtract from the QR-Code.
	 * @param version
	 *            QR-Code version.
	 * @return Data stored in the QR-Code.
	 */
	final static byte[] parseData(QrCode qrCode, int version) throws DecodeException
	{
		// qrCode.print();
		int formatBits = getFormatBits(qrCode);
		int ecLevel = formatBits >> 3;
		int mask = formatBits & 7;

		byte[] data = readRawData(qrCode, mask, version);
		ErrorCorrection errCorrection = new ErrorCorrection();
		data = errCorrection.errorCorrect(data, version, ecLevel);
		return data;
	}

	/**
	 * Reads the raw data stored in the QR-Code.
	 * 
	 * @param qrCode
	 *            QR-Code to read from.
	 * @param mask
	 *            Bitmask to subtract from the QR-Code.
	 * @param version
	 *            QR-Code version.
	 * @return Raw data stored in the QR-Code.
	 */
	private final static byte[] readRawData(QrCode qrCode, int mask, int version)
	{
		FunctionPatterns restrictedAreas = new FunctionPatterns(getVersion(qrCode), qrCode.size());
		int size = qrCode.size();
		int x = size - 1;
		boolean readUp = true;
		int bits = 0;
		int currentBit = 0;

		byte[] bytes = new byte[TOTAL_CODEWORDS[version]];
		int index = 0;

		while (x > -1) {
			// skip timer pattern
			if (x == 6) x--;

			if (readUp) {
				// read up
				for (int y = size - 1; y > -1; y--) {
					for (int i = 0; i < 2; i++) {
						if (!restrictedAreas.isFunctionPattern(x - i, y)) {
							currentBit <<= 1;
							if (qrCode.get(x - i, y) ^ BitMask.unmask(x - i, y, mask)) currentBit |= 1;
							if (++bits == 8) {
								// FIXME: ArrayIndexOutOfBoundsException
								bytes[index++] = (byte) currentBit;
								bits = 0;
								currentBit = 0;
							}
						}
					}
				}
			} else {
				// read down
				for (int y = 0; y < size; y++) {
					for (int i = 0; i < 2; i++) {
						if (!restrictedAreas.isFunctionPattern(x - i, y)) {
							currentBit <<= 1;
							if (qrCode.get(x - i, y) ^ BitMask.unmask(x - i, y, mask)) currentBit |= 1;
							if (++bits == 8) {
								bytes[index++] = (byte) currentBit;
								bits = 0;
								currentBit = 0;
							}
						}
					}
				}
			}
			x -= 2;
			readUp = !readUp;
		}
		return bytes;
	}

	/**
	 * Returns the QR-Code version.
	 * 
	 * @param qrCode
	 *            QR-Code to get version of.
	 * @return QR-Code version.
	 * @throws DecodeException
	 *             Thrown if was not possible to read the version information.
	 */
	final static int getVersion(QrCode qrCode) throws DecodeException
	{
		if (qrCode == null) throw new DecodeException("QR-Code is null");

		// Attempt to determine the version directly by looking at the QR-Code
		// size.
		int version = ((qrCode.size() - 21) >> 2) + 1;

		if (version < 1) throw new DecodeException("Version < 1");

		// On version 7 and above we need to read the version information areas.
		if (version < 7) return version;

		int s = qrCode.size();

		int i0 = s - 11;
		int i1 = s - 9;
		int j0 = 0;
		int j1 = 5;

		int ur = 0;
		int ll = 0;

		for (int j = j1; j > j0 - 1; j--) {
			for (int i = i1; i > i0 - 1; i--) {
				// read upper right area
				ur <<= 1;
				if (qrCode.get(i, j)) ur |= 1;

				// read lower left area
				ll <<= 1;
				if (qrCode.get(j, i)) ll |= 1;
			}
		}

		// error correct.
		if (ur == ll)
			version = lookup(ur, VERSION_EC).getValue() + 7;
		else
			version = HammingDistance.getBestCandidate(new Candidate[] { lookup(ur, VERSION_EC), lookup(ll, VERSION_EC) }) + 7;

		if (version > 40) throw new DecodeException("Version > 40");
		return version;
	}

	/**
	 * Returns the QR-Code format bits.
	 * 
	 * @param qrCode
	 *            QR-Code to get format bits from.
	 * @return QR-Code format bits.
	 */
	final static int getFormatBits(QrCode qrCode)
	{
		int s = qrCode.size();
		int s1 = 0;
		int s2 = 0;
		int t1 = 0;
		int t2 = 0;

		// read first parts
		for (int i = 8; i > -1; i--) {
			if (i != 6) { // skip timing pattern
				s1 <<= 1;
				if (qrCode.get(8, i)) s1 |= 1;
			}

			if (i != 0) {
				s2 <<= 1;
				if (qrCode.get(s - i, 8)) s2 |= 1;
			}
		}

		// read second parts
		for (int i = 0; i < 8; i++) {
			if (0 < i) {
				t1 <<= 1;
				if (qrCode.get(8, s - i)) t1 |= 1;
			}
			if (i != 6) { // skip timing pattern
				t2 <<= 1;
				if (qrCode.get(i, 8)) t2 |= 1;
			}
		}

		// shift the second parts so we can add the first and second parts.
		t1 <<= 8;
		t2 <<= 8;

		// error correct.
		if (s1 == s2 && t1 == t2) return lookup(s1 + t1, FORMAT_EC).getValue();

		if (s1 == s2 && t1 != t2) return HammingDistance.getBestCandidate(new Candidate[] { lookup(s1 + t1, FORMAT_EC), lookup(s1 + t2, FORMAT_EC) });

		if (s1 != s2 && t1 == t2) return HammingDistance.getBestCandidate(new Candidate[] { lookup(s1 + t1, FORMAT_EC), lookup(s2 + t1, FORMAT_EC) });

		return HammingDistance.getBestCandidate(new Candidate[] { lookup(s1 + t1, FORMAT_EC), lookup(s1 + t2, FORMAT_EC), lookup(s2 + t1, FORMAT_EC), lookup(s2 + t2, FORMAT_EC) });
	}

	private final static Candidate lookup(int a, int[] table)
	{
		int index = -1;
		int minHamDist = Integer.MAX_VALUE;
		int hamDist = 0;
		int l = table.length;

		for (int i = 0; i < l; i++) {
			// Attempt direct lookup.
			hamDist = HammingDistance.getHammingDistance(table[i], a);
			if (hamDist == 0) {
				return new Candidate(i, hamDist);
			}
			// Save the table entry with the lowest hamming distance.
			if (hamDist < minHamDist) {
				minHamDist = hamDist;
				index = i;
			}
		}

		// All direct lookup attempts failed, return the entry with the lowest
		// hamming distance.
		return new Candidate(index, hamDist);
	}
}
