/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.client;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.GameCanvas;

import dk.onlinecity.qrr.QrReader;
import dk.onlinecity.qrr.image.ImageUtil;
import dk.onlinecity.qrr.image.Thumbnail;

public class DecodeCanvas extends GameCanvas implements Runnable
{
	private QrReaderMidlet midlet;
	private Thread thread;
	private Thumbnail thumbnail;
	private int[] pixels;
	private int ih;
	private int iw;
	private int[] ul;
	private int[] ur;
	private int[] ll;
	private int[] lr;

	private int attempt = 1;
	private int attempts;

	private String message = "No QR Code found.";
	private boolean showMessage = false;
	private String result;

	public DecodeCanvas(QrReaderMidlet midlet, byte[] rawImage)
	{
		super(true);
		// #if !motorola
		setFullScreenMode(true);
		// #endif
		this.midlet = midlet;

		Image tmp = Image.createImage(rawImage, 0, rawImage.length);

		iw = tmp.getWidth();
		ih = tmp.getHeight();

		int tw = getWidth() - 60;
		int th = (int) ((double) tw * ((double) ih / (double) iw));
		pixels = new int[iw * ih];

		tmp.getRGB(pixels, 0, iw, 0, 0, iw, ih);
		thumbnail = ImageUtil.getThumbnail(pixels, iw, ih, tw, th);
		render(getGraphics());
		flushGraphics();
	}

	protected void showNotify()
	{
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}

	}

	public void run()
	{
		decode();
	}

	private void decode()
	{
		QrReader qrReader = new QrReader(pixels, iw, ih, 8, 6);
		attempts = qrReader.getAttempts();
		render(getGraphics());
		flushGraphics();
		try {
			result = null;
			while (result == null) {
				result = qrReader.scan();
				ul = thumbnail.translateCoordinate(qrReader.getUl());
				ur = thumbnail.translateCoordinate(qrReader.getUr());
				ll = thumbnail.translateCoordinate(qrReader.getLl());
				lr = thumbnail.translateCoordinate(qrReader.getLr());
				render(getGraphics());
				flushGraphics();
				// System.out.println("attempt:" + attempt);
				attempt++;
			}

			System.out.println("result:" + result);
			render(getGraphics());
			flushGraphics();

		} catch (Exception e) {
			result = null;
			e.printStackTrace();
		}

		if (result == null) {
			long time = System.currentTimeMillis() + 2500;
			while (System.currentTimeMillis() < time) {
				showMessage = true;
				render(getGraphics());
				flushGraphics();
				Thread.yield();
			}
			midlet.showCamera();
			// midlet.showCamera();
		} else {
			midlet.showResult(result);
		}
		// processingImage = false;
	}

	private void render(Graphics g)
	{
		g.setColor(0x0);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.drawImage(drawSquare(thumbnail.getImage()), getWidth() >> 1, getHeight() >> 1, Graphics.HCENTER | Graphics.VCENTER);
		g = getGraphics();
		if (showMessage) {
			g.setColor(0xffffff);
			g.setFont(Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
			g.drawString(message, getWidth() >> 1, getHeight() - 5, Graphics.BOTTOM | Graphics.HCENTER);
		} else {
			renderProgress(g, 20, getWidth() - 20, 10);
		}

	}

	private Image drawSquare(Image thumb)
	{
		if (ul != null || ur != null || lr != null || ll != null) {
			Image image = Image.createImage(thumb.getWidth(), thumb.getHeight());
			Graphics g = image.getGraphics();
			g.drawImage(thumb, 0, 0, Graphics.TOP | Graphics.LEFT);
			g.setColor(0x00ff00);
			if (ul != null && ur != null) g.drawLine(ul[0], ul[1], ur[0], ur[1]);
			if (ur != null && lr != null) g.drawLine(ur[0], ur[1], lr[0], lr[1]);
			if (lr != null && ll != null) g.drawLine(lr[0], lr[1], ll[0], ll[1]);
			if (ll != null && ul != null) g.drawLine(ll[0], ll[1], ul[0], ul[1]);
			return image;
		}
		return thumb;
	}

	private int getProcess(int x0, int x1)
	{
		int w = x1 - x0;
		double progress = (double) attempt / (double) attempts;

		return (int) (progress * ((double) w));
	}

	private void renderProgress(Graphics g, int x0, int x1, int height)
	{
		g.setColor(0xffffff);
		g.fillRect(x0, getHeight() - height - 10, x1 - x0, height);
		g.setColor(0);
		g.fillRect(x0 + 1, getHeight() - height - 10 + 1, x1 - x0 - 2, height - 2);
		g.setColor(0xffffff);
		g.fillRect(x0 + 2, getHeight() - height - 10 + 2, getProcess(x0, x1 - 2 - 2), height - 4);
	}
}
