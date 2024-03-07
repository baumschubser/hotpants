/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.core.exceptions;

import dk.onlinecity.qrr.MinorException;

public class QrrException extends MinorException
{
	public QrrException()
	{
		super();
	}

	public QrrException(String msg)
	{
		super(msg);
	}
}
