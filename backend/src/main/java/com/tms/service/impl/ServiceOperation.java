package com.tms.service.impl;

import java.util.function.Supplier;
import com.tms.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Translates persistence/casting failures without changing legacy endpoint error contracts. */
final class ServiceOperation {
    private static final Logger log = LoggerFactory.getLogger(ServiceOperation.class);
    private ServiceOperation() {}
    static <T> T schedule(Supplier<T> action) { return execute("message", "Server error", action); }
    static <T> T seat(String message, Supplier<T> action) { return execute("error", message, action); }
    private static <T> T execute(String key, String message, Supplier<T> action) {
        try { return action.get(); }
        catch (ApiException e) { throw e; }
        catch (RuntimeException e) {
            log.debug("API operation failed", e);
            throw new ApiException(500, key, message);
        }
    }
}
