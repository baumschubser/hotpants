/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.client.probe;

import java.util.Vector;

import dk.onlinecity.qrr.util.Tokenizer;

public class Probe
{
	private Probe()
	{
	}

	/**
	 * Parses the video_encodings string <a href=
	 * "http://java.sun.com/javame/reference/apis/jsr135/javax/microedition/media/Manager.html#video_encodings"
	 * >encodings</a>
	 */
	public static Vector getSnapshotProfiles()
	{
		Vector encodings = Tokenizer.tokenize(System.getProperty("video.snapshot.encodings"), " ");
		Vector profiles = new Vector();

		int l = encodings.size();
		for (int i = 0; i < l; i++) {
			profiles.addElement(new SnapshotProfile((String) encodings.elementAt(i)));
		}
		return profiles;
	}
}
