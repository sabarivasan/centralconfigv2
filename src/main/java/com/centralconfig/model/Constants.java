package com.centralconfig.model;

/**
 * Constants
 */
public final class Constants {

    // The prefix for aliases in a document
    // Aliases are references from nodes in one document to another part of the same document or a different document.
    // Aliases bring in the entire sub-tree referenced
    public static final String ALIAS_PREFIX = "**";

    public static final int STRING_BUILDER_INIT_LENGTH_8 = 8;
    public static final int STRING_BUILDER_INIT_LENGTH_16 = 16;
    public static final int STRING_BUILDER_INIT_LENGTH_32 = 32;
    public static final int STRING_BUILDER_INIT_LENGTH_64 = 64;
    public static final int STRING_BUILDER_INIT_LENGTH_128 = 128;
    public static final int STRING_BUILDER_INIT_LENGTH_512 = 512;
    public static final int LENGTH_1024 = 1024;

    private Constants() { }


}
