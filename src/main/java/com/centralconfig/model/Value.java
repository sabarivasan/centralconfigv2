package com.centralconfig.model;

import com.centralconfig.persist.DbSerializable;

/**
 * Created by sabarivasan on 11/16/16.
 */
public class Value implements DbSerializable<Value> {

    private final Object value;
    private final DataType dataType;

    public Value(String ser) {
        dataType = DataType.fromSerCode(ser.charAt(0));
        String val = ser.substring(2);
        switch (dataType) {
            case DOUBLE: value = Double.parseDouble(val); break;
            case INT: value = Integer.parseInt(val); break;
            case BOOL: value = Boolean.parseBoolean(val); break;
            default: value = val; break;
        }
    }

    public Value(Object value, DataType dataType) {
        this.value = value;
        this.dataType = dataType;
    }

    public Object getValue() {
        return value;
    }

    public DataType getDataType() {
        return dataType;
    }

    @Override
    public String ser() {
        return dataType.getSerCode() + DbSerializable.SER_DATA_TYPE_SEPARATOR + value.toString();
    }

    @Override
    public void serTo(StringBuilder sb) {
        sb.append(dataType.getSerCode()).append(DbSerializable.SER_DATA_TYPE_SEPARATOR).append(value.toString());
    }

    @Override
    public Value deser(String ser) {
        return new Value(ser);
    }
}
