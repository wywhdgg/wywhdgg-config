package com.wywhdgg.dzb.core.util;

import com.wywhdgg.dzb.core.exception.ConfException;

/**
 * @author: dongzhb
 * @date: 2019/7/24
 * @Description:
 */
public class FieldReflectionUtil {
    private FieldReflectionUtil() {
    }

    public static Byte parseByte(String value) {
        try {
            value = value.replaceAll("　", "");
            return Byte.valueOf(value);
        } catch (NumberFormatException e) {
            throw new ConfException("parseByte but input illegal input=" + value, e);
        }
    }

    public static Boolean parseBoolean(String value) {
        value = value.replaceAll("　", "");
        if (Boolean.TRUE.toString().equalsIgnoreCase(value)) {
            return Boolean.TRUE;
        } else if (Boolean.FALSE.toString().equalsIgnoreCase(value)) {
            return Boolean.FALSE;
        } else {
            throw new ConfException("parseBoolean but input illegal input=" + value);
        }
    }

    public static Integer parseInt(String value) {
        try {
            value = value.replaceAll("　", "");
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new ConfException("parseInt but input illegal input=" + value, e);
        }
    }

    public static Short parseShort(String value) {
        try {
            value = value.replaceAll("　", "");
            return Short.valueOf(value);
        } catch (NumberFormatException e) {
            throw new ConfException("parseShort but input illegal input=" + value, e);
        }
    }

    public static Long parseLong(String value) {
        try {
            value = value.replaceAll("　", "");
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            throw new ConfException("parseLong but input illegal input=" + value, e);
        }
    }

    public static Float parseFloat(String value) {
        try {
            value = value.replaceAll("　", "");
            return Float.valueOf(value);
        } catch (NumberFormatException e) {
            throw new ConfException("parseFloat but input illegal input=" + value, e);
        }
    }

    public static Double parseDouble(String value) {
        try {
            value = value.replaceAll("　", "");
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            throw new ConfException("parseDouble but input illegal input=" + value, e);
        }
    }

    /**
     * 参数解析 （支持：Byte、Boolean、String、Short、Integer、Long、Float、Double、Date）
     */
    public static Object parseValue(Class<?> fieldType, String value) {
        // Class<?> fieldType = field.getType();	// Field field
        if (value == null || value.trim().length() == 0) {
            return null;
        }
        value = value.trim();

        if (String.class.equals(fieldType)) {
            return value;
        } else if (Boolean.class.equals(fieldType) || Boolean.TYPE.equals(fieldType)) {
            return parseBoolean(value);
        } /*else if (Byte.class.equals(fieldType) || Byte.TYPE.equals(fieldType)) {
			return parseByte(value);
		}  else if (Character.class.equals(fieldType) || Character.TYPE.equals(fieldType)) {
			 return value.toCharArray()[0];
		}*/ else if (Short.class.equals(fieldType) || Short.TYPE.equals(fieldType)) {
            return parseShort(value);
        } else if (Integer.class.equals(fieldType) || Integer.TYPE.equals(fieldType)) {
            return parseInt(value);
        } else if (Long.class.equals(fieldType) || Long.TYPE.equals(fieldType)) {
            return parseLong(value);
        } else if (Float.class.equals(fieldType) || Float.TYPE.equals(fieldType)) {
            return parseFloat(value);
        } else if (Double.class.equals(fieldType) || Double.TYPE.equals(fieldType)) {
            return parseDouble(value);
        } else {
            throw new RuntimeException("illeagal conf data type, type=" + fieldType);
        }
    }
}
