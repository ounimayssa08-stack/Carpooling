package com.carpooling.util;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtil {
    public static String formatTND(double amount) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.FRENCH);
        format.setMinimumFractionDigits(3);
        format.setMaximumFractionDigits(3);
        return format.format(amount) + " TND";
    }
}
