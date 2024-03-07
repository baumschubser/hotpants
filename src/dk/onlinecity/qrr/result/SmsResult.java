/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.result;

public class SmsResult extends Result
{
	private String phoneNumber;
	private String text;

	public SmsResult(String phoneNumber, String text)
	{
		super(Result.SMS);
		this.phoneNumber = phoneNumber;
		this.text = text;
	}

	public String getPhoneNumber()
	{
		return phoneNumber;
	}

	public String getText()
	{
		return text;
	}

	public String toString()
	{
		return "SMSTO:" + phoneNumber + ":" + text;
	}
}
