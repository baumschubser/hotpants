/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.client;

import javax.microedition.media.Player;

public class DefaultCameraHandler implements CameraHandler
{
	CameraHandler cameraHandler = null;

	public DefaultCameraHandler()
	{
		// #if amms
		try {
			// try to create a new instance of AdvancedCameraHandler
			cameraHandler = (CameraHandler) Class.forName("dk.onlinecity.qrr.client.AdvancedCameraHandler").newInstance();
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		} catch (ClassNotFoundException e) {
		} catch (Exception e) {
		}
		// #endif
	}

	public void focus(Player player)
	{
		if (player == null && cameraHandler == null) return;
		cameraHandler.focus(player);
	}

	public void zoom(Player player)
	{
		if (player == null && cameraHandler == null) return;
		cameraHandler.zoom(player);
	}

	public void exposure(Player player)
	{
		if (player == null && cameraHandler == null) return;
		cameraHandler.exposure(player);
	}
}
