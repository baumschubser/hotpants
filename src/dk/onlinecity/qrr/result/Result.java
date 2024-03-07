/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.result;

public abstract class Result
{
	public static final int TEXT = 0;
	public static final int URL = 1;
	public static final int SMS = 2;

	private int type;

	public Result(int type)
	{
		this.type = type;
		System.out.print("Result type:");
		switch (type) {
		case TEXT:
			System.out.println("Text");
			break;
		case URL:
			System.out.println("URL");
			break;
		case SMS:
			System.out.println("SMS");
			break;
		default:
			System.out.println(type);
		}
	}

	public int getType()
	{
		return type;
	}

	public abstract String toString();
}
