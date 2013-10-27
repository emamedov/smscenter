package org.eminmamedov.smscenter.common;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Class provides methods to work with data from {@link DBValue} annotation.
 * 
 * @author Emin Mamedov
 * 
 */
public final class DbValueUtils {

    private static final Logger log = Logger.getLogger(DbValueUtils.class);

    private DbValueUtils() {

    }

    /**
     * Return database value of specified Enum value. Returns string value if it
     * is not blank or int value otherwise
     * 
     * @param enumValue
     *            enum value which database value should be returned
     * @return database value
     * @throws IllegalArgumentException
     *             if no DBValue annotation has been found for this enum value
     */
    public static Object getDbValue(Enum<?> enumValue) {
        if (enumValue == null) {
            return null;
        }
        DBValue dbValue;
        try {
            dbValue = (DBValue) enumValue.getClass().getField(enumValue.name()).getAnnotation(DBValue.class);
            if (dbValue == null) {
                throw new IllegalArgumentException("Enum value [" + enumValue + "] is not annotated with DBValue");
            }
            String stringValue = dbValue.stringValue();
            if (StringUtils.isNotBlank(stringValue)) {
                return stringValue;
            }
            return dbValue.intValue();
        } catch (NoSuchFieldException e) {
            log.warn(e, e);
        }
        return null;
    }

    /**
     * Returns enum value of specified enum class that has specified database
     * value.
     * 
     * @param enumClass
     *            enum class that should be scanned
     * @param dbValue
     *            database value that is looked for
     * @return found enum value
     */
    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> E getEnumValueByDbValue(Class<E> enumClass, Object dbValue) {
        if (dbValue == null) {
            return null;
        }
        for (Enum<E> enumValue : enumClass.getEnumConstants()) {
            if (dbValue.toString().equals(getDbValue(enumValue).toString())) {
                return (E) enumValue;
            }
        }
        return null;
    }

}
