package bitbot._testunit;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 *
 * @author zheng
 */
public class TestPriceScale {

    public static void main(String[] args) {
        String strPrice = convertFromScientificNotation(5540);
        int integerPlaces = strPrice.indexOf('.');
        int decimalPlaces = strPrice.length() - (integerPlaces == -1 ? 0 : integerPlaces) - 1;

        System.out.println(strPrice + " ________ " + decimalPlaces + " " + strPrice.indexOf('.') + " " + Math.pow(10, decimalPlaces));
    }

    static String convertFromScientificNotation(double number) {
        // Check if in scientific notation
        if (number > 1 && String.valueOf(number).toLowerCase().contains("e")) {
            System.out.println("The scientific notation number'"
                    + number
                    + "' detected, it will be converted to normal representation with 25 maximum fraction digits.");
            NumberFormat formatter = new DecimalFormat();
            formatter.setMaximumFractionDigits(25);
            return formatter.format(number);
        } else {
            return String.valueOf(number);
        }
    }
}
