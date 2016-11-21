package com.centralconfig.model;

/**
 * An alias from one node in a document to a node in the same or another document.
 * Aliases help us support shared configuration.
 *
 * The source and target may be in the same document (same namespace path)
 * or different.
 *
 * Aliases are created by defining the value to be the prefix ** followed by a ypath
 *
 * Example:
 *      - rabbitMQ: **rabbitMQ/P2/master/endpoint
 */
public class Alias {

    private final YPath from;
    private final YPath to;


    public Alias(YPath from, YPath to) {
        this.from = from;
        this.to = to;
    }

    public YPath getFrom() {
        return from;
    }

    public YPath getTo() {
        return to;
    }
}
