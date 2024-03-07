/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.perspective;

/**
 * Class representing a matrix, that can bring it self on reduced row echelon
 * form.
 * 
 * @author Thomas Duerlund (td@onlinecity.dk)
 */
final class SimpleMatrix
{
	/** Double double array containing the matrix. */
	private double[][] matrix;
	/** Matrix rows. */
	private int m;
	/** Matrix columns. */
	private int n;

	/**
	 * Constructs an empty SimpleMatrix with m rows and n columns.
	 * 
	 * @param m
	 *            Rows to construct.
	 * @param n
	 *            Columns to construct.
	 */
	SimpleMatrix(int m, int n)
	{
		this.m = m;
		this.n = n;
		matrix = new double[m][n];
	}

	/**
	 * Sets the specified row <code>m</code> with another row <code>row</code>.
	 * 
	 * @param row
	 *            Row to replace with.
	 * @param m
	 *            Row number to set.
	 */
	void setRow(double[] row, int m)
	{
		matrix[m] = row;
	}

	/**
	 * Returns the matrix entry at (i, j).
	 * 
	 * @param i
	 *            Row coordinate.
	 * @param j
	 *            Column coordinate.
	 * @return Matrix entry at (i, j).
	 */
	double get(int i, int j)
	{
		return matrix[i][j];
	}

	/**
	 * Returns a row.
	 * 
	 * @param m
	 *            Row number.
	 * @return Row <code>m</code>.
	 */
	double[] getRow(int m)
	{
		return matrix[m];
	}

	/**
	 * Multiplies all entry in row <code>m</code> with <code>mult</code>.
	 * 
	 * @param m
	 *            Row number.
	 * @param mult
	 *            Multiplier.
	 */
	void multiplyRow(int m, double mult)
	{
		double[] row = getRow(m);
		for (int i = 0; i < row.length; i++) {
			row[i] *= mult;
		}
		setRow(row, m);
	}

	/**
	 * Multiplies row <code>j</code> with <code>mult</code> and adds it to row
	 * <code>i</code>.
	 * 
	 * @param i
	 *            The i'th row.
	 * @param j
	 *            The j'th row.
	 * @param mult
	 *            I'th row multiplier.
	 */
	void addMult(int i, int j, double mult)
	{
		double[] dst = getRow(i);
		double[] row = getRow(j);

		for (int k = 0; k < dst.length; k++) {
			dst[k] = dst[k] + (mult * row[k]);
		}
		setRow(dst, i);
	}

	/**
	 * Swaps two rows.
	 * 
	 * @param i
	 *            First row.
	 * @param j
	 *            Second row.
	 */
	void swapRows(int i, int j)
	{
		double[] tmp = getRow(i);
		setRow(getRow(j), i);
		setRow(tmp, j);
	}

	/**
	 * Returns the number of leading zeros in the i'th row.
	 * 
	 * @param i
	 *            Row index.
	 * @return Number of leading zeros in the i'th row.
	 */
	int leadingZeros(int i)
	{
		int zeros = 0;
		boolean pivotPassed = false;

		double[] row = getRow(i);
		for (int j = 0; j < row.length; j++) {
			if (row[j] != 0) {
				pivotPassed = true;
			}

			if (!pivotPassed && row[j] == 0) {
				zeros++;
			}
		}
		return zeros;
	}

	/**
	 * Brings the matrix on reduced row echelon form.
	 */
	void toReducedRowEchelonForm()
	{
		orderMatrix();
		int lead = 0;
		int rowCount = m;
		int columnCount = n;

		for (int r = 0; r < rowCount; r++) {
			if (columnCount <= lead) {
				break;
			}
			int i = r;
			while (get(i, lead) == 0.0) {
				i++;
				if (rowCount == i) {
					i = r;
					lead++;
					if (columnCount <= lead) {
						break;
					}
				}
			}
			swapRows(i, r);

			double mult = ((double) 1 / get(r, lead));
			multiplyRow(r, mult);
			for (int ii = 0; ii < m; ii++) {
				if (ii != r) {
					addMult(ii, r, -1.0 * get(ii, lead));
				}
			}
			lead++;
		}
	}

	/**
	 * Returns number of rows.
	 * 
	 * @return Number of rows.
	 */
	int getRows()
	{
		return m;
	}

	/**
	 * Returns number of columns.
	 * 
	 * @return Number of columns.
	 */
	int getColumns()
	{
		return n;
	}

	/**
	 * Prints the matrix.
	 */
	void print()
	{
		System.out.println("---");
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				System.out.print(matrix[i][j] + "  ");
			}
			System.out.println();
		}
		System.out.println("---");
	}

	/**
	 * Prints the specified row.
	 * 
	 * @param i
	 *            Row number.
	 */
	void printRow(int i)
	{
		for (int j = 0; j < n; j++) {
			System.out.print("(" + i + ", " + j + ")=" + matrix[i][j] + "  ");
		}
		System.out.println();
	}

	/**
	 * Orders the matrix, based on insertion sort.
	 */
	void orderMatrix()
	{
		Row[] array = rowArray();

		for (int i = 1; i < array.length; i++) {
			Row value = array[i];
			int j = i - 1;
			// no compareTo method here, now move along.
			while (j >= 0 && array[j].getLeadingZeros() >= value.getLeadingZeros()) {
				array[j + 1] = array[j];
				j--;
			}
			array[j + 1] = value;
		}

		// replace the matrix rows with the sorted rows;
		for (int i = 0; i < array.length; i++) {
			setRow(array[i].getRow(), i);
		}
	}

	/**
	 * 
	 * @return
	 */
	Row[] rowArray()
	{
		Row[] array = new Row[m];
		for (int i = 0; i < m; i++) {
			Row row = new Row(i, leadingZeros(i), getRow(i));
			array[i] = row;
		}
		return array;
	}
}

class Row
{
	private int rowIndex;
	private int leadingZeros;
	private double[] row;

	Row(int rowIndex, int leadingZeros, double[] row)
	{
		this.rowIndex = rowIndex;
		this.leadingZeros = leadingZeros;
		this.row = row;
	}

	int getLeadingZeros()
	{
		return leadingZeros;
	}

	int getRowIndex()
	{
		return rowIndex;
	}

	double[] getRow()
	{
		return row;
	}
}