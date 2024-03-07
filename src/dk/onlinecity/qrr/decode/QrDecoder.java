/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.decode;

import dk.onlinecity.qrr.QrCode;
import dk.onlinecity.qrr.core.exceptions.DecodeException;
import dk.onlinecity.qrr.util.Mext;

/**
 * This class can decode a QR-Code.
 * 
 * @author Thomas Duerlund (td@onlinecity.dk)
 */
public class QrDecoder
{
	private static final String[] ALPHA_CHARS = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P",
			"Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", " ", "$", "%", "*", "+", "-", ".", "/", ":" };

	private QrDecoder()
	{
	}

	/**
	 * @param qrCode
	 * @return
	 * @throws DecodeException
	 */
	public final static String decode(QrCode qrCode) throws DecodeException
	{
		int version = QrCodeParser.getVersion(qrCode);
		System.out.println("version:" + version);
		return parseData(new BitArray(QrCodeParser.parseData(qrCode, version)), version);
	}

	/**
	 * @param bits
	 * @param version
	 * @return
	 * @throws DecodeException
	 */
	private final static String parseData(BitArray bits, int version) throws DecodeException
	{
		String res = "";
		boolean stop = false;

		while (bits.bitsLeft() > 4 && !stop) {
			switch (bits.getNext(4)) {
			case Mode.TERMINATOR:
				stop = true;
				// quit;
				break;
			case Mode.NUMERIC:
				// decode numeric
				res += decodeNumeric(bits, Mode.getWordsToRead(bits, Mode.NUMERIC, version));
				break;
			case Mode.ALPHA:
				// decode alpha
				res += decodeAlpha(bits, Mode.getWordsToRead(bits, Mode.ALPHA, version));
				break;
			case Mode.STRUCTURED_APPEND:
				throw new DecodeException("Mode is not supported");
			case Mode.BYTE:
				// decode byte
				res += decodeByte(bits, Mode.getWordsToRead(bits, Mode.BYTE, version));
				break;
			case Mode.FNC1A:
				throw new DecodeException("Mode is not supported");
			case Mode.KANJI:
				throw new DecodeException("Mode is not supported");
			case Mode.FNC1B:
				throw new DecodeException("Mode is not supported");
			}
		}

		return res;
	}

	/**
	 * @param bits
	 * @param charCount
	 * @return
	 */
	private final static String decodeAlpha(BitArray bits, int charCount)
	{
		int wordLen = 11; // word
		int charsPerWord = 2;
		int remainder = 6 * (charCount % charsPerWord);
		int bitsToRead = (wordLen * Mext.div(charCount, charsPerWord)) + remainder;
		int fullWords = Mext.div(bitsToRead, wordLen);

		int j;
		String res = "";
		String s1;
		String s2;
		for (int i = 0; i < fullWords; i++) {
			j = bits.getNext(wordLen);
			s1 = ALPHA_CHARS[(int) (j / 45f)];
			s2 = ALPHA_CHARS[j % 45];
			res += s1 + s2;
		}

		if (remainder == 6) {
			j = bits.getNext(remainder);
			res += ALPHA_CHARS[j];
		}

		return res;
	}

	/**
	 * @param bits
	 * @param charCount
	 * @return
	 */
	private final static String decodeByte(BitArray bits, int charCount)
	{
		int j;
		byte[] bytes = new byte[charCount];
		for (int i = 0; i < charCount; i++) {
			j = bits.getNext(8);
			bytes[i] = (byte) j;
		}
		return new String(bytes);
	}

	/**
	 * @param bits
	 * @param charCount
	 * @return
	 */
	private final static String decodeNumeric(BitArray bits, int charCount)
	{
		int wordLen = 10;
		int charsPerWords = 3;
		int remainder = 0;
		switch (charCount % charsPerWords) {
		case 1:
			remainder = 4;
			break;
		case 2:
			remainder = 7;
			break;
		}

		int bitsToRead = wordLen * Mext.div(charCount, charsPerWords) + remainder;
		int fullWords = Mext.div(bitsToRead, wordLen);
		String result = "";
		int j;
		for (int i = 0; i < fullWords; i++) {
			j = bits.getNext(wordLen);
			result += fixedLenghtNumeric(j);
		}

		if (remainder == 4) result += bits.getNext(remainder);

		if (remainder == 7) {
			int i = bits.getNext(wordLen);
			result += (i < 10 ? "0" : "") + i;
		}

		return result;
	}

	/**
	 * @param i
	 * @return
	 */
	private final static String fixedLenghtNumeric(int i)
	{
		if (i < 10) return "00" + i;
		if (i < 100) return "0" + i;
		return i + "";
	}
}
