/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */
package dk.onlinecity.qrr.result;

public class TextResult extends Result
{
	private String text;

	public TextResult(String text)
	{
		super(Result.TEXT);
		this.text = text;
	}

	public String getText()
	{
		return text;
	}

	public String toString()
	{
		return text;
	}
}
