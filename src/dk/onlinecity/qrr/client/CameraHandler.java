/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.client;

import javax.microedition.media.Player;

public interface CameraHandler
{
	public void focus(Player player);

	public void zoom(Player player);

	public void exposure(Player player);
}
