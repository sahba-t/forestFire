package GA;

import java.util.Random;

/**
 * Created by sahba on 3/25/17.
 * to experiment with different cross overs and mutation methods
 */
public class Test {
    private final static double MUT_PROB = 0.1;
    private static final Random random = new Random();
    private static final boolean doMutate = true;

    public static void main(String[] args) {
        double[] values = new double[10];
        for (int i = 0; i < values.length - 1; i++) {
            values[i] = random.nextDouble();
            values[i + 1] = random.nextDouble();
            uniformCross(values[i], values[i + 1]);
        }

    }

    private static double uniformCross(double d1, double d2) {
        double result;
        long first = Double.doubleToRawLongBits(d1);
        long second = Double.doubleToRawLongBits(d2);
        long mask = 51L;
        mask = -1L << mask;
        first &= mask;
        second &= (~mask);
        result = Double.longBitsToDouble(first | second);
        System.out.format("got %.10f and %.10f put out %.10f\t", d1, d2, result);
        if (doMutate) {
            result = mutate(result);
            System.out.format("mutated to %.10f", result);
        }
        System.out.println();
        return result;
    }

    private static double onPointCross(double d1, double d2) {
        long first = Double.doubleToRawLongBits(d1);
        long second = Double.doubleToRawLongBits(d2);
        long mask = 1L;
        long result = 0;
        for (int i = 0; i < 64; i++, mask <<= 1) {
            if (random.nextBoolean()) {
                result |= mask & first;
            } else {
                result |= mask & second;
            }
        }
        System.out.format("got %.10f and %.10f put out %.10f\n", d1, d2, Double.longBitsToDouble(result));
        return Double.longBitsToDouble(result);
    }

    private static double mutate(double d) {
        long number = Double.doubleToLongBits(d);
        long mask = 1;
        for (int i = 0; i < 52; i++) {
            if (random.nextDouble() < MUT_PROB) {
                number = number ^ mask;
            }
            mask <<= 1;
        }
        return Double.longBitsToDouble(number);
    }
}
