/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.decode;

import com.google.zxing.common.reedsolomon.GF256;
import com.google.zxing.common.reedsolomon.ReedSolomonDecoder;
import com.google.zxing.common.reedsolomon.ReedSolomonException;

import dk.onlinecity.qrr.core.exceptions.DecodeException;

/**
 * This class corrects any correctable errors in the parsed data.
 * 
 * @author Thomas Duerlund (td@onlinecity.dk)
 */
final class ErrorCorrection
{
	/** {@link ReedSolomonDecoder}. */
	private final ReedSolomonDecoder reedSolomonDecoder;

	ErrorCorrection()
	{
		reedSolomonDecoder = new ReedSolomonDecoder(GF256.QR_CODE_FIELD);
	}

	/**
	 * Attempts to correct errors in the parsed data.
	 * 
	 * @param data
	 *            Data to error correct.
	 * @param version
	 * @param ecLevel
	 */
	final byte[] errorCorrect(byte[] data, int version, int ecLevel) throws DecodeException
	{

		int[] blockInfo = ErrorCorrectionTable.lookup(version, ecLevel);
		int sb = blockInfo[0];
		int c0 = blockInfo[1];
		int k0 = blockInfo[2];
		int p = ErrorCorrectionTable.getP(version, ecLevel);

		int lb, c1, k1;

		if (blockInfo.length == 6) {
			lb = blockInfo[3];
			c1 = blockInfo[4];
			k1 = blockInfo[5];
		} else {
			lb = 0;
			c1 = 0;
			k1 = 0;
		}

		if (p != 0) {
			System.out.println("VERSION  :" + version);
			System.out.println("ERR LEVEL:" + ErrorCorrectionTable.printErrLevel(ecLevel));
			System.out.println("P        :" + p);
			System.out.println("SHORT BLK:" + sb);
			System.out.println("LONG BLK :" + lb);
		}

		DataBlock[] dataBlocks = getDatablocks(data, sb, c0, k0, lb, c1, k1, p);
		data = new byte[sb * k0 + lb * k1];

		int blocks = dataBlocks.length;
		int index = 0;
		int s;
		for (int i = 0; i < blocks; i++) {
			try {
				reedSolomonDecoder.decode(dataBlocks[i].getWords(), dataBlocks[i].getNumErrorCorrectionWords());
				// append decoded words to final data array
				s = dataBlocks[i].getNumDataWords();
				for (int j = 0; j < s; j++)
					data[index + j] = dataBlocks[i].get(j);
				index += s;
			} catch (ReedSolomonException e) {
				throw new DecodeException("Error correction failed");
			}
		}

		// System.out.println("------------------------------------");

		return data;
	}

	/**
	 * @param data
	 * @param shortBlocks
	 * @param shortBlockTotalWords
	 * @param shortBlockDataWords
	 * @param longBlocks
	 * @param longBlockTotalWords
	 * @param longBlockDataWords
	 * @param p
	 * @return
	 */
	private static DataBlock[] getDatablocks(byte[] data, int shortBlocks, int shortBlockTotalWords, int shortBlockDataWords, int longBlocks, int longBlockTotalWords,
			int longBlockDataWords, int p)
	{
		// determine total number of data words
		int totalWords = data.length;
		int shortBlockEcWords = shortBlockTotalWords - shortBlockDataWords; // -
		// p;
		int longBlockEcWords = longBlockTotalWords - longBlockDataWords; // - p;

		if (p != 0) {
			System.out.println("short block error words:" + shortBlockEcWords);
			System.out.println("long block error words :" + longBlockEcWords);
		}

		// check lengths and return error if mismatch
		if (totalWords != (shortBlocks * shortBlockTotalWords + longBlocks * longBlockTotalWords)) throw new RuntimeException();

		// determine where error correction blocks start
		int errorCorrectionStartIndex = shortBlocks * shortBlockDataWords + longBlocks * longBlockDataWords;

		// determine total number of data blocks
		int blocks = shortBlocks + longBlocks;

		DataBlock[] dataBlocks = new DataBlock[blocks];

		for (int i = 0; i < blocks; i++) {
			if (i < shortBlocks) {
				// System.out.println("block:" + i + " is short. DataWords:" +
				// shortBlockDataWords + " EC:"
				// + shortBlockEcWords);
				dataBlocks[i] = new DataBlock(shortBlockDataWords, shortBlockEcWords);
			} else {
				// System.out.println("block:" + i + " is long. DataWords:" +
				// longBlockDataWords + " EC:"
				// + longBlockEcWords);
				dataBlocks[i] = new DataBlock(longBlockDataWords, longBlockEcWords);
			}
		}

		// read data blocks
		readDataBlocks(data, dataBlocks, shortBlocks, shortBlockDataWords, longBlocks, longBlockDataWords, 0);

		// read error correction blocks
		readDataBlocks(data, dataBlocks, shortBlocks, shortBlockEcWords, longBlocks, longBlockEcWords, errorCorrectionStartIndex);

		return dataBlocks;
	}

	/**
	 * @param data
	 * @param dataBlocks
	 * @param shortBlocks
	 * @param shortBlockWords
	 * @param longBlocks
	 * @param longBlockWords
	 * @param offset
	 */
	private static final void readDataBlocks(byte[] data, DataBlock[] dataBlocks, int shortBlocks, int shortBlockWords, int longBlocks, int longBlockWords, int offset)
	{
		int totalWords = shortBlocks * shortBlockWords + longBlocks * longBlockWords;
		int index = 0;
		int shortIndex = 0;
		int longIndex = 0;
		int block;

		int type = offset == 0 ? DataBlock.DATA : DataBlock.EC;

		while (index < totalWords) {
			if (shortIndex < shortBlockWords) {
				for (block = 0; block < shortBlocks; block++) {
					// System.out.print("block:" + block + " ");
					dataBlocks[block].set(shortIndex, data[index + offset], type);
					index++;
				}
				shortIndex++;
			}

			if (longIndex < longBlockWords) {
				for (block = shortBlocks; block < longBlocks + shortBlocks; block++) {
					// System.out.print("block:" + block + " ");
					dataBlocks[block].set(longIndex, data[index + offset], type);
					index++;
				}
				longIndex++;
			}
		}
	}
}
