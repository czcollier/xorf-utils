package net.xorf.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MultiValueMap<K, V> extends HashMap<K, Set<V>> {
    private static final long serialVersionUID = 1L;

    /** @param key unused @param value unused */
    @Override
    public Set<V> put(K key, Set<V> value) {
        for (V v : value)
            puts(key, v);
        return get(key);
    }
    
    public V puts(K key, V value) {
        Set<V> s;
        if (!containsKey(key)) {
            s = new HashSet<V>();
            super.put(key, s);
        }
        else s = get(key);
        s.add(value);
        return value;
    }
}
