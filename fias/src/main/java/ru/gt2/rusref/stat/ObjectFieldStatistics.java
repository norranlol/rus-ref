package ru.gt2.rusref.stat;

import lombok.Getter;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Date;

/**
 * Статистика по полю.
 */
public class ObjectFieldStatistics<T> {
    private final Field field;

    @Getter
    private final String fieldName;

    protected int nullCount;

    protected int notNullCount;

    protected int notValidCount;
    
    public final void updateStatistics(Object obj, boolean violated) {
        if (violated) {
            notValidCount++;
        }

        try {
            T value = (T) field.get(obj);
            doUpdateStatistics(value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to get access to field", e);
        }
    }
    
    public static ObjectFieldStatistics<?> newFieldStatistics(Field field) {
        Class<?> type = field.getType();
        if (Integer.class.equals(type)) {
            return new IntegerFieldStatistics(field);
        } else if (String.class.equals(type)) {
            return new StringFieldStatistics(field);
        } else if (Date.class.equals(type)) {
            return new DateFieldStatistics(field);
        }
        return new ObjectFieldStatistics<Object>(field);
    }

    public void print(PrintStream printStream) {
        printStream.print("notNullCount = " + notNullCount + ", nullCount = " + nullCount);
        if (notValidCount > 0) {
            printStream.print(", notValidCount = " + notValidCount);
        }
    }
    
    protected void doUpdateStatistics(T value) {
        if (null == value) {
            nullCount++;
        } else {
            notNullCount++;
        }
    }

    protected ObjectFieldStatistics(Field field) {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        this.field = field;
        fieldName = field.getName();
    }

    protected BigDecimal getAverage(BigInteger sum) {
        return new BigDecimal(sum).divide(BigDecimal.valueOf(notNullCount), 2, RoundingMode.HALF_UP);
    }
}