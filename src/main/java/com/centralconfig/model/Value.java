package com.centralconfig.model;

import com.centralconfig.persist.DbSerializable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by sabarivasan on 11/16/16.
 * //TODO:can we make this class immutable and still compatible with Jackson serialization?
 */
public class Value implements DbSerializable<Value> {

    @JsonProperty
    private Object value;

    @JsonProperty
    private DataType dataType;

    public Value() { }

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

    public boolean isAlias() {
        return dataType.isAlias();
    }

    @JsonIgnore
    public String getAliasDestination() {
        if (!isAlias()) {
            throw new InternalError("This is not an alias");
        }
        return ((String) value).substring(Constants.ALIAS_PREFIX.length());
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Value value1 = (Value) o;

        return getValue() != null ? getValue().equals(value1.getValue()) : value1.getValue() == null;
    }

    @Override
    public int hashCode() {
        return getValue() != null ? getValue().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "" + value + " (" + dataType + ")";
    }
}

