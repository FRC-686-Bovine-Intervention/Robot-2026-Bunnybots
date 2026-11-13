package frc.util.math;

import java.util.Arrays;

public class MathExtraUtil {
	public static double average(double a, double b) {
		return (a + b) / 2.0;
	}
	public static double average(double... a) {
		return Arrays.stream(a).average().orElse(0);
	}

	public static boolean isWithin(double value, double min, double max) {
		return value >= min && value <= max;
	}

	public static double dotProduct(double[] a, double[] b) {
		double output = 0;
		for (int i = 0; i < a.length; i++) {
			output += a[i]*b[i];
		}
		return output;
	}

	public static double[] addVectors(double[] a, double[] b) {
		double[] longestVec;
		double[] shortestVec;
		if (a.length < b.length) {
			longestVec = b;
			shortestVec = a;
		} else {
			longestVec = a;
			shortestVec = b;
		}
		double[] output = longestVec.clone();
		for (int i = 0; i < shortestVec.length; i++) {
			output[i] += shortestVec[i];
		}
		return output;
	}

	public static double[] scalarMultiply(double[] a, double scalar) {
		double[] output = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			output[i] = a[i] * scalar;
		}
		return output;
	}

	public static double[] matchVectorLength2d(double[] x, double[] reference) {
		double referenceLength = Math.sqrt(Math.pow(reference[0], 2) + Math.pow(reference[1], 2));
		double xLength = Math.sqrt(Math.pow(x[0], 2) + Math.pow(x[1], 2));

		return scalarMultiply(x, referenceLength/xLength);
	}

	/**
     * Sorts both arrays a and b highest to lowest, the second array is the scores with the matching index to the first array
     */
	public static void downwardsInsertionSort(int[] a, int[] b) {
	    for (int i = 1; i < b.length; i++) {
	        int keyA = a[i];
	        int keyB = b[i];
	        int j = i - 1;

	        while (j >= 0 && b[j] < keyB) {
	            a[j + 1] = a[j];
	            b[j + 1] = b[j];
	            j--;
	        }

	        a[j + 1] = keyA;
	        b[j + 1] = keyB;
	    }
	}

	public static void moveValueToEnd(int[] a, int index) {
		int targetToMove = a[index];
		for (int i = index; i < a.length - 1; i++) {
			a[i] = a[i + 1];
		}
		a[a.length - 1] = targetToMove;
	}
}
