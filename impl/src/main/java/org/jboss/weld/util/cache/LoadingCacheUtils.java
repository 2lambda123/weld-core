/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.util.cache;

import java.util.concurrent.ExecutionException;

import org.jboss.weld.logging.UtilLogger;

import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ExecutionError;
import com.google.common.util.concurrent.UncheckedExecutionException;

/**
 * Guava loading cache utilities.
 *
 * @author Martin Kouba
 * @deprecated Remove once WELD-1756 is resolved
 */
public final class LoadingCacheUtils {

    private LoadingCacheUtils() {
    }

    /**
     * Get the cache value for the given key.
     *
     * @param cache
     * @param key
     * @param <K> Key type
     * @param <V> Value type
     * @return the cache value
     * @throws ExecutionError if an error is thrown while loading the value
     */
    public static <K, V> V getCacheValue(LoadingCache<K, V> cache, K key) {
        try {
            return cache.get(key);
        } catch (ExecutionException e) {
            throw UtilLogger.LOG.unableToLoadCacheValue(key, e.getCause());
        } catch (UncheckedExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw UtilLogger.LOG.unableToLoadCacheValue(key, cause);
        }
    }

    /**
     * Get and cast the cache value for the given key.
     *
     * @param cache
     * @param key
     * @param <T> Required type
     * @param <K> Key type
     * @param <V> Value type
     * @return the cache value cast to the required type
     * @throws ExecutionError if an error is thrown while loading the value
     */
    @SuppressWarnings("unchecked")
    public static <T, K, V> T getCastCacheValue(LoadingCache<K, V> cache, Object key) {
        return (T) getCacheValue(cache, (K) key);
    }

}
