package com.second_project.book_store.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Utility class for currency formatting.
 * Formats amounts in Vietnamese Dong (VND).
 */
public class CurrencyUtils {

    private static final DecimalFormat VND_FORMATTER;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(',');
        VND_FORMATTER = new DecimalFormat("#,###", symbols);
    }

    /**
     * Format amount as VND (Vietnamese Dong).
     * VND doesn't use decimal places, so amounts are rounded to whole numbers.
     * 
     * @param amount Amount to format
     * @return Formatted string like "1,000,000 VND"
     */
    public static String formatVND(BigDecimal amount) {
        if (amount == null) {
            return "0 VND";
        }
        // Round to whole number (VND doesn't use decimals)
        long vndAmount = amount.longValue();
        return VND_FORMATTER.format(vndAmount) + " VND";
    }

    /**
     * Format amount as VND without the "VND" suffix.
     * 
     * @param amount Amount to format
     * @return Formatted string like "1,000,000"
     */
    public static String formatVNDAmount(BigDecimal amount) {
        if (amount == null) {
            return "0";
        }
        long vndAmount = amount.longValue();
        return VND_FORMATTER.format(vndAmount);
    }
}

