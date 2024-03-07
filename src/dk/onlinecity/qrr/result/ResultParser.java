/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.result;

public class ResultParser
{
	private static Parser[] parsers = new Parser[] { new UrlParser(), new SmsParser() };

	private ResultParser()
	{

	}

	public static Result parseResult(String str)
	{
		Result result;
		int l = parsers.length;

		for (int i = 0; i < l; i++) {
			if ((result = parsers[i].parse(str)) != null) return result;
		}
		return new TextResult(str);
	}

	private static class UrlParser extends Parser
	{
		public Result parse(String str)
		{
			if (str.substring(0, 7).equals("http://")) return parseUrl(str);

			if (str.substring(0, 4).equals("www.")) return parseUrl(str);

			return null;
		}

		private UrlResult parseUrl(String str)
		{
			return new UrlResult(str);
		}
	}

	/**
	 * Sms parser.
	 * 
	 * @author Thomas Duerlund (td@onlinecity.dk)
	 */
	private static class SmsParser extends Parser
	{
		public Result parse(String str)
		{
			if (str.substring(0, 6).toLowerCase().equals("smsto:")) {
				str = str.substring(6);
				int delimIndex = str.indexOf(":");
				String phoneNumber = str.substring(0, delimIndex);
				String text = str.substring(++delimIndex);
				return new SmsResult(phoneNumber, text);
			}
			if (str.substring(0, 6).toLowerCase().equals("sms://")) {
				String sub = str.substring(6);
				int delimIndex = str.indexOf(":");
				String phoneNumber = sub.substring(0, delimIndex);
				String text = sub.substring(++delimIndex);
				return new SmsResult(phoneNumber, text);
			}
			return null;
		}
	}
}
