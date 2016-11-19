package com.centralconfig.model;

import java.util.EnumSet;

/**
 * Created by sabarivasan on 11/16/16.
 */
public enum DocType {
    YAML,
    JSON,
    PROPERTIES;

    public static final EnumSet<DocType> YAML_JSON = EnumSet.of(YAML, JSON);
}
