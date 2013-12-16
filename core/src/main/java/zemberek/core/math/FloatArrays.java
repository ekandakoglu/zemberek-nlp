package zemberek.core.math;

import java.util.Arrays;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

public class FloatArrays {
    public static final float[] ZERO_LENGTH_ARRAY = new float[0];

    // do not allow instantiation
    private FloatArrays() {
    }

    /**
     * @return true if difference is smaller or equal to range
     */
    public static boolean inRange(float d1, float d2, float range) {
        return Math.abs(d1 - d2) <= range;
    }

    /**
     * @param input float array
     * @return reverse of the float array
     */
    public static float[] reverse(float[] input) {
        float[] result = new float[input.length];
        for (int i = 0; i < input.length; i++) {
            result[input.length - i - 1] = input[i];
        }
        return result;
    }

    /**
     * @param input int array
     * @return float array converted from input int array
     */
    public static float[] convert(int[] input) {
        float[] data = new float[input.length];
        int k = 0;
        for (int i : input) {
            data[k++] = i;
        }
        return data;
    }

    /**
     * @param input 2d int array
     * @return 2d float array converted from 2d int array
     */
    public static float[][] convert(int[][] input) {
        float[][] data = new float[input.length][];
        int k = 0;
        for (int[] i : input) {
            data[k] = new float[i.length];
            int j = 0;
            for (int ii : i) {
                data[k][j++] = ii;
            }
            k++;
        }
        return data;
    }

    /**
     * @param input float array
     * @return float array converted from input float array
     */
    public static float[] convert(double[] input) {
        float[] data = new float[input.length];
        int k = 0;
        for (double i : input) {
            data[k++] = (float) i;
        }
        return data;
    }

    /**
     * @param d1    input float array
     * @param d2    input float array
     * @param range float input
     * @return true if the difference between elements of d1 and d2 is smaller than or equal to given range
     */
    public static boolean arrayEqualsInRange(float[] d1, float[] d2, float range) {
        validateArrays(d1, d2);
        for (int i = 0; i < d1.length; i++) {
            if (Math.abs(d1[i] - d2[i]) > range) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return true if two input float arrays are equal
     */
    public static boolean arrayEquals(float[] d1, float[] d2) {
        validateArrays(d1, d2);
        return Arrays.equals(d1, d2);
    }

    /**
     * @return the float array after appending zeros to its end with the given amount
     * @throws IllegalArgumentException when amount input is negative
     */
    public static float[] appendZeros(float[] darray, int zeroAmountToAppend) {
        if (zeroAmountToAppend < 0) {
            throw new IllegalArgumentException("Cannot append negative amount of zeros. Amount:" + zeroAmountToAppend);
        }
        return Arrays.copyOf(darray, darray.length + zeroAmountToAppend);
    }

    public static float[] normalize16bitLittleEndian(byte[] bytez) {
        return normalize16bitLittleEndian(bytez, bytez.length);
    }

    /**
     * @param bytez  input byte array
     * @param amount input, size of the byte array
     * @return float array inncluding the normalized float value of each byte elements as Little-Endian representation
     *         For 0xABCD:
     *         Big-Endian Rep.-->0xABCD
     *         Little-Endian Rep-->0xCDBA
     */
    public static float[] normalize16bitLittleEndian(byte[] bytez, int amount) {
        if ((amount & 1) != 0) {
            throw new IllegalArgumentException("Amount of bytes must be an order of 2. But it is: " + amount);
        }
        float[] result = new float[amount / 2];
        for (int i = 0; i < amount; i += 2) {
            final int val = (short) (bytez[i + 1] << 8) | (bytez[i] & 0xff);
            if (val >= 0) {
                result[i >>> 1] = (float) val / Short.MAX_VALUE;
            } else {
                result[i >>> 1] = -(float) val / Short.MIN_VALUE;
            }
        }
        return result;
    }

    /**
     * @param input input float array
     * @return byte array including the de-normalized 16-bit Big-Endian representations of float values in float array
     */
    public static byte[] denormalize16BitLittleEndian(float[] input) {
        byte[] result = new byte[input.length * 2];
        for (int i = 0; i < input.length; i++) {
            int denorm;
            if (input[i] < 0) {
                denorm = (int) (-input[i] * Short.MIN_VALUE);
            } else {
                denorm = (int) (input[i] * Short.MAX_VALUE);
            }
            result[i * 2] = (byte) (denorm & 0xff);
            result[i * 2 + 1] = (byte) (denorm >>> 8);
        }
        return result;
    }

    /**
     * @param input         input float array
     * @param bitsPerSample input as bit number
     * @return byte array including the de-normalized n-bit Big-Endian representations of float values in float array where n is bitsPerSample
     */
    static byte[] denormalizeLittleEndian(float[] input, int bitsPerSample) {
        int bytesPerSample = bitsPerSample % 8 == 0 ? bitsPerSample / 8 : bitsPerSample / 8 + 1;
        int maxVal = 1 << bitsPerSample - 1;
        byte[] result = new byte[input.length * bytesPerSample];
        for (int i = 0; i < input.length; i++) {
            int denorm;
            if (input[i] < 0) {
                denorm = (int) (-input[i] * maxVal);
            } else {
                denorm = (int) (input[i] * maxVal);
            }
            for (int j = 0; j < bytesPerSample; j++) {
                result[i * bytesPerSample + j] = (byte) ((denorm >>> j * 8) & 0xff);
            }
        }
        return result;
    }

    /**
     * gets a float array with values between -1.0 and 1.0 and converts it to
     * an integer in the range of [0,max]
     *
     * @param input float array
     * @param max   max integer value.
     * @return an integer array/
     */
    public static int[] toUnsignedInteger(float[] input, int max) {
        if (max < 1) {
            throw new IllegalArgumentException("Maximum int value must be positive. But it is:" + max);
        }
        int[] iarr = new int[input.length];
        float divider = (float) ((double)max / 2.0);
        for (int i = 0; i < input.length; i++) {
            float d = input[i];
            if (d < -1.0 || d > 1.0) {
                throw new IllegalArgumentException("Array value should be between -1.0 and 1.0. But it is: " + d);
            }
            iarr[i] = (int) (input[i] * divider);
        }
        return iarr;
    }

    /**
     * finds the maximum value of an array.
     *
     * @param input input array
     * @return maximum value.
     * @throws IllegalArgumentException if array is empty or null.
     */
    public static float max(float... input) {
        validateArray(input);
        float max = input[0];
        for (int i = 1; i < input.length; i++) {
            if (input[i] > max) {
                max = input[i];
            }
        }
        return max;
    }

    /**
     * finds the minimum value of an array.
     *
     * @param input input array
     * @return minimum value.
     * @throws IllegalArgumentException if array is empty or null.
     */
    public static float min(float... input) {
        validateArray(input);
        float min = input[0];
        for (int i = 1; i < input.length; i++) {
            if (input[i] < min) {
                min = input[i];
            }
        }
        return min;
    }

    /**
     * checks whether the input array is null or empty
     *
     * @param input input float array
     */
    public static void validateArray(float... input) {
        if (input == null) {
            throw new IllegalArgumentException("array is null!");
        } else if (input.length == 0) {
            throw new IllegalArgumentException("array is empty!");
        }
    }

    /**
     * @param input input array
     * @return index at which the maximum value of input is, minimum index is returned when multiple maximums
     */
    public static int maxIndex(float... input) {
        validateArray(input);
        float max = input[0];
        int index = 0;
        for (int i = 1; i < input.length; i++) {
            if (input[i] > max) {
                max = input[i];
                index = i;
            }
        }
        return index;
    }

    /**
     * @param input input array
     * @return index at which the minimum value element of input is, minimum index is returned when multiple minimums
     */
    public static int minIndex(float... input) {
        validateArray(input);
        float min = input[0];
        int minIndex = 0;
        for (int i = 1; i < input.length; i++) {
            if (input[i] < min) {
                min = input[i];
                minIndex = i;
            }
        }
        return minIndex;
    }

    /**
     * @param input input
     * @return sum of all elements in array
     */
    public static float sum(float... input) {
        float sum = 0;
        for (float v : input) {
            sum += v;
        }
        return sum;
    }

    /**
     * @param a1 input
     * @param a2 input
     * @return float array of which elements are the sum of 2 input arrays' elements
     */
    public static float[] sum(float[] a1, float[] a2) {
        validateArrays(a1, a2);
        float[] sum = new float[a1.length];
        for (int i = 0; i < a1.length; i++) {
            sum[i] = a1[i] + a2[i];
        }
        return sum;
    }

    /**
     * sums two float vector. result is written to first vector.
     *
     * @param first  first vector.
     * @param second second vector
     */
    public static void addToFirst(float[] first, float[] second) {
        validateArrays(first, second);
        for (int i = 0; i < first.length; i++) {
            first[i] = first[i] + second[i];
        }
    }

    /**
     * Adds a value to all elements of the [data] array.
     */
    public static void addToAll(float[] data, float valueToAdd) {
        validateArray(data);
        for (int i = 0; i < data.length; i++) {
            data[i] += valueToAdd;
        }
    }

    /**
     * sums two float vectors (second vector is scaled by scale factor).
     * result is written to first vector.
     *
     * @param first  first vector.
     * @param second second vector
     * @param scale scale factor for second
     */
    public static void addToFirstScaled(float[] first, float[] second, float scale) {
        validateArrays(first, second);
        for (int i = 0; i < first.length; i++) {
            first[i] = first[i] + second[i] * scale;
        }
    }

    /**
     * @param input input float array
     * @return an array containing square-values of the input array's elements
     */
    public static float[] square(float... input) {
        float[] res = new float[input.length];
        for (int i = 0; i < input.length; i++) {
            res[i] = input[i] * input[i];
        }
        return res;
    }

    public static void squareInPlace(float... input) {
        for (int i = 0; i < input.length; i++) {
            input[i] = input[i] * input[i];
        }
    }

    /**
     * substracts two float vector.
     *
     * @param a1 first vector.
     * @param a2 second vector
     * @return substraction result
     */
    public static float[] subtract(float[] a1, float[] a2) {
        validateArrays(a1, a2);
        float[] diff = new float[a1.length];
        for (int i = 0; i < a1.length; i++) {
            diff[i] = a1[i] - a2[i];
        }
        return diff;
    }

    /**
     * substracts two float vector. result is written to first vector.
     *
     * @param first  first vector.
     * @param second second vector
     */
    public static void subtractFromFirst(float[] first, float[] second) {
        validateArrays(first, second);
        for (int i = 0; i < first.length; i++) {
            first[i] = first[i] - second[i];
        }
    }

    /**
     * @param a1 input float array
     * @param a2 input float array
     * @return the array produced after multiplying the elements of input arrays
     */
    public static float[] multiply(float[] a1, float[] a2) {
        validateArrays(a1, a2);
        float[] mul = new float[a1.length];
        for (int i = 0; i < a1.length; i++) {
            mul[i] = a1[i] * a2[i];
        }
        return mul;
    }

    /**
     * @param a1 input float array
     * @param a2 input float array
     * @return the dot product value of elements in input arrays
     */

    public static float dotProduct(float[] a1, float[] a2) {
        return sum(multiply(a1, a2));
    }

    /**
     * multiplies two float vectors and result is written to the first vector.
     *
     * @param first  first vector
     * @param second second vector.
     */
    public static void multiplyToFirst(float[] first, float[] second) {
        validateArrays(first, second);
        for (int i = 0; i < first.length; i++) {
            first[i] = first[i] * second[i];
        }
    }

    /**
     * Multiplies all elements of a vector with a float number and returns a
     * new vector
     *
     * @param a1 vector
     * @param b  scale factor
     * @return new scaled vector
     */
    public static float[] scale(float[] a1, float b) {
        validateArray(a1);
        float[] mul = new float[a1.length];
        for (int i = 0; i < a1.length; i++) {
            mul[i] = a1[i] * b;
        }
        return mul;
    }

    /**
     * Multiplies all elements of a vector with a float number
     *
     * @param a1 vector
     * @param b  scale factor
     */
    public static void scaleInPlace(float[] a1, float b) {
        validateArray(a1);
        for (int i = 0; i < a1.length; i++) {
            a1[i] = a1[i] * b;
        }
    }

    /**
     * Calculates mean of a vector.
     *
     * @param input float array
     * @return mean
     */
    public static float mean(float... input) {
        validateArray(input);
        return sum(input) / input.length;
    }

    /**
     * for A=[a0, a1, ...,an]
     * for B=[b0, b1, ...,bn]
     * returns C=|a0-b0|+|a1-b1|+...+|an-bn|
     *
     * @param a input array a
     * @param b input array b
     * @return squared sum of array elements.
     */
    public static float absoluteSumOfDifferences(float[] a, float[] b) {
        return sum(absoluteDifference(a, b));
    }

    /**
     * for A=[a0, a1, ...,an]
     * for B=[b0, b1, ...,bn]
     * returns C=[|a0-b0|,|a1-b1|,...,|an-bn|]
     *
     * @param a input array a
     * @param b input array b
     * @return squared sum of array elements.
     */
    public static float[] absoluteDifference(float[] a, float[] b) {
        validateArrays(a, b);
        float[] diff = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            diff[i] += abs(a[i] - b[i]);
        }
        return diff;
    }


    /**
     * checks whether one of the input arrays are null or not, and whether their length is equal or not
     *
     * @param a1 input float array
     * @param a2 input float array
     */
    public static void validateArrays(float[] a1, float[] a2) {
        if (a1 == null) {
            throw new NullPointerException("first array is null!");
        }
        if (a2 == null) {
            throw new NullPointerException("second array is null!");
        }
        if (a1.length != a2.length) {
            throw new IllegalArgumentException("Array sizes must be equal. But, first:"
                    + a1.length + ", and second:" + a2.length);
        }
    }

    /**
     * for A=[a0, a1, ...,an] returns a0*a0+a1*a1+....+an*an
     *
     * @param array input array
     * @return squared sum of array elements.
     */
    public static float squaredSum(float[] array) {
        float result = 0;
        validateArray(array);
        for (float a : array) {
            result += a * a;
        }
        return result;
    }

    public static float squaredSumOfDifferences(float[] a, float[] b) {
        return (squaredSum(subtract(a, b)));
    }

    /**
     * @param input input float array
     * @return variance value of the elements in the input array
     */
    public static float variance(float[] input) {
        float sigmaSquare = 0;
        float mean = mean(input);
        for (float a : input) {
            final float meanDiff = a - mean;
            sigmaSquare += meanDiff * meanDiff;
        }
        return sigmaSquare / (input.length - 1);
    }

    /**
     * @param a input float array
     * @return standard deviation value of the elements in the input array
     */
    public static float standardDeviation(float[] a) {
        return (float) sqrt(variance(a));
    }

    /**
     * @param a input float array
     * @return true if array includes at least one Not-a-Number (NaN) value, false otherwise
     */
    public static boolean containsNaN(float[] a) {
        for (float v : a) {
            if (Double.isNaN(v))
                return true;
        }
        return false;
    }

    /**
     * replaces the elements smaller than minValue with the minValue
     *
     * @param var      input float array
     * @param minValue float
     */
    public static void floorInPlace(float[] var, float minValue) {
        for (int k = 0; k < var.length; k++) {
            if (var[k] < minValue)
                var[k] = minValue;
        }
    }

    /**
     * If a data point is non-zero and below 'floor' make it equal to floor
     *
     * @param data  the data to floor
     * @param floor the floored value
     */
    public static void nonZeroFloorInPlace(float[] data, float floor) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] != 0.0 && data[i] < floor) {
                data[i] = floor;
            }
        }
    }

    /**
     * Normalize the given data.
     *
     * @param data the data to normalize
     */
    public static void normalizeInPlace(float[] data) {
        float sum = sum(data);
        scaleInPlace(data, 1f / sum);
    }
    
}
