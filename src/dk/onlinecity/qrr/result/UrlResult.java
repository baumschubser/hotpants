/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.result;

public class UrlResult extends Result
{
	private String url;

	public UrlResult(String url)
	{
		super(Result.URL);
		this.url = url;
	}

	public String getUrl()
	{
		return url;
	}

	public String getFullUrl()
	{
		url = url.toLowerCase();
		if (url.substring(0, 4).equals("http")) return url;

		return "http://" + url;
	}

	public String toString()
	{
		return "[URL]:" + url;
	}
}
