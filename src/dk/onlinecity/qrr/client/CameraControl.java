/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.client;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;

import dk.onlinecity.qrr.client.probe.SnapshotProfile;
import dk.onlinecity.qrr.client.probe.Probe;

public class CameraControl
{
	private static CameraControl instance;
	private Player player;
	private VideoControl videoControl;
	private CameraHandler cameraHandler;
	private Vector profiles;
	private SnapshotProfile profile = null;

	private CameraControl()
	{
		if (profiles == null) {
			profiles = Probe.getSnapshotProfiles();
		}

		if (player == null) {
			initPlayer();
		} else {
			int state = player.getState();
			switch (state) {
			case Player.UNREALIZED:
				System.out.println("player is unrealized");
				initPlayer();
				break;
			case Player.REALIZED:
				System.out.println("player is realized");
				break;
			case Player.PREFETCHED:
				System.out.println("player is prefetched");
				break;
			case Player.STARTED:
				System.out.println("player is started");
				break;
			case Player.CLOSED:
				System.out.println("player is closed");
				break;
			}
		}
	}

	private Player createPlayer()
	{
		String platform = System.getProperty("microedition.platform");
		try {
			if (platform != null && platform.toLowerCase().indexOf("nokia") >= 0) return Manager.createPlayer("capture://image");
		} catch (MediaException me) {
		} catch (Exception e) {
		}

		try {
			return Manager.createPlayer("capture://video");
		} catch (IOException e) {
		} catch (MediaException e) {
		}

		return null;
	}

	private void initPlayer()
	{
		try {
			player = createPlayer();
                        player.realize();
			player.prefetch();
			cameraHandler = new DefaultCameraHandler();
			cameraHandler.zoom(player);
			cameraHandler.exposure(player);
			cameraHandler.focus(player);
			System.out.println("player is realized and prefetched");
		} catch (MediaException e) {
		}

	}

	public static CameraControl getInstance()
	{
		if (instance == null) {
			instance = new CameraControl();
		}
		return instance;
	}

	public Player getPlayer()
	{
		return player;
	}

	public VideoControl getVideoControl(Canvas canvas)
	{
		if (videoControl == null) {
			videoControl = (VideoControl) player.getControl("VideoControl");
			videoControl.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, canvas);

		}
		return videoControl;
	}

	public byte[] getSnapshot() throws RuntimeException
	{
		if (videoControl == null) {
			throw new RuntimeException("VideoControl = null");
		}

		cameraHandler.focus(player);
		cameraHandler.exposure(player);
		cameraHandler.zoom(player);

		if (profile != null) {
			try {
				return videoControl.getSnapshot(profile.getEncodingString());
			} catch (MediaException e) {
				e.printStackTrace();
			}
		}

		byte[] tmp;
		int l = profiles.size();
		System.out.println("profiles:" + l);
		for (int i = 0; i < l; i++) {
			profile = (SnapshotProfile) profiles.elementAt(i);
			try {
				System.out.println("using:" + profile.getEncodingString());

				tmp = videoControl.getSnapshot(profile.getEncodingString());
				if (tmp != null) return tmp;
			} catch (MediaException e) {

			} catch (NullPointerException e) {

			}
		}

		try {
			return videoControl.getSnapshot(null);
		} catch (MediaException e) {
		}

		throw new RuntimeException("Could not use camera");
	}
}
