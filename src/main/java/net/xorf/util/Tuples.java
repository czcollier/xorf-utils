package net.xorf.util;


import java.util.Iterator;
import java.util.Map;

/**
 * Classes within implement a pidgin implementation of tuples for Java.  These
 * are ordered sequences of elements of any type.  Unlike Java collections, elements
 * can be of mixed types.  Also unlike collections, tupes are of fixed size and
 * are intended to be small (this implementation currently supports tuples only as big
 * as four elements).
 *
 * The size of the tuple and the types contained within define the overall type of
 * the tuple itself.  This is useful for defining simple compound types that are
 * used temporarily and don't need the machinery of a full class definition.
 *
 * Typing for these tuples is implemented using separate classes for different sizes,
 * and generic parameters for each element.
 *
 * Use the varargs {@link #tuple(Object[])})} to create a tuple generically.
 *
 * Example usage:
 * <pre>
 * {@code
 * protected Tuples.Tuple2<String, String> splitOnSlash(String str) {
 *    final String[] elems = str.split("/", 2);
 *    return Tuples.tuple(elems[0], (elems.length > 1) ? elems[1] : null);
 * }
 * }
 * </pre>
 */
public class Tuples {
    private Tuples() { }

	public interface Tuple { }

	public static class Tuple0 implements Tuple {
		@Override public String toString() {
			return "()";
		}
	}

	public static class Tuple1<T1> extends Tuple0 {
		public final T1 _1;
		public Tuple1(T1 p1) { _1 = p1; }
		@Override public String toString() {
			return String.format("(%1$s)", _1);
		}
	}

	public static class Tuple2<T1, T2> extends Tuple1<T1> {
		public final T2 _2;
		public Tuple2(T1 p1, T2 p2) { super(p1); _2 = p2;}
		@Override public String toString() {
			return String.format("(%1$s, %2$s)", _1, _2);
		}
	}

	public static class Tuple3<T1, T2, T3> extends Tuple2<T1, T2> {
		public final T3 _3;
		public Tuple3(T1 p1, T2 p2, T3 p3) { super(p1, p2); _3 = p3; }
		@Override public String toString() {
			return String.format("(%1$s, %2$s, %3$s)", _1, _2, _3);
		}
	}

	public static class Tuple4<T1, T2, T3, T4> extends Tuple3<T1, T2, T3> {
		public final T4 _4;
		public Tuple4(T1 p1, T2 p2, T3 p3, T4 p4) { super(p1, p2, p3); _4 = p4; }
		@Override public String toString() {
			return String.format("(%1$s, %2$s, %3$s, %4$s)", _1, _2, _3, _4);
		}
	}

	public static Tuple0 tuple() {
		return new Tuple0();
	}

	public static <T1> Tuple1<T1> tuple(T1 v1) {
		return new Tuple1<T1>(v1);
	}

	public static <T1, T2> Tuple2<T1, T2> tuple(T1 v1, T2 v2) {
		return new Tuple2<T1, T2>(v1, v2);
	}

	public static <T1, T2, T3> Tuple3<T1, T2, T3> tuple(T1 v1, T2 v2, T3 v3) {
		return new Tuple3<T1, T2, T3>(v1, v2, v3);
	}

	public static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> tuple(T1 v1, T2 v2, T3 v3, T4 v4) {
		return new Tuple4<T1, T2, T3, T4>(v1, v2, v3, v4);
	}

    public static <T> Tuple tuple(T... v) {
        switch (v.length) {
           case 1: return tuple(v[0]);
           case 2: return tuple(v[0], v[1]);
           case 3: return tuple(v[0], v[1], v[2]);
           case 4: return tuple(v[0], v[1], v[2], v[3]);
           default:
               throw new IllegalArgumentException("sorry, we don't support tuples with > 4 elements right now");
        }
    }

    public static <T> Iterable<Tuple1<T>> zip(Iterable<T> itr) {
        return Views.map(itr, new Views.Func1<T, Tuple1<T>>() {
            @Override
            public Tuple1<T> call(T arg) {
                return tuple(arg);
            }
        });
    }

    public static <T1, T2> Iterable<Tuple2<T1, T2>> zip(Iterable<T1> i1, Iterable<T2> i2) {
        final Iterator<T2> itr2 = i2.iterator();

        return Views.map(i1, new Views.Func1<T1, Tuple2<T1, T2>>() {
            @Override
            public Tuple2<T1, T2> call(T1 arg) {
                return tuple(arg, itr2.next());
            }
        });
    }

    public static <T1, T2, T3> Iterable<Tuple3<T1, T2, T3>> zip(Iterable<T1> i1, Iterable<T2> i2, Iterable<T3> i3) {
        final Iterator<T2> itr2 = i2.iterator();
        final Iterator<T3> itr3 = i3.iterator();

        return Views.map(i1, new Views.Func1<T1, Tuple3<T1, T2, T3>>() {
            @Override
            public Tuple3<T1, T2, T3> call(T1 arg) {
                return tuple(arg, itr2.next(), itr3.next());
            }
        });
    }

    public static <T> Tuple1<Iterable<T>> unzip1(Iterable<Tuple1<T>> tups) {
        final Iterable<T> i = Views.map(tups, new Views.Func1<Tuple1<T>, T>() {
            @Override
            public T call(Tuple1<T> arg) {
                return arg._1;
            }
        });

        return tuple(i);
    }

    public static <T1, T2> Tuple2<Iterable<T1>, Iterable<T2>>
        unzip2(Iterable<? super Tuple2<T1, T2>> tups) {
        Tuple1<Iterable<T1>> i1 = unzip1((Iterable<Tuple1<T1>>)tups);

        final Iterable<T2> i2 = Views.map((Iterable<Tuple2<T1, T2>>) tups, new Views.Func1<Tuple2<T1, T2>, T2>() {
            @Override
            public T2 call(Tuple2<T1, T2> arg) {
                return arg._2;
            }
        });

        return Tuples.tuple(i1._1, i2);
    }

    public static <T1, T2, T3>  Tuple3<Iterable<T1>, Iterable<T2>, Iterable<T3>>
        unzip3(Iterable<? super Tuple3<T1, T2, T3>> tups) {
        Tuple2<Iterable<T1>, Iterable<T2>> i = unzip2((Iterable<Tuple2<T1, T2>>)tups);

        final Iterable<T3> i3 = Views.map((Iterable<Tuple3<T1, T2, T3>>) tups, new Views.Func1<Tuple3<T1, T2, T3>, T3>() {
            @Override
            public T3 call(Tuple3<T1, T2, T3> arg) {
                return arg._3;
            }
        });

        return tuple(i._1, i._2, i3);
    }

    public static <K, V> Map<K, V> toMap(Iterable<Tuple2<K, V>> tuples) {
        final Literals.MapBuilder<K, V> m = Literals.map();
        for (Tuples.Tuple2<K, V> e : tuples)
            m.map(e._1, e._2);
        return m;
    }
}
