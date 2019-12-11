/*
 * Copyright (c) 2008-2019 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.cuba.core.global;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public class LocaleResolver {

    public static Locale resolve(String localeString) {
        Locale result;
        if (localeString.contains("-")) {
            result = Locale.forLanguageTag(localeString);
        } else {
            result = LocaleUtils.toLocale(localeString);
        }
        return result;
    }

    public static String localeToString(Locale locale) {
        if (locale == null) {
            return null;
        }
        Locale strippedLocale = locale.stripExtensions();
        return StringUtils.isEmpty(strippedLocale.getScript()) ?
                strippedLocale.toString() : strippedLocale.toLanguageTag();
    }
}
