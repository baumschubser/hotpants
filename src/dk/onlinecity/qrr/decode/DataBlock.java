/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.decode;

final class DataBlock
{
	final static int DATA = 0;
	final static int EC = 1;

	private int[] words;
	private int ecWords;
	private int ecOffset;

	DataBlock(int dataWords, int ecWords)
	{
		words = new int[dataWords + ecWords];
		this.ecWords = ecWords;
		ecOffset = dataWords;
	}

	final void set(int index, byte word, int type)
	{

		if (type == DATA) {
			words[index] = word & 0xff;
		}

		if (type == EC) {
			words[index + ecOffset] = word & 0xff;
		}
	}

	final byte get(int index)
	{
		return (byte) words[index];
	}

	final void print()
	{
		for (int i = 0; i < words.length; i++) {
			System.out.print(words[i] + " ");
		}
		System.out.println();
	}

	final int[] getWords()
	{
		return words;
	}

	final int getNumErrorCorrectionWords()
	{
		return ecWords;
	}

	final int getNumDataWords()
	{
		return ecOffset;
	}
}
