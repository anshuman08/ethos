package com.openbravo.pos.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;

/**
 *
 * @author marembo (marembo2008@gmail.com)
 * @since Dec 12, 2017, 9:50:25 AM
 */
public final class ReportNameUtil {

    private static final Logger LOG = Logger.getLogger(ReportNameUtil.class.getName());

    public static String formatDisplayName(final String productName, final String stockName) {
         LOG.log(Level.INFO, "(product, stockName) = ({0}, {1})", new Object[]{productName, stockName});
        if (stockName == null || stockName.trim().isEmpty()) {
            return productName;
        }

        return format("%s - (%s)", productName, stockName);
    }

}
