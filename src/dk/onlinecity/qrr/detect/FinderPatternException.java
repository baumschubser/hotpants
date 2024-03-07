/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.detect;

import dk.onlinecity.qrr.MinorException;

/**
 * The Finder pattern exception is thrown if {@link FinderPatternLocator} cannot
 * find at least 3 finder pattern candidates.
 * 
 * @author Thomas Duerlund (td@onlinecity.dk)
 */
public class FinderPatternException extends MinorException
{
	public FinderPatternException()
	{
		super();
	}

	public FinderPatternException(String message)
	{
		super(message);
	}
}
