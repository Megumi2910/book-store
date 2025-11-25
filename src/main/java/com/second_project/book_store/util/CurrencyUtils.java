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
        
        // Formatter for whole numbers (VND doesn't use decimals)
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
     * VND doesn't use decimal places, so amounts are rounded to whole numbers.
     * 
     * @param amount Amount to format
     * @return Formatted string like "1,000,000"
     */
    public static String formatVNDAmount(BigDecimal amount) {
        if (amount == null) {
            return "0";
        }
        // Round to whole number (VND doesn't use decimals)
        long vndAmount = amount.longValue();
        return VND_FORMATTER.format(vndAmount);
    }

    /**
     * Format amount as VND (Vietnamese Dong) from a Double.
     * Convenience method for dashboard stats that use Double.
     * 
     * @param amount Amount to format (can be null)
     * @return Formatted string like "1,000,000 VND"
     */
    public static String formatVND(Double amount) {
        if (amount == null) {
            return "0 VND";
        }
        return formatVND(BigDecimal.valueOf(amount));
    }
}

