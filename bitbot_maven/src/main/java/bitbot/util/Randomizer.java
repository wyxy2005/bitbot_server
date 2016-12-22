package bitbot.util;

import java.util.Random;

/**
 *
 * @author z
 */
public class Randomizer {

    private final static Random rand;

    static {
	rand = new Random(System.currentTimeMillis() | (System.currentTimeMillis() / 20));
    }
    
    public static final int nextInt() {
	return rand.nextInt();
    }

    public static final int nextInt(final int arg0) {
	return rand.nextInt(arg0);
    }

    public static final void nextBytes(final byte[] bytes) {
	rand.nextBytes(bytes);
    }

    public static final boolean nextBoolean() {
	return rand.nextBoolean();
    }

    public static final double nextDouble() {
	return rand.nextDouble();
    }

    public static final float nextFloat() {
	return rand.nextFloat();
    }

    public static final long nextLong() {
	return rand.nextLong();
    }

    public static final int rand(final int lbound, final int ubound) {
	return (int) ((rand.nextDouble() * (ubound - lbound + 1)) + lbound);
    }
    
    public static final float randFloat(final float lbound, final float ubound) {
	return (float) ((rand.nextDouble() * (ubound - lbound + 1)) + lbound);
    }
}
