package com.barfl.treecutters.util;

public final class NumFmt {
    private NumFmt() {
    }

    public static String format(double value) {
        long rounded = Math.round(value);
        String digits = Long.toString(Math.abs(rounded));
        StringBuilder sb = new StringBuilder();
        int len = digits.length();
        for (int i = 0; i < len; i++) {
            if (i > 0 && (len - i) % 3 == 0) sb.append(',');
            sb.append(digits.charAt(i));
        }
        return sb.toString();
    }
}
