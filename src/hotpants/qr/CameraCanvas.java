/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package hotpants.qr;

import dk.onlinecity.qrr.client.CameraControl;
import hotpants.Midlet;
import hotpants.ScanForm;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class CameraCanvas extends Canvas
{
	private static CameraCanvas instance = null;
	private final Midlet midlet;
        private final ScanForm scanForm;
	private InitCamera initCamera = null;
	private String message = "Opening camera...";

	private CameraCanvas(Midlet midlet, ScanForm scanForm)
	{
		// #if !motorola
		setFullScreenMode(true);
		// #endif
		this.midlet = midlet;
                this.scanForm = scanForm;
	}

	public static CameraCanvas getInstance(Midlet midlet, ScanForm scanForm)
	{
		if (instance == null) instance = new CameraCanvas(midlet, scanForm);

		if (instance.isShown()) {
			System.out.println("instance is shown");
			instance.repaint();
		}

		return instance;
	}

	protected void showNotify()
	{
		if (initCamera == null) {
			initCamera = new InitCamera();
			new Thread(initCamera).start();
		}
	}

	protected void paint(Graphics g)
	{
		g.setColor(0x000000);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(0xffffff);
		g.setFont(Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_MEDIUM));
		g.drawString(message, getWidth() >> 1, getHeight() >> 1, Graphics.BOTTOM | Graphics.HCENTER);
	}
        
        protected void pointerPressed(int x, int y) {
            scanForm.takeSnapshot();
        }

	protected void keyPressed(int keyCode)
	{
		switch (keyCode) {
		case 0: // idle
			break;
		case -7: // right soft key
		case -8: // delete
		case -11: // back
                    midlet.showMainForm();
                    break;
		default:
                    scanForm.takeSnapshot();
		}
	}

	private void setMessage(String message)
	{
		this.message = message;
	}

	class InitCamera implements Runnable
	{
		public void run()
		{
			CameraControl.getInstance();
			setMessage("Take photo");
		}
	}
}
