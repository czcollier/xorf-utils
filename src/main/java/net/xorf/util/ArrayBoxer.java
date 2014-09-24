package net.xorf.util;

/**
 * Makes boxed-type versions of arrays for all primitive type arrays
 */
public class ArrayBoxer {

    //static-only utilities
    private ArrayBoxer() { }

    public static Byte[] box(byte[] prims) {
        Byte[] ret = new Byte[prims.length];
        for (int i = 0; i < prims.length; i++)
            ret[i] = prims[i];
        return ret;
    }

    public static Short[] box(short[] prims) {
        Short[] ret = new Short[prims.length];
        for (int i = 0; i < prims.length; i++)
            ret[i] = prims[i];
        return ret;
    }

    public static Integer[] box(int[] prims) {
        Integer[] ret = new Integer[prims.length];
        for (int i = 0; i < prims.length; i++)
            ret[i] = prims[i];
        return ret;
    }

    public static Long[] box(long[] prims) {
        Long[] ret = new Long[prims.length];
        for (int i = 0; i < prims.length; i++)
            ret[i] = prims[i];
        return ret;
    }

    public static Float[] box(float[] prims) {
        Float[] ret = new Float[prims.length];
        for (int i = 0; i < prims.length; i++)
            ret[i] = prims[i];
        return ret;
    }

    public static Double[] box(double[] prims) {
        Double[] ret = new Double[prims.length];
        for (int i = 0; i < prims.length; i++)
            ret[i] = prims[i];
        return ret;
    }

    public static Boolean[] box(boolean[] prims) {
        Boolean[] ret = new Boolean[prims.length];
        for (int i = 0; i < prims.length; i++)
            ret[i] = prims[i];
        return ret;
    }

    public static Character[] box(char[] prims) {
        Character[] ret = new Character[prims.length];
        for (int i = 0; i < prims.length; i++)
            ret[i] = prims[i];
        return ret;
    }
}

