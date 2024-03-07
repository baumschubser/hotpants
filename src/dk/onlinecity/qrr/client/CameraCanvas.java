/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.client;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class CameraCanvas extends Canvas
{
	private static CameraCanvas instance = null;
	private final QrReaderMidlet midlet;
	private InitCamera initCamera = null;
	private String message = "Opening camera...";

	private CameraCanvas(QrReaderMidlet midlet)
	{
		// #if !motorola
		setFullScreenMode(true);
		// #endif
		this.midlet = midlet;
	}

	public static CameraCanvas getInstance(QrReaderMidlet midlet)
	{
		if (instance == null) instance = new CameraCanvas(midlet);

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

	protected void keyPressed(int keyCode)
	{
		switch (keyCode) {
		case 0: // idle
			break;
		case -7: // right soft key
		case -8: // delete
		case -11: // back
			Alert exitAlert = new Alert("", "Exit OC QR-Reader?", null, AlertType.CONFIRMATION);
			exitAlert.addCommand(new Command("OK", Command.OK, 1));
			exitAlert.addCommand(new Command("Cancel", Command.CANCEL, 1));
			exitAlert.setCommandListener(new CommandListener()
			{
				public void commandAction(Command cmd, Displayable d)
				{
					if (cmd.getCommandType() == Command.OK)
						midlet.notifyDestroyed();
					else
						midlet.showCamera();
				}
			});
			Display.getDisplay(midlet).setCurrent(exitAlert);
			break;
		default:
			midlet.takeSnapshot();
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
			setMessage("Please wait...");
			midlet.showCamera();
		}
	}
}
