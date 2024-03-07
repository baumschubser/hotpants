/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.decode;

import dk.onlinecity.qrr.core.exceptions.DecodeException;

final class HammingDistance
{
	private HammingDistance()
	{
	}

	/**
	 * <p>
	 * Returns the hamming distance between two integers.
	 * </p>
	 * <p>
	 * <a href="http://en.wikipedia.org/wiki/Hamming_distance">http://en.
	 * wikipedia.org/wiki/Hamming_distance</a>
	 * </p>
	 * 
	 * @param a
	 *            Integer a.
	 * @param b
	 *            Integer b.
	 * @return Hamming distance between <code>a</code> and <code>b</code>.
	 */
	final static int getHammingDistance(int a, int b)
	{
		int hamDist = 0;
		int value = a ^ b;

		while (value != 0) {
			++hamDist;
			value &= value - 1;
		}
		return hamDist;
	}

	/**
	 * Returns the {@link Candidate} with the lowest hamming distance.
	 * 
	 * @param candidates
	 *            Array of {@link Candidate}.
	 * @return Value of the {@link Candidate} with the lowest hamming distance.
	 * @throws DecodeException
	 *             Thrown if the candidate array is empty.
	 */
	final static int getBestCandidate(Candidate[] candidates) throws DecodeException
	{
		int l = candidates.length;
		if (l == 0) throw new DecodeException("No candidates");

		int j;
		Candidate value;

		// sort the candidates using insertion sort.
		for (int i = 0; i < l; i++) {
			value = candidates[i];
			j = i - 1;
			while (j >= 0 && candidates[j].getHamDist() > value.getHamDist()) {
				candidates[j + 1] = candidates[j];
				j--;
			}
			candidates[j + 1] = value;
		}

		return candidates[0].getValue();
	}
}

final class Candidate
{
	private final int value;
	private final int hamDist;

	public Candidate(int value, int hamDist)
	{
		this.value = value;
		this.hamDist = hamDist;
	}

	final int getValue()
	{
		return value;
	}

	final int getHamDist()
	{
		return hamDist;
	}
}
