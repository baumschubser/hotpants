package dk.onlinecity.qrr;

import dk.onlinecity.qrr.detect.FinderPattern;
import dk.onlinecity.qrr.detect.FinderPatternException;

public class DetectInfo
{
	public static boolean DEBUG_INFO = true;

	private static DetectInfo instance = null;

	private FinderPattern[] fp;

	private double maxModuleSize;
	private int provisionalVersion;

	private int[] fp0LowerBorderCenter;
	private int[] fp0LowerBorderEdge;

	private int[] fp1LowerBorderCenter;
	private int[] fp1LowerBorderEdge;

	private int[] fp0RightBorderCenter;
	private int[] fp0RightBorderEdge;

	private int[] fp2RightBorderCenter;
	private int[] fp2RightBorderEdge;

	private DetectInfo()
	{

	}

	public static DetectInfo getInstance()
	{
		if (instance == null) {
			instance = new DetectInfo();
		}
		return instance;
	}

	/**
	 * @param fp
	 */
	public void setFinderPattern(FinderPattern[] fp)
	{
		this.fp = fp;
	}

	/**
	 * 
	 * @return
	 * @throws FinderPatternException
	 */
	public FinderPattern[] getFinderPatterns() throws FinderPatternException
	{
		if (fp == null) throw new FinderPatternException();
		return fp;
	}

	/**
	 * 
	 * @param maxModuleSize
	 */
	public void setMaxModuleSize(double maxModuleSize)
	{
		this.maxModuleSize = maxModuleSize;
	}

	/**
	 * 
	 * @return
	 */
	public double getMaxModuleSize()
	{
		return maxModuleSize;
	}

	public void setProvisionalVersion(int provisionalVersion)
	{
		this.provisionalVersion = provisionalVersion;
	}

	public int getProvisionalVersion()
	{
		return provisionalVersion;
	}

	/**
	 * 
	 * @return
	 */
	public final int[] getFp0LowerBorderCenter()
	{
		return fp0LowerBorderCenter;
	}

	public final void setFp0LowerBorderCenter(int[] fp0LowerBorderCenter)
	{
		this.fp0LowerBorderCenter = fp0LowerBorderCenter;
	}

	public final int[] getFp0LowerBorderEdge()
	{
		return fp0LowerBorderEdge;
	}

	public final void setFp0LowerBorderEdge(int[] fp0LowerBorderEdge)
	{
		this.fp0LowerBorderEdge = fp0LowerBorderEdge;
	}

	public final int[] getFp1LowerBorderCenter()
	{
		return fp1LowerBorderCenter;
	}

	public final void setFp1LowerBorderCenter(int[] fp1LowerBorderCenter)
	{
		this.fp1LowerBorderCenter = fp1LowerBorderCenter;
	}

	public final int[] getFp1LowerBorderEdge()
	{
		return fp1LowerBorderEdge;
	}

	public final void setFp1LowerBorderEdge(int[] fp1LowerBorderEdge)
	{
		this.fp1LowerBorderEdge = fp1LowerBorderEdge;
	}

	public final int[] getFp0RightBorderCenter()
	{
		return fp0RightBorderCenter;
	}

	public final void setFp0RightBorderCenter(int[] fp0RightBorderCenter)
	{
		this.fp0RightBorderCenter = fp0RightBorderCenter;
	}

	public final int[] getFp0RightBorderEdge()
	{
		return fp0RightBorderEdge;
	}

	public final void setFp0RightBorderEdge(int[] fp0RightBorderEdge)
	{
		this.fp0RightBorderEdge = fp0RightBorderEdge;
	}

	public final int[] getFp2RightBorderCenter()
	{
		return fp2RightBorderCenter;
	}

	public final void setFp2RightBorderCenter(int[] fp2RightBorderCenter)
	{
		this.fp2RightBorderCenter = fp2RightBorderCenter;
	}

	public final int[] getFp2RightBorderEdge()
	{
		return fp2RightBorderEdge;
	}

	public final void setFp2RightBorderEdge(int[] fp2RightBorderEdge)
	{
		this.fp2RightBorderEdge = fp2RightBorderEdge;
	}
}
