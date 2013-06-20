/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import static javolution.annotation.RealTime.Limit.LINEAR;
import static javolution.annotation.RealTime.Limit.N_SQUARE;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javolution.annotation.DefaultTextFormat;
import javolution.annotation.Parallelizable;
import javolution.annotation.RealTime;
import javolution.internal.util.collection.ComparatorCollectionImpl;
import javolution.internal.util.collection.DistinctCollectionImpl;
import javolution.internal.util.collection.FilteredCollectionImpl;
import javolution.internal.util.collection.MappedCollectionImpl;
import javolution.internal.util.collection.ParallelCollectionImpl;
import javolution.internal.util.collection.ReversedCollectionImpl;
import javolution.internal.util.collection.SequentialCollectionImpl;
import javolution.internal.util.collection.SharedCollectionImpl;
import javolution.internal.util.collection.SortedCollectionImpl;
import javolution.internal.util.collection.UnmodifiableCollectionImpl;
import javolution.internal.util.collection.closure.DoWhileConsumerImpl;
import javolution.internal.util.collection.closure.FormatConsumerImpl;
import javolution.internal.util.collection.closure.SearchConsumerImpl;
import javolution.internal.util.collection.closure.SingleRemoveFilterImpl;
import javolution.internal.util.comparator.WrapperComparatorImpl;
import javolution.lang.Copyable;
import javolution.text.Cursor;
import javolution.text.TextContext;
import javolution.text.TextFormat;
import javolution.util.function.CollectionOperator;
import javolution.util.function.Comparators;
import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Function;
import javolution.util.function.Operators;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;
import javolution.util.service.CollectionService.IterationController;

/**
 * <p> A high-performance collection with {@link RealTime real-time} behavior; 
 *     smooth capacity increase/decrease and minimal memory footprint. 
 *     Fast collections support multiple views which can be chained.
 * <ul>
 *    <li>{@link #unmodifiable} - View which does not allow any modification.</li>
 *    <li>{@link #shared} - View allowing concurrent modifications.</li>
 *    <li>{@link #parallel} - A {@link #shared shared} view allowing parallel processing (closure-based).</li>
 *    <li>{@link #sequential} - View disallowing parallel processing.</li>
 *    <li>{@link #filtered} - View exposing the elements matching the specified filter.</li>
 *    <li>{@link #mapped} - View exposing elements through the specified mapping function.</li>
 *    <li>{@link #sorted} - View exposing elements according to the collection sorting order.</li>
 *    <li>{@link #reversed} - View exposing elements in reverse iterative order.</li>
 *    <li>{@link #distinct} - View exposing each element only once.</li>
 *    <li>{@link #comparator} - View using the specified comparator for element equality/order.</li>
 * </ul>
 *    Views are similar to <a href="http://lambdadoc.net/api/java/util/stream/package-summary.html">
 *    Java 8 streams</a> except that views are themselves collections (virtual collections)
 *    and actions on a view can impact the original collection. Collection views are nothing "new" 
 *    since they already existed in the original java.util collection classes (e.g. List.subList(...),
 *    Map.keySet(), Map.values()). Javolution extends to this concept and allows views to be chained 
 *    which addresses the concern of class proliferation (see
 *    <a href="http://cr.openjdk.java.net/~briangoetz/lambda/collections-overview.html">
 *    State of the Lambda: Libraries Edition</a>). 
 *    [code]
 *    FastTable<String> names = new FastTable<String>("Oscar Thon", "Eva Poret", "Paul Auchon");
 *    boolean found = names.comparator(Comparators.LEXICAL_CASE_INSENSITIVE).contains("LUC SURIEUX"); 
 *    names.subList(0, n).clear(); // Removes the n first names (see java.util.List).
 *    names.distinct().add("Guy Liguili"); // Adds "Guy Liguili" only if not already present.
 *    names.filtered(isLong).clear(); // Removes all the persons with long names.
 *    names.parallel().filtered(isLong).clear(); // Same as above but performed concurrently.
 *    ...
 *    Predicate<CharSequence> isLong = new Predicate<CharSequence>() { 
 *         public boolean test(CharSequence csq) {
 *             return csq.length() > 16; 
 *         }
 *    });
 *    [/code]
 *    
 *    Views can of course be used to perform "stream" oriented filter-map-reduce operations with the same benefits:
 *    Parallelism support, excellent memory characteristics (no caching and cost nothing to create), etc.
 *    [code]
 *    String anyLongName = names.filtered(isLong).reduce(Operators.ANY); // Returns any long name.
 *    int nbrChars = names.mapped(toLength).reduce(Operators.SUM); // Returns the total number of characters.
 *    int maxLength = names.parallel().mapped(toLength).reduce(Operators.MAX); // Finds the longest name in parallel.
 *    ...
 *    Function<CharSequence, Integer> toLength = new Function<CharSequence, Integer>() {
 *         public Integer apply(CharSequence csq) {
 *             return csq.length(); 
 *         }
 *    });
 *    
 *    // JDK Class.getEnclosingMethod using Javolution's views and Java 8 (to be compared with the current 20 lines implementation !).
 *    Method matching = new FastTable<Method>(enclosingInfo.getEnclosingClass().getDeclaredMethods())
 *       .filtered(m -> Comparators.STANDARD.areEqual(m.getName(), enclosingInfo.getName())
 *       .filtered(m -> Comparators.ARRAY.areEqual(m.getParameterTypes(), parameterClasses))
 *       .filtered(m -> Comparators.STANDARD.areEqual(m.getReturnType(), returnType))
 *       .reduce(Operators.ANY);
 *    if (matching == null) throw new InternalError("Enclosing method not found");
 *    return matching;
 *    [/code]
 *    </p>
 *           
 * <p> Fast collections can be iterated over using closures, this is also the preferred way 
 *     to iterate over {@link #shared() shared} collections (no concurrent update possible).
 *     If a collection (or a map) is shared, derived views are also thread-safe.
 *     Similarly, if a collection is {@link #parallel parallel}, closure-based iterations 
 *     on derived views can be performed concurrently. 
 *     [code]
 *     FastMap<String, Runnable> tasks = ...
 *     ...
 *     tasks.values().parallel().forEach(new Consumer<Runnable>() { // Executes task concurrently. 
 *         public void accept(Runnable task) {
 *              task.run();
 *         }
 *     });
 *     [/code]
 *     With Java 8, closures are greatly simplified using lambda expressions.
 *     [code]
 *     tasks.values().parallel().forEach(task -> task.run()); // Same as above.
 *     names.sorted().reversed().forEach(str -> System.out.println(str)); // Prints names in reverse alphabetical order. 
 *     [/code]
 *     </p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
@RealTime // Inheritable.
@Parallelizable(mutexFree = false, comment = "Mutexes used by shared/parallel views.")
@DefaultTextFormat(FastCollection.StandardText.class)
public abstract class FastCollection<E> implements Collection<E>,
        Copyable<FastCollection<E>>, Serializable {

    private static final long serialVersionUID = 0x600L; // Version.

    /**
     * Default constructor.
     */
    protected FastCollection() {}

    /***************************************************************************
     * Views.
     */

    /**
     * Returns an unmodifiable view over this collection. Any attempt to 
     * modify the collection through this view will result into 
     * a {@link java.lang.UnsupportedOperationException} being raised.
     */
    public FastCollection<E> unmodifiable() {
        return new UnmodifiableCollectionImpl<E>(service());
    }

    /**
     * Returns a thread-safe view over this collection allowing 
     * concurrent read without blocking and concurrent write possibly 
     * blocking. All updates performed by the specified action on this 
     * collection are atomic as far as this collection's readers are concerned.
     * To perform complex actions on a shared collection in an atomic manner, 
     * the {@link #atomic atomic} method should be used (on the shared view
     * or on any view derived from the shared view).
     * 
     *  @see #atomic(Runnable)
     */
    public FastCollection<E> shared() {
        return new SharedCollectionImpl<E>(service());
    }

    /** 
     * Returns a view over this collection using the specified comparator
     * for element equality or sorting. Two elements are 
     * considered equals if {@code cmp.compare(e1, e2) == 0}.
     * 
     * @see #contains(java.lang.Object) 
     * @see #remove(java.lang.Object) 
     * @see #sorted() 
     * @see #distinct() 
     */
    public FastCollection<E> comparator(Comparator<? super E> cmp) {
        if (cmp instanceof EqualityComparator) // Best (areEqual consistent with compare) !
            return new ComparatorCollectionImpl<E>(service(),
                    (EqualityComparator<? super E>) cmp);
        return new ComparatorCollectionImpl<E>(service(),
                new WrapperComparatorImpl<E>(cmp)); // areEqual(x,y) == (compare(x,y) == 0)
    }

    /** 
     * Returns a view exposing only the elements matching the specified 
     * filter.
     */
    public FastCollection<E> filtered(Predicate<? super E> filter) {
        return new FilteredCollectionImpl<E>(service(), filter);
    }

    /** 
     * Returns a view exposing elements through the specified mapping function.
     * The returned view does not allow new elements to be added.
     */
    public <R> FastCollection<R> mapped(
            Function<? super E, ? extends R> function) {
        return new MappedCollectionImpl<E, R>(service(), function);
    }

    /** 
     * Returns an unmodifiable view exposing element sorted according to this
     * collection {@link #comparator() comparator}. 
     * The {@link #comparator(java.util.Comparator) comparator view} can be used
     * to sort elements using any user-defined comparator.
     */
    public FastCollection<E> sorted() {
        return new SortedCollectionImpl<E>(service());
    }

    /** 
     * Returns a view exposing elements in reverse iterative order.
     */
    public FastCollection<E> reversed() {
        return new ReversedCollectionImpl<E>(service());
    }

    /** 
     * Returns a view exposing only distinct elements (it does not iterate twice 
     * over the {@link #comparator() same} elements). Adding elements already 
     * in the collection through this view has no effect (if this collection is 
     * initially empty, using the distinct view prevents element duplication).   
     */
    public FastCollection<E> distinct() {
        return new DistinctCollectionImpl<E>(service());
    }

    /** 
     * Returns a parallel/{@link #shared() shared} view of this collection. 
     * Using this view, closure-based iteration can be performed concurrently. 
     * The default implementation is based upon 
     * {@link javolution.context.ConcurrentContext ConcurrentContext}.
     * 
     * <p> Note: Some view may prevent parallel processing over their 
     *           collection target. It is therefore preferable for 
     *           parallel views to be the last view before processing. 
     *           For example {@code collection.unmodifiable().parallel()}
     *           is preferred to {@code collection.parallel().unmodifiable()}.</p>
     */
    public FastCollection<E> parallel() {
        return new ParallelCollectionImpl<E>(service());
    }

    /** 
     * Returns a sequential view of this collection. Using this view, 
     * all closure-based iterations are performed in sequence.
     */
    public FastCollection<E> sequential() {
        return new SequentialCollectionImpl<E>(service());
    }

    /***************************************************************************
     * Closure operations.
     */

    /** 
     * Iterates over all this collection elements applying the specified 
     * consumer. Iterations are performed concurrently if the 
     * collection is {@link #parallel() parallel}.
     * 
     * @param consumer the functional consumer applied to the collection elements.
     */
    @RealTime(limit = LINEAR)
    public void forEach(Consumer<? super E> consumer) {
        service().forEach(consumer, IterationController.STANDARD);
    }

    /** 
     * Iterates over this collection elements applying the specified predicate
     * until that predicate returns {@link false} (if the collection is 
     * {@link #parallel() parallel} all concurrent iterations will terminate then).
     * 
     * @param doContinue the functional predicate applied to the collection elements.
     * @return {@code true} if the iteration has not been interrupted (no predicate 
     *         evaluation returned {@code false}); {@code true} otherwise.
     */
    @RealTime(limit = LINEAR)
    public boolean doWhile(Predicate<? super E> doContinue) {
        DoWhileConsumerImpl<E> doWhile = new DoWhileConsumerImpl<E>(doContinue);
        service().forEach(doWhile, doWhile);
        return !doWhile.isTerminated();
    }

    /**
     * Removes from this collection all the elements matching the specified
     * functional predicate. Removal are performed concurrently if the 
     * collection is {@link #parallel() parallel}.
     * 
     * @param filter a predicate returning {@code true} for elements to be removed.
     * @return {@code true} if at least one element has been removed;
     *         {@code false} otherwise.
     */
    @RealTime(limit = LINEAR)
    public boolean removeIf(final Predicate<? super E> filter) {
        return service().removeIf(filter, IterationController.STANDARD);
    }

    /** 
     * Performs a reduction of the elements of this collection using the 
     * specified operator (convenience method). This may not involve iterating 
     * over all the collection elements, for example the operators:
     * {@link Operators#ANY ANY}, {@link Operators#AND AND} and  
     * {@link Operators#OR} may stop iterating early.
     *    
     * @param operator the operator.
     * @return {@code operator.apply(this.getService())}
     */
    @SuppressWarnings("unchecked")
    @RealTime(limit = LINEAR)
    public E reduce(CollectionOperator<? super E> operator) {
        return ((CollectionOperator<E>) operator).apply(service());
    }

    /***************************************************************************
     * Collection operations.
     */

    @Override
    public boolean add(E element) {
        return service().add(element);
    }

    @Override
    public boolean isEmpty() {
        return this.mapped(TO_ONE).reduce(Operators.ANY) != null;
    }

    @Override
    @RealTime(limit = LINEAR)
    public int size() {
        return this.mapped(TO_ONE).reduce(Operators.SUM);
    }

   private static final Integer ONE = new Integer(1);
   private static final Function<Object, Integer> TO_ONE = new Function<Object, Integer>() {
 
        @Override
        public Integer apply(Object param) {
            return ONE;
        }

    };

    @Override
    @RealTime(limit = LINEAR)
    public void clear() {
        removeIf(new Predicate<E>() {
            @Override
            public boolean test(E param) {
                return true;
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    @RealTime(limit = LINEAR)
    public boolean contains(final Object element) {
        SearchConsumerImpl<E> search = new SearchConsumerImpl<E>((E) element,
                service().comparator());
        service().forEach(search, search);
        return search.isFound();
    }

    @SuppressWarnings("unchecked")
    @RealTime(limit = LINEAR)
    public boolean remove(final Object element) {
        SingleRemoveFilterImpl<E> removeFilter = new SingleRemoveFilterImpl<E>(
                (E) element, service().comparator());
        service().removeIf(removeFilter, removeFilter);
        return removeFilter.isFound();
    }

    @Override
    public Iterator<E> iterator() {
        return service().iterator();
    }

    @Override
    @RealTime(limit = LINEAR)
    public boolean addAll(Collection<? extends E> that) {
        if (that instanceof FastCollection)
            return addAll((FastCollection<? extends E>) that);
        boolean modified = false;
        for (E e : that) {
            if (add(e)) {
                modified = true;
            }
        }
        return modified;
    }

    private boolean addAll(FastCollection<? extends E> that) {
        final boolean[] modified = new boolean[1];
        that.forEach(new Consumer<E>() {
            @Override
            public void accept(E param) {
                if (add(param)) {
                    modified[0] = true;
                }
            }
        });
        return modified[0];
    }

    @Override
    @RealTime(limit = N_SQUARE)
    public boolean containsAll(Collection<?> that) {
        if (that instanceof FastCollection)
            return containsAll((FastCollection<?>) that);
        for (Object e : that) {
            if (!contains(e))
                return false;
        }
        return true;
    }

    private boolean containsAll(FastCollection<?> that) {
        return that.doWhile(new Predicate<Object>() {

            @Override
            public boolean test(Object param) {
                return contains(param);
            }
        });
    }

    @Override
    @RealTime(limit = N_SQUARE)
    public boolean removeAll(final Collection<?> that) {
        return removeIf(new Predicate<E>() {
            public boolean test(E param) {
                return that.contains(param);
            }
        });
    }

    @Override
    @RealTime(limit = N_SQUARE)
    public boolean retainAll(final Collection<?> that) {
        return removeIf(new Predicate<E>() {
            public boolean test(E param) {
                return !that.contains(param);
            }
        });
    }

    @Override
    @RealTime(limit = LINEAR)
    public Object[] toArray() {
        return toArray(new Object[size()]);
    }

    @Override
    @SuppressWarnings("unchecked")
    @RealTime(limit = LINEAR)
    public <T> T[] toArray(final T[] array) {
        final int size = size();
        final T[] result = (size <= array.length) ? array
                : (T[]) java.lang.reflect.Array.newInstance(array.getClass()
                        .getComponentType(), size);
        service().forEach(new Consumer<E>() {
            int i;

            @Override
            public void accept(E e) {
                if (i >= result.length)
                    return; // Concurrent add.
                result[i++] = (T) e;
            }
        }, IterationController.SEQUENTIAL); // Can only be done sequentially.
        if (result.length > size) {
            result[size] = null; // As per Collection contract.
        }
        return result;
    }

    /***************************************************************************
     * Misc.
     */

    /** 
     * Returns the comparator uses by this collection for equality and/or 
     * ordering if this collection is ordered.
     */
    public EqualityComparator<? super E> comparator() {
        return service().comparator();
    }

    /** 
     * Executes the specified action on this collection in an atomic manner. As 
     * far as readers of this collection are concerned, either they see the full
     * result of the action or nothing. This method is relevant only for
     * {@link #shared shared} or {@link #parallel parallel} collections.
     * The framework ensures that only one atomic action can be performed at 
     * any given time and no concurrent closure-based iteration is possible 
     * during that time.
     * 
     * @param action the action to be executed in an atomic manner.
     */
    public void atomic(Runnable action) {
        service().atomic(action);
    }

    /**
     * Compares the specified object with this collection for equality.
     * If this collection is a set, returns <code>true</code> if the specified
     * object is also a set, the two sets have the same size and the specified 
     * set contains all the element of this set. If this collection is a list, 
     * returns <code>true</code> if and
     * only if the specified object is also a list, both lists have the same 
     * size, and all corresponding pairs of elements in
     * the two lists are <i>equal</i> using the default object equality.
     * If this collection is neither a list, nor a set, this method returns 
     * the default object equality (<code>this == obj</code>).
     *
     * @param obj the object to be compared for equality with this collection
     * @return <code>true</code> if both collection are considered equals;
     *        <code>false</code> otherwise. 
     */
    @SuppressWarnings("unchecked")
    @Override
    @RealTime(limit = LINEAR)
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (this instanceof Set) {
            if (!(obj instanceof Set))
                return false;
            Set<E> that = (Set<E>) obj;
            return (this.size() == that.size()) && containsAll(that);
        } else if (this instanceof List) {
            if (!(obj instanceof List))
                return false;
            List<E> that = (List<E>) obj;
            return Comparators.ARRAY.areEqual(this.toArray(), that.toArray());
        } else {
            return false;
        }
    }

    @Override
    @RealTime(limit = LINEAR)
    public int hashCode() {
        if (this instanceof Set) {
            return mapped(TO_HASH).reduce(Operators.SUM); // Can be performed in parallel.            
        } else if (this instanceof List) {
            final int[] hash = new int[1];
            service().forEach(new Consumer<E>() {

                @Override
                public void accept(E e) {
                    hash[0] = 31 * hash[0] + ((e == null) ? 0 : e.hashCode());
                }
            }, IterationController.SEQUENTIAL);
            return hash[0];
        } else {
            return super.hashCode();
        }
    }

    private static Function<Object, Integer> TO_HASH = new Function<Object, Integer>() {

        @Override
        public Integer apply(Object param) {
            return param == null ? 0 : param.hashCode();
        }
    };

    /**
     * Returns a copy of this collection holding a copies of its elements.
     * For lists and sets the copy is {@link #equals} to the original 
     * (the copy of a list is a list and the copy of a set is a set).
     * The iterative order of the copy is the same as the original (even for 
     * sets).
     */
    @Override
    @RealTime(limit = LINEAR)
    public FastCollection<E> copy() {
        final FastCollection<E> copy = (this instanceof Set) ? new FastSet<E>(
                service().comparator()) : // FastSet keeps iterative order.
                new FastTable<E>(service().comparator());
        service().forEach(new Consumer<E>() {
            @SuppressWarnings("unchecked")
            @Override
            public void accept(E e) {
                E c = (e instanceof Copyable) ? ((Copyable<E>) e).copy() : e;
                copy.add(c);
            }
        }, IterationController.SEQUENTIAL);
        return copy;
    }

    @Override
    @RealTime(limit = LINEAR)
    public String toString() {
        return TextContext.getFormat(FastCollection.class).format(this);
    }

    /**
     * Returns the service implementation of this collection (for sub-classes).
     */
    protected abstract CollectionService<E> service();

    /**
     * Returns the service implementation of any fast collection 
     * (for sub-classes).
     */
    protected static <E> CollectionService<E> serviceOf(
            FastCollection<E> collection) {
        return collection.service();
    }

    /***************************************************************************
     * Textual format.
     */

    /**
     * The standard java collection format (parsing not supported). 
     * It is the format used when printing standard {@code java.util.Collection} 
     * instances except that the elements are printed using their local 
     * {@link TextFormat TextFormat} instead of calling 
     * {@code String.valueOf(element)} (which does not require the creation of 
     * a large number of temporary {@link String} objects). 
     */
    @Parallelizable
    @RealTime(limit = LINEAR)
    public static class StandardText extends TextFormat<FastCollection<?>> {

        @Override
        public FastCollection<Object> parse(CharSequence csq, Cursor cursor)
                throws IllegalArgumentException {
            throw new UnsupportedOperationException(
                    "Parsing of generic FastCollection not supported");
        }

        @Override
        public Appendable format(FastCollection<?> that, final Appendable dest)
                throws IOException {
            dest.append('[');
            FormatConsumerImpl formatConsumer = new FormatConsumerImpl(dest);
            that.service().forEach(formatConsumer, formatConsumer);
            if (formatConsumer.ioException() != null)
                throw formatConsumer.ioException();
            return dest.append(']');
        }
    }

}