package net.xorf.util;

import java.util.*;

public final class Literals {

    public static <T> List<T> nil() {
        return Literals.list();
    }
    
	public static <T> FluentList<T> list() {
		return new FluentList<T>();
	}

    public static <T> FluentList<T> list(T... items) {
        return new FluentList<T>(Arrays.asList(items));
    }

    public static <T> FluentList<T> listFrom(Iterable<T> from) {
        return new FluentList<T>(Views.asList(from));
    }

    //public static <T> FluentList<T> list(T[] arg1, T[]... items) {
    //    return new FluentList<T>(Arrays.asList(arg1));
    //}
    
    public static <T> Set<T> set() {
    	return new HashSet<T>();
    }

    public static <T> Set<T> set(T... items) {
        return new HashSet<T>(list(items));
    }
    
    public static <S, T> MapBuilder<S, T> map() {
    	return new MapBuilder<S, T>();
    }
    
    public static <S, T, SS extends MapBuilder<? super S, ? super T>> SS pmap(S key, T value) {
        return (SS)new MapBuilder<S, T>().map(key, value);
    }

    public static <S, T> MapBuilder<S, T> map(S key, T value) {
        return new MapBuilder<S, T>().map(key, value);
    }
    public static <S, T> MapBuilder<S, T> map(Map<S, T> source) {
        return new MapBuilder<S, T>(source);
    }

    public static <S, T> OrderedMapBuilder<S, T> orderedMap(S key, T value) {
    	return new OrderedMapBuilder<S, T>().map(key, value);
    }

    public static <S, T> OrderedMapBuilder<S, T> orderedMap() {
        return new OrderedMapBuilder<S, T>();
    }
    
    public static <T> IterableOver<T> iterableOver(T[] arr) {
        return new IterableOver<T>(arr);
    }

    public static class MapBuilder<S, T> extends HashMap<S, T> {
		private static final long serialVersionUID = 1L;
        private T defaultValue;

        public MapBuilder() {
            super();
        }
        
        public MapBuilder(Map<S, T> source) {
            super(source);
        }

        public MapBuilder<S, T> map(S key, T value) {
            put(key, value);
            return this;
        }

        public <SS extends MapBuilder<? super S, ? super T>> SS pmap(S key, T value) {
            put(key, value);
            return (SS)this;
        }

        public MapBuilder<S, T> mapAll(Map<S, T> entries) {
            putAll(entries);
            return this;
        }
        
        public List<Map.Entry<S, T>> asList() {
            return new ArrayList<Map.Entry<S, T>>(entrySet());
        }

        public MapBuilder<S, T> setDefault(T defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }
        
        /**
        * Returns the value to which the specified key is mapped,
        * or {@code null} if this map contains no mapping for the key
        *
        * UNLESS the default value is non-null, in which case the
        * default value will be returned.
        */
        @Override
        public T get(Object key) {
            T val = super.get(key);

            if (val == null && defaultValue != null)
                return defaultValue;

            return val;
        }
    }
    
    public static class OrderedMapBuilder<S, T> extends LinkedHashMap<S, T> {
    	private static final long serialVersionUID = 1L;
    	
        public OrderedMapBuilder<S, T> map(S key, T value) {
            put(key, value);
            return this;
        }
    }
    
    public static class FluentList<T> extends ArrayList<T> implements List<T> {
        private static final long serialVersionUID = 1L;

        public FluentList() {
            super();
        }
        
        public FluentList(Collection<T> c) {
            super(c);
        }
        
        public FluentList<T> append(T elem) {
            this.add(elem);
            return this;
        }

    }
    
    public static class IterableOver<T> implements Iterable<T> {
        private final T[] back;
        
        public IterableOver(T[] back) {
            this.back = back;
        }
        
        @Override
        public Iterator<T> iterator() {
            return this.new IteratorOver();
        }
        
        class IteratorOver implements Iterator<T> {
            private int pos = 0;
            
            @Override
            public boolean hasNext() {
                return pos < back.length;
            }

            @Override
            public T next() {
                return back[pos++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
            
        }
    }
}
