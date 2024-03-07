/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.client;

import javax.microedition.media.MediaException;
import javax.microedition.media.Player;

public class AdvancedCameraHandler implements CameraHandler
{
	private static final int DEFAULT_ZOOM = 100;
	private static final int MAX_OPTICAL_ZOOM = 200;
	private static final int MAX_DIGITAL_ZOOM = 250; // 150;

	public AdvancedCameraHandler() throws ClassNotFoundException
	{
		javax.microedition.amms.control.camera.FocusControl dummyFocusControl;
		javax.microedition.amms.control.camera.ZoomControl dummyZoomControl;
		javax.microedition.amms.control.camera.ExposureControl dummyExposure;
	}

	public void focus(Player player)
	{
		System.out.println("advanced camera handler: focus()");
		try {
			javax.microedition.amms.control.camera.FocusControl focus = (javax.microedition.amms.control.camera.FocusControl) player
					.getControl("javax.microedition.amms.control.camera.FocusControl");
			if (focus == null) {
				focus = (javax.microedition.amms.control.camera.FocusControl) player.getControl("FocusControl");
			}

			if (focus == null) {
				System.out.println("FocusControl not available.");
				return;
			}

			try {
				if (focus.isMacroSupported() && !focus.getMacro()) {
					focus.setMacro(true);
				}
			} catch (MediaException e) {
				System.out.println("marco is not supported");
			}

			try {
				if (focus.isAutoFocusSupported()) {
					focus.setFocus(javax.microedition.amms.control.camera.FocusControl.AUTO);
					try {
						Thread.sleep(750L);
					} catch (InterruptedException e) {
					}
					focus.setFocus(javax.microedition.amms.control.camera.FocusControl.AUTO_LOCK);
				}
			} catch (MediaException e) {
			}

		} catch (ClassCastException e) {
		}

		catch (NoClassDefFoundError e) {

		}
	}

	public void zoom(Player player)
	{
		System.out.println("advanced camera handler: zoom()");
		try {
			javax.microedition.amms.control.camera.ZoomControl zoom = (javax.microedition.amms.control.camera.ZoomControl) player
					.getControl("javax.microedition.amms.control.camera.ZoomControl");

			if (zoom == null) {
				zoom = (javax.microedition.amms.control.camera.ZoomControl) player.getControl("ZoomControl");
			}

			if (zoom == null) {
				System.out.println("ZoomControl not available.");
				return;
			}

			// try to use optical zoom if available
			int maxOpticalZoom = zoom.getMaxOpticalZoom();
			if (maxOpticalZoom > DEFAULT_ZOOM) {
				zoom.setOpticalZoom(maxOpticalZoom > MAX_OPTICAL_ZOOM ? MAX_OPTICAL_ZOOM : maxOpticalZoom);
			} else { // optical zoom is not available, try to use digital zoom
				int maxDigitalZoom = zoom.getMaxDigitalZoom();
				zoom.setDigitalZoom(maxDigitalZoom > MAX_DIGITAL_ZOOM ? MAX_DIGITAL_ZOOM : maxDigitalZoom);
			}

		} catch (ClassCastException e) {

		} catch (NoClassDefFoundError e) {

		}

	}

	public void exposure(Player player)
	{
		System.out.println("advanced camera handler: exposure()");
		try {
			javax.microedition.amms.control.camera.ExposureControl exposure = (javax.microedition.amms.control.camera.ExposureControl) player
					.getControl("javax.microedition.amms.control.camera.ExposureControl");
			if (exposure == null) {
				exposure = (javax.microedition.amms.control.camera.ExposureControl) player.getControl("ExposureControl");
			}

			if (exposure != null) {
				int[] isos = exposure.getSupportedISOs();
				int l = isos.length;
				if (isos != null && l > 0) {
					int maxIso = Integer.MIN_VALUE;
					for (int i = 0; i < l; i++) {
						maxIso = maxIso < isos[i] ? isos[i] : maxIso;
					}
					try {
						exposure.setISO(maxIso);
					} catch (MediaException e) {
						e.printStackTrace();
					}

				}

				String[] meterings = exposure.getSupportedLightMeterings();
				l = meterings.length;

				if (meterings != null) {
					for (int i = 0; i < l; i++) {
						if (meterings[i].equals("center-weighted")) {
							exposure.setLightMetering("center-weighted");
							i = l;
						}
					}
				}
			}
		} catch (ClassCastException e) {
		} catch (NoClassDefFoundError e) {
		}
	}
}
