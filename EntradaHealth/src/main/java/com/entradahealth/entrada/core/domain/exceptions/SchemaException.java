package com.entradahealth.entrada.core.domain.exceptions;

/**
 * TODO: Document this!
 *
 * @author edwards
 * @since 11/11/13
 */
public class SchemaException extends Exception {
    public final long rev;

    public SchemaException(long rev, String reason) {
        super(String.format("Error executing schema revision %d. %s", rev, reason));
        this.rev = rev;
    }

    public SchemaException(long rev, String reason, Exception inner) {
        super(String.format("Error executing schema revision %d. %s", rev, reason), inner);
        this.rev = rev;
    }
}
