package com.centralconfig.model;

/**
 * The data types that leaves of documents have
 */
public enum DataType {
    STRING('S'),
    DOUBLE('D'),
    INT('I'),
    BOOL('B'),
    ANCHOR('A');


    private final char serCode;

    DataType(char serCode) {
        this.serCode = serCode;
    }


    public char getSerCode() {
        return serCode;
    }

    public static DataType inferType(Object val) {
        if (val instanceof Double) {
            return DOUBLE;
        } else if (val instanceof Integer) {
            return INT;
        } else if (val instanceof Boolean) {
            return BOOL;
        } else if (val instanceof String) {
            if (((String) val).startsWith(Constants.ANCHOR_PREFIX)) {
                return ANCHOR;
            } else {
                return STRING;
            }
        }
        return null;
    }

    public static DataType fromSerCode(char serCode) {
        for (DataType dt: DataType.values()) {
            if (serCode == dt.serCode) {
                return dt;
            }
        }
        return null;
    }
}
