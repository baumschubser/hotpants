/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.perspective;

/**
 * The perspective transformer calculates the transition matrix for changing
 * perspective from ( (x0, y0), (x1, y1), (x2, y2), (x3, y3) ) to ((u0, v0),
 * (u1, v1), (u2, v2), (u3, v3)). When a <code>PerspectiveTransformer</code> is
 * constructed, it calculates the transition matrix and constructs a
 * {@link CoordinateTranslator}, that can be obtained by calling the
 * <code>getCoordinateTranslator</code> method.
 * 
 * @author Thomas Duerlund (td@onlinecity.dk)
 * 
 */
public class PerspectiveTransformer
{
	private PerspectiveTransformer()
	{

	}

	public static CoordinateTranslator getCoordinateTranslator(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3, double u0, double v0,
			double u1, double v1, double u2, double v2, double u3, double v3)
	{
		return getCoordinateTranslator(x0, y0, x1, y1, x2, y2, x3, y3, u0, v0, u1, v1, u2, v2, u3, v3, 0, 0);
	}

	/**
	 * Returns a new {@link CoordinateTranslator}.
	 * 
	 * @param x0
	 *            X0 coordinate.
	 * @param y0
	 *            Y0 coordinate.
	 * @param x1
	 *            X1 coordinate.
	 * @param y1
	 *            Y1 coordinate.
	 * @param x2
	 *            x2 coordinate.
	 * @param y2
	 *            Y2 coordinate.
	 * @param x3
	 *            X3 coordinate.
	 * @param y3
	 *            Y3 coordinate.
	 * @param u0
	 *            U0 coordinate.
	 * @param v0
	 *            V0 coordinate.
	 * @param u1
	 *            U1 coordinate.
	 * @param v1
	 *            V1 coordinate.
	 * @param u2
	 *            U2 coordinate.
	 * @param v2
	 *            V2 coordinate.
	 * @param u3
	 *            U3 coordinate.
	 * @param v3
	 *            V3 coordinate.
	 * @return
	 */
	public static CoordinateTranslator getCoordinateTranslator(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3, double u0, double v0,
			double u1, double v1, double u2, double v2, double u3, double v3, int w, int h)
	{
		SimpleMatrix transitionMatrix = new SimpleMatrix(8, 9);
		double[] x = new double[] { x0, x1, x2, x3 };
		double[] y = new double[] { y0, y1, y2, y3 };
		double[] u = new double[] { u0, u1, u2, u3 };
		double[] v = new double[] { v0, v1, v2, v3 };

		for (int i = 0; i < 4; i++) {
			transitionMatrix.setRow(new double[] { x[i], y[i], 1, 0, 0, 0, (-x[i] * u[i]), (-y[i] * u[i]), u[i] }, i);
		}

		for (int i = 0; i < 4; i++) {
			transitionMatrix.setRow(new double[] { 0, 0, 0, x[i], y[i], 1, (-x[i] * v[i]), (-y[i] * v[i]), v[i] }, i + 4);
		}

		transitionMatrix.toReducedRowEchelonForm();

		double[] coefficients = new double[transitionMatrix.getRows()];

		for (int i = 0; i < transitionMatrix.getRows(); i++) {
			coefficients[i] = transitionMatrix.get(i, transitionMatrix.getColumns() - 1);
		}
		return new CoordinateTranslator(coefficients);
	}
}
