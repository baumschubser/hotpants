/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.client;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import dk.onlinecity.qrr.result.Result;
import dk.onlinecity.qrr.result.ResultParser;
import dk.onlinecity.qrr.result.TextResult;
import dk.onlinecity.qrr.result.UrlResult;

public class QrReaderMidlet extends MIDlet
{
	public final static Command CMD_NEW = new Command("New", Command.OK, 1);
	public final static Command CMD_EXIT = new Command("Exit", Command.EXIT, 1);
	public final static Command CMD_OK = new Command("OK", Command.OK, 1);
	public final static Command CMD_CANCEL = new Command("Cancel", Command.CANCEL, 1);

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException
	{
	}

	protected void pauseApp()
	{

	}

	protected void startApp() throws MIDletStateChangeException
	{
		Display.getDisplay(this).setCurrent(CameraCanvas.getInstance(this));
	}

	void showCamera()
	{
		CameraControl cameraControl = CameraControl.getInstance();
		CameraCanvas cameraCanvas = CameraCanvas.getInstance(this);
		Player player = cameraControl.getPlayer();
		VideoControl videoControl = cameraControl.getVideoControl(cameraCanvas);
		try {
			videoControl.setDisplayFullScreen(true);
			videoControl.setDisplayLocation(0, 0);
			player.start();
			videoControl.setVisible(true);
			Display.getDisplay(this).setCurrent(cameraCanvas);
		} catch (MediaException e) {
			e.printStackTrace();
		}
	}

	void takeSnapshot()
	{
		CameraControl cameraControl = CameraControl.getInstance();
		showDecoding(cameraControl.getSnapshot());
	}

	void showDecoding(byte[] rawImage)
	{
		CameraControl cameraControl = CameraControl.getInstance();
		Player player = cameraControl.getPlayer();
		try {
			player.stop();
		} catch (MediaException e) {
		}
		DecodeCanvas decodeCanvas = new DecodeCanvas(this, rawImage);
		Display.getDisplay(this).setCurrent(decodeCanvas);
	}

	void showResult(String result)
	{
		Result res = ResultParser.parseResult(result);

		switch (res.getType()) {
		// case Result.SMS:
		// break;
		case Result.URL:
			openUrl((UrlResult) res);
			break;
		default:
			showText((TextResult) res);
			break;
		}
	}

	/**
	 * Displays a free text message.
	 * 
	 * @param text
	 *            Text to display.
	 */
	private void showText(final TextResult text)
	{
		Form form = new Form("Text");
		form.append(text.getText());
		form.addCommand(CMD_NEW);
		form.addCommand(CMD_EXIT);
		form.setCommandListener(new CommandListener()
		{

			public void commandAction(Command cmd, Displayable d)
			{
				if (cmd == CMD_NEW) {
					showCamera();
				} else if (cmd == CMD_EXIT) {
					notifyDestroyed();
				}
			}
		});
		Display.getDisplay(this).setCurrent(form);
	}

	/**
	 * Opens an URL.
	 * 
	 * @param url
	 *            URL to open.
	 */
	private void openUrl(final UrlResult url)
	{
		Alert alert = new Alert("", "Open \"" + url.getUrl() + "\" in browser?", null, AlertType.CONFIRMATION);
		alert.setTimeout(Alert.FOREVER);
		alert.addCommand(CMD_OK);
		alert.addCommand(CMD_CANCEL);
		alert.setCommandListener(new CommandListener()
		{
			public void commandAction(Command cmd, Displayable d)
			{
				if (cmd.getCommandType() == Command.OK) {
					try {
						platformRequest(url.getFullUrl());
					} catch (ConnectionNotFoundException e) {
					} catch (SecurityException e) {
					} finally {
						notifyDestroyed();
					}
				} else if (cmd.getCommandType() == Command.CANCEL) {
					showCamera();
				}
			}
		});
		Display.getDisplay(this).setCurrent(alert);
	}
}
