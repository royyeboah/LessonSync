package com.adbdti.lessonsync.Auth;

import com.google.api.client.util.IOUtils;
import com.google.api.client.util.store.AbstractDataStore;
import com.google.api.client.util.store.AbstractDataStoreFactory;
import com.google.api.client.util.store.DataStore;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stores OAuth credentials in Redis instead of on the local filesystem.
 *
 * <p>The Google client library uses this to persist each user's refresh token and to write back
 * access tokens whenever it silently refreshes them, so tokens survive restarts without anyone
 * pasting them into the code.
 */
public class RedisDataStoreFactory extends AbstractDataStoreFactory {

    private final StringRedisTemplate redisTemplate;
    private final String keyPrefix;

    public RedisDataStoreFactory(StringRedisTemplate redisTemplate, String keyPrefix) {
        this.redisTemplate = redisTemplate;
        this.keyPrefix = keyPrefix;
    }

    @Override
    protected <V extends Serializable> DataStore<V> createDataStore(String id) {
        return new RedisDataStore<>(this, id);
    }

    private static class RedisDataStore<V extends Serializable> extends AbstractDataStore<V> {

        private final StringRedisTemplate redisTemplate;
        private final String redisKey;

        RedisDataStore(RedisDataStoreFactory factory, String id) {
            super(factory, id);
            this.redisTemplate = factory.redisTemplate;
            this.redisKey = factory.keyPrefix + ":" + id;
        }

        @Override
        public Set<String> keySet() {
            return new HashSet<>(redisTemplate.<String, String>opsForHash().keys(redisKey));
        }

        @Override
        public Collection<V> values() throws IOException {
            Map<String, String> entries = redisTemplate.<String, String>opsForHash().entries(redisKey);
            List<V> values = new ArrayList<>(entries.size());
            for (String encoded : entries.values()) {
                values.add(decode(encoded));
            }
            return Collections.unmodifiableList(values);
        }

        @Override
        public V get(String key) throws IOException {
            if (key == null) {
                return null;
            }
            String encoded = redisTemplate.<String, String>opsForHash().get(redisKey, key);
            return encoded == null ? null : decode(encoded);
        }

        @Override
        public DataStore<V> set(String key, V value) throws IOException {
            redisTemplate.opsForHash().put(redisKey, key, encode(value));
            return this;
        }

        @Override
        public DataStore<V> clear() {
            redisTemplate.delete(redisKey);
            return this;
        }

        @Override
        public DataStore<V> delete(String key) {
            if (key != null) {
                redisTemplate.opsForHash().delete(redisKey, key);
            }
            return this;
        }

        private String encode(V value) throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            IOUtils.serialize(value, out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        }

        private V decode(String encoded) throws IOException {
            byte[] bytes = Base64.getDecoder().decode(encoded);
            return IOUtils.deserialize(new ByteArrayInputStream(bytes));
        }
    }
}
