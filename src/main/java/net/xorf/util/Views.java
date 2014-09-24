package net.xorf.util;

import net.xorf.util.Tuples.Tuple;
import net.xorf.util.Tuples.Tuple2;

import java.lang.reflect.Array;
import java.util.*;

/**
 * A collection of utilities for simplifying code that manipulates
 * <tt>java.lang.Iterable</tt>s and for treating them in a manner
 * inspired by functional programming language idioms.
 * 
 * @author ccollier
 *
 */
public class Views {
    /** functor types **/

    public interface VFunc0 { void call(); }
    public interface VFunc1<TP1> { void call(TP1 arg); }
    public interface VFunc2<TP1, TP2> { void call(TP1 arg1, TP2 arg2); }

    public interface Func0<TRet> { TRet call(); }
	public interface Func1<TP1, TRet> { TRet call(TP1 arg); }
	public interface Func2<TP1, TP2, TRet> { TRet call(TP1 arg1, TP2 arg2); }
    public interface Func3<TP1, TP2, TP3, TRet> { TRet call(TP1 arg1, TP2 arg2, TP3 arg3); }

	public interface Aggregate<TAgg, TElem> extends VFunc1<TElem> { TAgg value(); }
	public interface Predicate<T> extends Func1<T, Boolean> { }

	//alias for UnaryPredicate - clearer base class name for
	//code clarity when creating custom filters.
	public interface Filter<T> extends Predicate<T> { }
	
	//alias for UnaryFunctor - clearer base class name for code clarity
	public interface Mapper<TP1, TRet> extends Func1<TP1, TRet> { }
    
	//convenience for {@link #toMap(Iterable<TSource>, UnaryFunctor<TSource, Tuple2<TDestKey, TDestVal>>)}
	//to keep class declarations and method signatures sane
	public interface ToMapMapper<TSource, TKey, TValue> extends Mapper<TSource, Tuple2<TKey, TValue>> { }

	public interface DynaFunctor<TArgs extends Tuple, TRet> { TRet call(TArgs args); }
	
	/**
	 * This class is to be used externally only in a static manner.
	 * May not be instantiated.
	 */
	private Views() { }
		
	/** static methods for transforming/filtering/aggregating iterables **/ 
    
    public static <T> View<T> viewOf(Iterable<T> src) {
        return new Passthrough<T>(src);
    }

    public static <T> View<T> viewOf(T[] arr) {
        return new ArrayView<T>(arr);
    }

    public static View<Byte> viewOf(byte[] arr) {
        return new ArrayView<Byte>(ArrayBoxer.box(arr));
    }

    public static View<Short> viewOf(short[] arr) {
        return new ArrayView<Short>(ArrayBoxer.box(arr));
    }

    public static View<Integer> viewOf(int[] arr) {
        return new ArrayView<Integer>(ArrayBoxer.box(arr));
    }

    public static View<Long> viewOf(long[] arr) {
        return new ArrayView<Long>(ArrayBoxer.box(arr));
    }

    public static View<Float> viewOf(float[] arr) {
        return new ArrayView<Float>(ArrayBoxer.box(arr));
    }

    public static View<Double> viewOf(double[] arr) {
        return new ArrayView<Double>(ArrayBoxer.box(arr));
    }

    public static View<Boolean> viewOf(boolean[] arr) {
        return new ArrayView<Boolean>(ArrayBoxer.box(arr));
    }

    public static View<Character> viewOf(char[] arr) {
        return new ArrayView<Character>(ArrayBoxer.box(arr));
    }

    public static <T> View<T> readOnlyViewOf(Iterable<T> src) {
        return new ReadOnlyPassthrough<T>(src);
    }

    static class NoOp<T> implements Func1<T, T> {
        @Override public T call(T arg) { return arg; }
    }

    public static <V> View<V> fromString(String str, String delim, Func1<String, V> builder) {
        return Views.map(Arrays.asList(str.split(delim)), builder);
    }
    
    public static View<String> fromString(String str, String delim) {
        return fromString(str, delim, new NoOp<String>());
    }
    
    public static <V> View<V> fromString(String str, Func1<String, V> builder) {
        return fromString(str, "[,\\|]", builder);
    }
    
    public static View<String> fromString(String str) {
        return fromString(str, new NoOp<String>());
    }

    public static <T extends Comparable<T>> OrderableView<T> orderableViewOf(Iterable<T> src) {
        return new OrderableView<T>(src);
    }
    
	public static <T> T first(Iterable<T> i) {
		return i.iterator().next();
	}

	public static <T, TComp>
    boolean contains(Iterable<T> itr, TComp comp, Equality<T, TComp> equality) {
	    for (T e : itr) {
	        if (equality.call(e, comp))
	            return true;
	    }
	    return false;
	}

    public static <T> boolean contains(Iterable<T> itr, T comp) {
        return contains(itr, comp, new DefaultEquality<T>());
    }

    public static <T> boolean contains(Iterable<T> itr, Predicate<T> pred) {
        for (T e : itr) {
            if (pred.call(e))
                return true;
        }
        return false;
    }


    public interface Equality<T1, T2> extends Func2<T1, T2, Boolean> { }

    public static abstract class NullSafeEquality<T1, T2> implements Equality<T1, T2> {
        @Override public Boolean call(T1 arg1, T2 arg2) {
            return arg1 == null && arg2 == null
                || (arg1 == null
                    ? false
                    : nullSafeEquals(arg1, arg2));
        }
        
        public abstract Boolean nullSafeEquals(T1 arg1, T2 arg2);
    }

    public static class DefaultEquality<T> extends NullSafeEquality<T, T> {
        @Override public Boolean nullSafeEquals(T arg1, T arg2) {
            return arg1.equals(arg2);
        }
    }
    
	public static <T> View<T> first(Iterable<T> itr, int limit) {
	    return new Limit<T>(itr, limit);
	}

    public static <T> View<T> from(Iterable<T> itr, int limit) {
        return new TailLimit<T>(itr, limit);
    }

    public static <T> View<T> slice(Iterable<T> itr, int start, int end) {
        return first(from(itr, start), end);
    }
    
    public static <T> View<T>  expand(Iterable<T> itr, int size, T sub) {
        return new Expand<T>(itr, size, sub);
    }  

	public static <T> View<T>
			filter(Iterable<T> itr, Filter<T> filter, VFunc1<T> shunt) {
		return new FilteredView<T>(itr, filter, shunt);
	}

    public static <T> View<T>
    filter(Iterable<T> itr, Filter<T> filter) {
        return new FilteredView<T>(itr, filter, null);
    }

	public static <T> View<T> unique(Iterable<T> src) {
	    return new UniqueView<T>(src);
	}
    
    public static <T> View<T> union(Iterable<T> first, Iterable<T> next) {
        return concatenate(first, next).unique();
    }
	
	public static <T> void foreach(Iterable<T> src, VFunc1<T> func) {
	    for (T elem : src) func.call(elem);
	}
	
	public static <TDest, TSource> View<TDest>
			map(Iterable<TSource> source, Func1<TSource, TDest> mapFunc) {
		return new Transform<TSource, TDest>(source, mapFunc);
	}

    public static <TDestKey, TDestVal, TSource> Map<TDestKey, TDestVal>
        asMap(Iterable<TSource> source, ToMapMapper<TSource, TDestKey, TDestVal> mapFunc, MapOrder mapOrder) {
        final Map<TDestKey, TDestVal> map = mapOrder == MapOrder.PRESERVED
                ? new LinkedHashMap<TDestKey, TDestVal>()
                : new HashMap<TDestKey, TDestVal>();

        return _buildMap(map, source, mapFunc);
    }

    public static <TDestKey, TDestVal, TSource> Map<TDestKey, TDestVal>
    asMap(Iterable<TSource> source, ToMapMapper<TSource, TDestKey, TDestVal> mapFunc) {
        return asMap(source, mapFunc, MapOrder.UNDEFINED);
    }

    public static <TKey, TVal> Map<TKey, TVal> asMap(Iterable<Tuple2<TKey, TVal>> source, MapOrder mapOrder) {
        return asMap(source, new ToMapMapper<Tuple2<TKey, TVal>, TKey, TVal>() {
            @Override public Tuple2<TKey, TVal> call(Tuple2<TKey, TVal> arg) { return arg; }
        }, MapOrder.UNDEFINED);
    }

    public static <TKey, TVal> Map<TKey, TVal> asMap(Iterable<Tuple2<TKey, TVal>> source) {
        return asMap(source, MapOrder.UNDEFINED);
    }

    public enum MapOrder {
        PRESERVED,
        UNDEFINED
    }

	private static <TDestKey, TDestVal, TSource> Map<TDestKey, TDestVal>
	        _buildMap(
                Map<TDestKey, TDestVal> retMap,
                Iterable<TSource> source, ToMapMapper<TSource, TDestKey, TDestVal> mapFunc) {
	    Transform<TSource, Tuple2<TDestKey, TDestVal>> t =
	            new Transform<TSource, Tuple2<TDestKey, TDestVal>>(source, mapFunc);

	    for (Tuple2<TDestKey, TDestVal> e : t)
	        retMap.put(e._1, e._2);
	            
	    return retMap;
	}

    public static <TVal, TKey> Map<TKey, Set<TVal>>
    groupBy(Iterable<TVal> source, Func1<TVal, TKey> mapFunc) {

        MultiValueMap<TKey, TVal> m = new MultiValueMap<TKey, TVal>();

        for (TVal v : source)
            m.puts(mapFunc.call(v), v);

        return m;
    }

	public static <T> View<T> concatenate(Iterable<T> first, Iterable<T> next) {
	    return new Concatenation<T>(first, next);
	}
	
	public static <T> View<String>
			toStrings(Iterable<T> itr) {
		return map(itr, new Mapper<T, String>() {
			@Override public String call(T source) { return String.valueOf(source); }
		});
	}

	public static <T> String
			toString(Iterable<T> itr, String start, String sep, String end) {
		return aggregate(itr, new StringMaker<T>(start, sep, end));
	}
	
	public static <T> String
	        toString(Iterable<T> itr) {
	    return aggregate(itr, new StringMaker<T>("[", ",", "]"));
	}
	
	public static <T> String stringFormat(Iterable<T> itr, String fmt, String elemFmt) {
	    return aggregate(itr, new FormattedStringMaker<T>(fmt, elemFmt));
	}
	
	public static <T> String delimit(Iterable<T> itr, String delimiter) {
		return toString(itr, "", delimiter, "");
	}
    
    public static <T> List<T> asList(Iterable<T> itr) {
    	return new ListBuilder<T>(itr).list();
    }
    
    public static <T> Set<T> asSet(Iterable<T> itr){
        return new SetBuilder<T>(itr).set();
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(Iterable<T> itr) {
        return (T[]) Views.asList(itr).toArray();
    }
    
    @SuppressWarnings("unchecked")
	public static <T> T[] toArray(Class<T> elemType, Iterable<T> itr) {
    	return (T[]) Views.asList(itr).toArray(
    	        (T[])Array.newInstance(elemType, 0));
    }

    /**
     * Returns the size of the <tt>Iterable</tt>.
     * @deprecated Uuse <tt>count</tt> instead
     */
    @Deprecated
    public static <T> int length(Iterable<T> itr) {
        return Views.aggregate(itr, new Aggregate<Integer, T>() {
            int cnt = 0;

            /** @param arg deliberately not used  */
            @Override
            public void call(T arg) {
                ++cnt;
            }

            @Override
            public Integer value() {
                return cnt;
            }
        });
    }

    /**
     * Replacement for <tt>length</tt> call.  Want to rename the
     * call - the word <tt>count</tt> is a better representation
     * of the meaning of the call.
     */
    public static <T> int count(Iterable<T> itr) {
        return length(itr);
    }
    
    public static <T> T get(Iterable<T> itr, int index) {
        Iterator<T> i = itr.iterator();
        int idx = 0;
        while (true) {
           if (! i.hasNext()) 
               break;
            if (idx == index)
                return i.next();
            idx++; i.next();
        }
        throw new IndexOutOfBoundsException(String.valueOf(idx));
    }
    
    public static <T, TOrder extends Comparable<TOrder>> View<T>
    		orderBy(Iterable<T> itr, final Func1<T, TOrder> func) {
    	Comparator<T> comp = new Comparator<T>() {
			@Override
			public int compare(T left, T right) {
				TOrder odLeft = func.call(left);
				TOrder odRight = func.call(right);
				return odLeft.compareTo(odRight);
			}
    	};
    	
    	List<T> sortList = asList(itr);
    	Collections.sort(sortList, comp);
    	return new Passthrough<T>(sortList);
    }
    
    public static <T extends Comparable<T>> OrderableView<T> naturalOrder(final Iterable<T> in) {
        //final Func1<T, T> identComp = identityComparator();
        List<T> sortList = asList(in);
        Collections.sort(sortList);
        return new OrderableView<T>(sortList);
    }
    
    public static <T extends Comparable<T>> T max(Iterable<T> src) {
        return Views.aggregate(src, new Views.Func2<T, T, T>() {
            @Override
            public T call(T arg1, T arg2) {
                return arg1.compareTo(arg2) > 0 ? arg1 : arg2;
            }
        });
    }

    public static <T extends Comparable<T>> T min(Iterable<T> src) {
        return Views.aggregate(src, new Views.Func2<T, T, T>() {
            @Override
            public T call(T arg1, T arg2) {
                return arg1.compareTo(arg2) < 0 ? arg1 : arg2;
            }
        });
    }
    
    public static <T, TAgg> TAgg
			aggregate(Iterable<T> src, TAgg initial, Func2<TAgg, T, TAgg> func) {
		TAgg aggregate = initial;
		
		for (T val : src)
			aggregate = func.call(aggregate, val);

		return aggregate;
	}

	public static <T> T
			aggregate(Iterable<T> src, Func2<T, T, T> func) {
		Iterator<T> itr = src.iterator();
		if (!itr.hasNext())
			return null;
		
		T aggregate = itr.next();
		
		while(itr.hasNext())
			aggregate = func.call(aggregate, itr.next());

		return aggregate;
	}
	
	public static <TAgg, TElem> TAgg
			aggregate(Iterable<TElem> src, Aggregate<TAgg, TElem> func) {
		for (TElem t : src) func.call(t);
		return func.value();
	}
	
	public static <T> boolean isEmpty(Iterable<T> ibl) {
        return ibl == null || !ibl.iterator().hasNext();
    }

	/** 
	 * base View class - returned from transform operations and contains
	 * corresponding operations so transforms can be chained using methods
	 * on the Iterables returned from previous transforms.
	 *  
	 * @author ccollier
	 *
	 * @param <T>
	 */
    public static abstract class View<T> implements Iterable<T> {
        
        private List<Class<?>> getGenericTypes() {
            return GenericReflectionUtils.getTypeArguments(View.class, getClass());
        }
        
        public void foreach(VFunc1<T> func) {
            Views.foreach(this, func);
        }

        public View<T> readOnlyView() {
            return Views.readOnlyViewOf(this);
        }

        public <TDest> View<TDest> map(Mapper<T, TDest> transformer) {
            return Views.map(this, transformer);
        }
        
        public View<T> filter(Filter<T> filter, VFunc1<T> shunt) {
            return Views.filter(this, filter, shunt);
        }
        
        public View<T> filter(Filter<T> filter) {
        	return Views.filter(this, filter);
        }
        
        public View<T> unique() {
            return Views.unique(this);
        }
        
        public T first() {
            return Views.first(this);
        }

        public View<T> first(int limit) {
            return Views.first(this, limit);
        }

        public View<T> from(int limit) {
            return Views.from(this, limit);
        }

        public View<T> slice(int start, int size) {
            return Views.slice(this, start, size);
        }
        
        public boolean contains(T comp) {
            return Views.contains(this, comp);
        }

        public boolean contains(Predicate<T> pred) {
            return Views.contains(this, pred);
        }

        public View<T> expand(int size, T sub) {
            return Views.expand(this, size, sub);
        }

        /**
         * Use <tt>count</tt> instead
         * @return
         */
        @Deprecated
        public int length() {
            return Views.length(this);
        }

        public int count() {
            return Views.count(this);
        }

        public boolean isEmpty() {
            return Views.isEmpty(this);
        }
        
        public T get(int idx) {
            return Views.get(this, idx);
        }
        
        public View<T> union(Iterable<T> next) {
            return Views.union(this, next);
        }
        
        public View<T> concatenate(Iterable<T> next) {
            return Views.concatenate(this, next);
        }
        
        public T aggregate(Func2<T, T, T> func) {
        	return Views.aggregate(this, func);
        }
        
        public <TAgg> TAgg aggregate(TAgg initial, Func2<TAgg, T, TAgg> func) {
        	return Views.aggregate(this, initial, func);
        }

        public List<T> asList() {
        	return Views.asList(this);
        }
        
        public Set<T> asSet() {
            return Views.asSet(this);
        }
        
        @SuppressWarnings("unchecked")
        public T[] asArray() {
        	return Views.toArray((Class<T>) getGenericTypes().get(0), this);
        }
        
        public <TDestKey, TDestVal> Map<TDestKey, TDestVal>
        asMap(ToMapMapper<T, TDestKey, TDestVal> mapFunc) {
            return Views.asMap(this, mapFunc);
        }

        public <TOrder extends Comparable<TOrder>> View<T> orderBy(Func1<T, TOrder> func) {
        	return Views.orderBy(this, func);
        }

        public View<String> toStrings() {
        	return Views.toStrings(this);
        }
        
        public String toString(String start, String sep, String end) {
        	return Views.toString(this, start, sep, end);
        }
        
        @Override
        public String toString() {
            return Views.toString(this);
        }
        
        public String stringFormat(String fmt, String elemFmt) {
            return Views.stringFormat(this, fmt, elemFmt);
        }
        
        public String delimit(String delimiter) {
        	return Views.delimit(this, delimiter);
        }
    }

    public static class ArrayView<T> extends View<T> {
        protected final T[] arr;

        public ArrayView(T[] arr) { this.arr = arr; }

        @Override public Iterator<T> iterator() {
            return new ArrayIterator();
        }

        public class ArrayIterator implements Iterator<T> {
            private int idx = 0;

            @Override public boolean hasNext() {
                return idx < arr.length;
            }

            @Override
            public T next() {
                return arr[idx++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    }

    public static class OrderableView<T extends Comparable<T>> extends View<T> {
        private final Iterable<T> source;
        
        public OrderableView(Iterable<T> source) {
            this.source = source;
        }
        
        @Override
        public Iterator<T> iterator() {
            return source.iterator();
        }

        public OrderableView<T> naturalOrder() {
            return Views.naturalOrder(this);
        }
        
        public T max() {
            return Views.max(this);
        }
        
        public T min() {
            return Views.min(this);
        }
    }
	
	/** View subclasses that perform specific mutations on iterables/views **/
	
	public static class Passthrough<T> extends View<T> {
		private final Iterable<T> source;
		
		public Passthrough(Iterable<T> source) {
			this.source = source;
		}
		
		@Override
		public Iterator<T> iterator() {
			return source.iterator();
		}
	}

    public static class ReadOnlyPassthrough<T> extends View<T> {
        private final Iterable<T> source;

        public ReadOnlyPassthrough(Iterable<T> source) {
            this.source = source;
        }

        @Override
        public Iterator<T> iterator() {
            return new ReadOnlyIterator(source.iterator());
        }

        public class ReadOnlyIterator implements Iterator<T> {
            private final Iterator<T> sourceItr;

            private ReadOnlyIterator(Iterator<T> sourceItr) {
                this.sourceItr = sourceItr;
            }

            @Override
            public boolean hasNext() {
                return sourceItr.hasNext();
            }

            @Override
            public T next() {
                return sourceItr.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    }

    public static class ListBuilder<T> extends View<T> {
        private final Iterable<T> source;

        public ListBuilder(Iterable<T> source) {
            this.source = source;
        }

        public List<T> list() {
            List<T> list = new ArrayList<T>();
            for (T elem : source) list.add(elem);
            return list;
        }

        @Override
        public Iterator<T> iterator() {
            return list().iterator();
        }
    }

    public static class SetBuilder<T> extends View<T> {
        private final Iterable<T> source;

        public SetBuilder(Iterable<T> source) {
            this.source = source;
        }

        public Set<T> set() {
            return new AbstractSet<T>() {
                @Override
                public Iterator<T> iterator() {
                    return source.iterator();
                }

                @Override
                public int size() {
                    return Views.length(source);
                }
            };
        }

        @Override
        public Iterator<T> iterator() {
            return set().iterator();
        }
    }

    public static class TailLimit<T> extends View<T> {
        protected final Iterable<T> source;
        protected final int limit;

        public TailLimit(Iterable<T> source, final int limit) {
            this.source = source;
            if (limit < 0)
                throw new IllegalArgumentException("limit cannot be < 0");

            this.limit = limit;
        }

        @Override
        public Iterator<T> iterator() {
            return this.new TailLimitIterator();
        }

        public class TailLimitIterator implements Iterator<T> {
            private final Iterator<T> sourceItr = source.iterator();
            private TailLimitIterator() {
                for (int i = 0; i < limit && sourceItr.hasNext(); i++) {
                    sourceItr.next();
                }
            }

            @Override
            public boolean hasNext() {
                return sourceItr.hasNext();
            }

            @Override
            public T next() {
                return sourceItr.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    }

    public static class Limit<T> extends View<T> {
        protected final Iterable<T> source;
        protected final int limit;

        public Limit(Iterable<T> source, int limit) {
            this.source = source;
            if (limit < 0)
                throw new IllegalArgumentException("limit cannot be < 0");

            this.limit = limit;
        }

        @Override
        public Iterator<T> iterator() {
            return this.new LimitIterator();
        }

        public class LimitIterator implements Iterator<T> {
            private final Iterator<T> sourceItr = source.iterator();
            private int pos = 0;

            @Override
            public boolean hasNext() {
                return sourceItr.hasNext() && pos < limit;
            }

            @Override
            public T next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                pos++;
                return sourceItr.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    }
    
    public static class Expand<T> extends View<T> {
        protected final Iterable<T> source;
        protected final int size;
        protected final T sub;

        public Expand(Iterable<T> source, int size, T sub) {
            this.source = source;
            this.sub = sub;

            if (size < 0)
                throw new IllegalArgumentException("limit cannot be < 0");
            this.size = size;
        }

        @Override
        public Iterator<T> iterator() {
            return this.new ExpandIterator();
        }
        
        public class ExpandIterator implements Iterator<T> {
            private final Iterator<T> sourceItr = source.iterator();
            private int pos = 0;

            @Override
            public boolean hasNext() {
                return pos < size;
            }

            @Override
            public T next() {
                if (sourceItr.hasNext())
                    return sourceItr.next();
                pos++;
                return sub;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    }
	
	public static class UniqueView<T> extends View<T> {
	    protected final Iterable<T> source;
	    
	    public UniqueView(Iterable<T> source) {
	        this.source = source;
	    }
	    
        @Override
        public Iterator<T> iterator() {
            return this.new UniqueIterator();
        }
	    
        public class UniqueIterator implements Iterator<T> {
            private final Iterator<T> itr;
            private final Set<T> set = new LinkedHashSet<T>();

            public UniqueIterator() {
                if (source != null)
                    for (T e : source)
                        set.add(e);

                itr = set.iterator();
            }

            @Override
            public boolean hasNext() {
                return itr.hasNext();
            }

            @Override
            public T next() {
                return itr.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    }

    public static class FilteredView<T> extends View<T> {
        protected final Iterable<T> source;
        protected final Predicate<T> predicate;
        protected final VFunc1<T> shunt;

        public FilteredView(Iterable<T> source, Predicate<T> predicate, VFunc1<T> shunt) {
            this.source = source;
            this.predicate = predicate;
            this.shunt = shunt;
        }

        @Override
        public Iterator<T> iterator() {
            return this.new FilterIterator();
        }

        public class FilterIterator implements Iterator<T> {
            Iterator<T> itr = source.iterator();
            T curr = null;
            boolean nextReady = false;
            boolean hasNext = itr.hasNext();

            @Override
            public boolean hasNext() {
                if (!nextReady)
                    goNext();
                nextReady = true;
                return hasNext;
            }

            /**
             * Must set <tt>curr</tt> to the next element that passes
             * the predicate, and hasNext to
             */
            private void goNext() {
                while (true) {
                    if (!itr.hasNext()) {
                        hasNext = false;
                        break;
                    }
                    curr = itr.next();
                    if (predicate.call(curr)) {
                        break;
                    } else if (shunt != null) {
                        shunt.call(curr);
                    }
                }
            }

            @Override
            public T next() {
                if (!nextReady)
                    goNext();
                nextReady = false;
                return curr;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    }

    public static class Transform<TSource, TDest> extends View<TDest> {
        protected transient Iterable<TSource> source;
        protected final Func1<TSource, TDest> mapFunc;

        public Transform(Iterable<TSource> source, Func1<TSource, TDest> mapper) {
            this.source = source;
            this.mapFunc = mapper;
        }

        @Override
        public Iterator<TDest> iterator() {
            return this.new TransformingIterator();
        }

        public class TransformingIterator implements Iterator<TDest> {
            private final Iterator<TSource> sourceItr;

            public TransformingIterator() {
                sourceItr = source.iterator();
            }

            @Override
            public boolean hasNext() {
                return sourceItr.hasNext();
            }

            @Override
            public TDest next() {
                return mapFunc.call(sourceItr.next());
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    }

    private static final class Concatenation<TElem> extends View<TElem> {
        protected final Iterable<TElem> first;
        protected final Iterable<TElem> next;

        public Concatenation(Iterable<TElem> first, Iterable<TElem> next) {
            this.first = first;
            this.next = next;
        }

        @Override
        public Iterator<TElem> iterator() {
            return this.new ConcatenationIterator();
        }

        public class ConcatenationIterator implements Iterator<TElem> {
            private Iterator<TElem> currItr;
            private boolean firstExhausted;
          
            public ConcatenationIterator() {
                currItr = first.iterator();
            }
            
            @Override
            public boolean hasNext() {
                if (!currItr.hasNext() && !firstExhausted) {
                    currItr = next.iterator();
                    firstExhausted = true;
                }
                return currItr.hasNext();
            }

            @Override
            public TElem next() {
                return currItr.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    }

    public static final <T> Iterable<Iterable<T>> grouped(Iterable<T> source, int size) {
        return new Grouper(source, size);
    }

    private static final class Grouper<TElem> extends View<Iterable<TElem>> {
        private final Iterable<TElem> source;
        private final int sourceSize;
        private final int groupSize;

        public Grouper(Iterable<TElem> source, int groupSize) {
            if (groupSize < 1)
                throw new IllegalArgumentException("group size cannot be < 1");
            this.source = source;
            this.groupSize = groupSize;
            this.sourceSize = Views.count(source);
        }

        @Override
        public Iterator<Iterable<TElem>> iterator() {
            return this.new GrouperIterator();
        }

        public class GrouperIterator implements Iterator<Iterable<TElem>> {
            private int idx = 0;

            @Override
            public boolean hasNext() {
                return idx < sourceSize;
            }

            @Override
            public Iterable<TElem> next() {
                Iterable<TElem> ret = Views.slice(source, idx, groupSize);
                idx += groupSize;
                return ret;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    }

    /**
     * string-related utilities *
     */

    private static final class StringMaker<TElem> implements Aggregate<String, TElem> {
        private final StringBuilder sb = new StringBuilder();
        private final String start, sep, end;
        private boolean first = true;

        protected StringMaker(String start, String sep, String end) {
            this.start = start;
            this.sep = sep;
            this.end = end;

            sb.append(this.start);
        }

        @Override
        public void call(TElem next) {
            if (!first) sb.append(sep);
            else first = false;
            sb.append(String.valueOf(next));
        }

        @Override
        public String value() {
            sb.append(end);
            return sb.toString();
        }
    }
    
	private static final class FormattedStringMaker<TElem> implements Aggregate<String, TElem> {
        private final StringBuilder sb = new StringBuilder();
        private final String fmt, elemFmt;

        protected FormattedStringMaker(String fmt, String elemFmt) {
            this.fmt = fmt;
            this.elemFmt = elemFmt;
        }
        
        @Override
        public void call(TElem next) {
            sb.append(String.format(elemFmt, String.valueOf(next)));
        }

        @Override
        public String value() {
            return String.format(fmt, sb.toString());
        }
    }

    static class NullFilter<T> implements Predicate<T> {
        @Override
        public Boolean call(T elem) {
            return elem != null;
        }
    }
}
