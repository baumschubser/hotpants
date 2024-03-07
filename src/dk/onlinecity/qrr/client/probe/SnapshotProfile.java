/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.client.probe;

import java.util.Vector;

import dk.onlinecity.qrr.util.Tokenizer;

/**
 * An encoding profile contains information about a particular snapshot encoding
 * string from the <code>System.getProperty("video.snapshot.encodings")</code>
 * string.
 * 
 * @author Thomas Duerlund (td@onlinecity.dk)
 */
public class SnapshotProfile
{
	/** Exact encoding string used for the camera. */
	private String encodingString = null;
	/** Encoding format. */
	private String encoding = null;
	/** Snapshot width if any specified, otherwise it is zero. */
	private int height = 0;
	/** Snapshot height if any specified, otherwise it is zero. */
	private int width = 0;

	/**
	 * Constructs the encoding profile and parses the encoding string.
	 * 
	 * @param encodingString
	 *            Encoding string.
	 */
	public SnapshotProfile(String encodingString)
	{
		this.encodingString = encodingString;
		parseEncodingString();
		stripEncodings();
	}

	/**
	 * Parses the encoding profile string.
	 */
	private void parseEncodingString()
	{
		Vector properties = Tokenizer.tokenize(encodingString, "&");
		String[] p;
		int l = properties.size();
		for (int i = 0; i < l; i++) {
			p = Tokenizer.split((String) properties.elementAt(i), "=");
			if (p[0].toLowerCase().equals("encoding")) {
				encoding = p[1];
				continue;
			}

			if (p[0].toLowerCase().equals("width")) {
				width = Integer.parseInt(p[1]);
				continue;
			}

			if (p[0].toLowerCase().equals("height")) {
				height = Integer.parseInt(p[1]);
				continue;
			}
		}
	}

	private void stripEncodings()
	{
		if (encoding.indexOf("/") != -1) {
			encoding = encoding.substring(encoding.indexOf("/") + 1, encoding.length());
			encodingString = encodingString.substring(encodingString.indexOf("/") + 1, encodingString.length());
		}
	}

	/**
	 * @return Exact encoding string.
	 */
	public String getEncodingString()
	{
		return encodingString;
	}

	/**
	 * @return Encoding.
	 */
	public String getEncoding()
	{
		return encoding;
	}

	/**
	 * @return Snapshot height if any specified, otherwise zero is returned.
	 */
	public int getHeight()
	{
		return height;
	}

	/**
	 * @return Snapshot width if any specified, otherwise zero is returned.
	 */
	public int getWidth()
	{
		return width;
	}
}
