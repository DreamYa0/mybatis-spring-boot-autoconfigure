package com.g7.framework.hikari.autoconfigure.util;

import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 变量名转换工具
 * @author dreamyao
 */
public final class CharMatcher {

    public static Manipulation none() {
        return Manipulation.NONE;
    }

    public static Manipulation separatedToCamel() {
        return Manipulation.SEPARATED_TO_CAMELCASE;
    }

    public static Manipulation camelToHyphen() {
        return Manipulation.CAMELCASE_TO_HYPHEN;
    }

    public enum Manipulation {

        /**
         * 不做任何转换
         */
        NONE {
            @Override
            public String apply(String value) {
                return value;
            }
        },

        /**
         * - 转 _
         */
        HYPHEN_TO_UNDERSCORE {
            @Override
            public String apply(String value) {
                return value.indexOf('-') != -1 ?
                        value.replace('-', '_') : value;
            }
        },

        /**
         * _ 转 .
         */
        UNDERSCORE_TO_PERIOD {
            @Override
            public String apply(String value) {
                return value.indexOf('_') != -1 ?
                        value.replace('_', '.') : value;
            }
        },

        /**
         * . 转 _
         */
        PERIOD_TO_UNDERSCORE {
            @Override
            public String apply(String value) {
                return value.indexOf('.') != -1 ?
                        value.replace('.', '_') : value;
            }
        },

        CAMELCASE_TO_UNDERSCORE {
            @Override
            public String apply(String value) {
                if (value.isEmpty()) {
                    return value;
                }
                Matcher matcher = CAMEL_CASE_PATTERN.matcher(value);
                if (!matcher.find()) {
                    return value;
                }
                matcher = matcher.reset();
                StringBuffer result = new StringBuffer();
                while (matcher.find()) {
                    matcher.appendReplacement(result, matcher.group(1) + '_' +
                            StringUtils.uncapitalize(matcher.group(2)));
                }
                matcher.appendTail(result);
                return result.toString();
            }
        },

        /**
         * 装驼峰命名转下划线、中划线命名
         */
        CAMELCASE_TO_HYPHEN {
            @Override
            public String apply(String value) {
                if (value.isEmpty()) {
                    return value;
                }
                Matcher matcher = CAMEL_CASE_PATTERN.matcher(value);
                if (!matcher.find()) {
                    return value;
                }
                matcher = matcher.reset();
                StringBuffer result = new StringBuffer();
                while (matcher.find()) {
                    matcher.appendReplacement(result, matcher.group(1) + '-'
                            + StringUtils.uncapitalize(matcher.group(2)));
                }
                matcher.appendTail(result);
                return result.toString();
            }
        },

        /**
         * 下划线、中划线命名装驼峰命名
         */
        SEPARATED_TO_CAMELCASE {
            @Override
            public String apply(String value) {
                return separatedToCamelCase(value, false);
            }
        },

        CASE_INSENSITIVE_SEPARATED_TO_CAMELCASE {
            @Override
            public String apply(String value) {
                return separatedToCamelCase(value, true);
            }
        };

        private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("([^A-Z-])([A-Z])");
        private static final Pattern SEPARATED_TO_CAMEL_CASE_PATTERN = Pattern.compile("[_\\-.]");
        private static final char[] SUFFIXES = new char[]{'_', '-', '.'};

        private static String separatedToCamelCase(String value, boolean caseInsensitive) {
            if (value.isEmpty()) {
                return value;
            }
            StringBuilder builder = new StringBuilder();
            for (String field : SEPARATED_TO_CAMEL_CASE_PATTERN.split(value)) {
                field = (caseInsensitive ? field.toLowerCase() : field);
                builder.append(builder.length() == 0 ? field : StringUtils.capitalize(field));
            }
            char lastChar = value.charAt(value.length() - 1);
            for (char suffix : SUFFIXES) {
                if (lastChar == suffix) {
                    builder.append(suffix);
                    break;
                }
            }
            return builder.toString();
        }

        public abstract String apply(String value);

    }

}