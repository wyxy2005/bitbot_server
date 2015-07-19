package bitbot.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 *
 * @author zheng
 */
public class NumberUtil {

    public static String convertFromScientificNotation(double number) {
        // Check if in scientific notation
        if (number > 1 && String.valueOf(number).toLowerCase().contains("e")) {
            //  System.out.println("The scientific notation number'"
            //         + number
            //        + "' detected, it will be converted to normal representation with 25 maximum fraction digits.");
            NumberFormat formatter = new DecimalFormat();
            formatter.setMaximumFractionDigits(25);
            return formatter.format(number);
        }
        return String.valueOf(number);
    }
}
