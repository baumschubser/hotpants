/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr;

import dk.onlinecity.qrr.core.exceptions.DecodeException;
import dk.onlinecity.qrr.decode.QrDecoder;
import dk.onlinecity.qrr.detect.FinderPatternException;
import dk.onlinecity.qrr.detect.QrDetector;
import dk.onlinecity.qrr.extract.QrExtract;
import dk.onlinecity.qrr.image.ImageUtil;
import dk.onlinecity.qrr.image.MonochromeImage;

/**
 * The QR reader scans and reads an image for QR-Codes.
 * 
 * @author Thomas Duerlund (td@onlinecity.dk).
 * 
 */
public class QrReader
{
	/** Threshold candidates. */
	private final int[] tCandidates;
	/** Threshold candidate index. */
	private int tIndex;
	/** Number of threshold candidate values. */
	private final int tLen;
	/** {@link QrDetector} */
	private QrDetector qrDetector;
	/** {@link QrCandidate}. */
	private QrCandidate[] qrCandidates;
	/** {@link MonochromeImage} to scan for QR-Codes. */
	private final MonochromeImage image;
	/** Upper left corner of a potential QR-Code. */
	private int[] ul;
	/** Upper right corner of a potential QR-Code. */
	private int[] ur;
	/** Lower left corner of a potential QR-Code. */
	private int[] ll;
	/** Lower right corner of a potential QR-Code. */
	private int[] lr;

	public QrReader(int[] pixels, int w, int h, int gw, int gh)
	{
		ImageUtil iu = new ImageUtil(pixels, w, h, gw, gh);
		image = iu.getMonochromeImage();
		tCandidates = iu.getThresholdValues();
		tIndex = 0;
		tLen = tCandidates.length;
		qrDetector = new QrDetector(image);

		ul = null;
		ur = null;
		ll = null;
		lr = null;
	}

	/**
	 * @return
	 * @throws QrReaderException
	 */
	public String scan() throws QrReaderException
	{
		String res = null;
		image.setThreshold(tCandidates[tIndex]);

		try {
			qrCandidates = qrDetector.detectCandidates(image);
			for (int i = 0; i < qrCandidates.length; i++) {
				ul = qrCandidates[i].getUl();
				ur = qrCandidates[i].getUr();
				ll = qrCandidates[i].getLl();
				lr = qrCandidates[i].getLr();
				// qrCandidates[i].print();
				try {
					res = QrDecoder.decode(QrExtract.extract(image, qrCandidates[i]));
					if (res != null) return res;
				} catch (DecodeException e) {
					// System.out.println("decode exception");
					// e.printStackTrace();
				}
			}
		} catch (FinderPatternException e) {
			// System.out.println("finder pattern exception");
			// e.printStackTrace();
		}

		if (tLen - 1 < ++tIndex) throw new QrReaderException("Could not find any QR-Code");
		return null;
	}

	public String autoScan() throws QrReaderException
	{
		String result = null;
		while (result == null)
			result = scan();
		return result;
	}

	public int getAttempts()
	{
		return tLen;
	}

	public int[] getUl()
	{
		return ul == null ? new int[] { -1, -1 } : ul;
	}

	public int[] getUr()
	{
		return ur == null ? new int[] { -1, -1 } : ur;
	}

	public int[] getLl()
	{
		return ll == null ? new int[] { -1, -1 } : ll;
	}

	public int[] getLr()
	{
		return lr == null ? new int[] { -1, -1 } : lr;
	}
}
