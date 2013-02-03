package javolution.util;

import java.io.Serializable;
import java.util.Comparator;
import javolution.annotation.StackSafe;
import javolution.lang.Copyable;

/**
 * <p> A comparator to be used for equality as well as for ordering.
 *     Instances of this class provide a hashcode function 
 *     consistent with equal (if two objects {@link #areEqual
 *     are equal}, they have the same {@link #hashCodeOf hashcode}),
 *     equality with <code>null</code> values is supported.</p>
 *     
 * <p> {@link FastComparator} can be employed with {@link FastMap} (e.g. 
 *     custom key comparators for identity maps, value retrieval using keys
 *     of a different class that the map keys) or with {@link FastCollection}
 *     classes.</p>
 *     
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
@StackSafe(initialization = false)
public abstract class FastComparator<T> implements Comparator<T>,
        Copyable<FastComparator<T>>, Serializable {

    /**
     * Holds the default object comparator.
     * Two instances o1 and o2 are considered {@link #areEqual equal} if and
     * only if <code>o1.equals(o2)</code>. The {@link #compare} method 
     * throws {@link ClassCastException} if the specified objects are not
     * {@link Comparable}. 
     */
    public static final FastComparator<Object> DEFAULT = new Default();

    private static final class Default extends FastComparator<Object> {

        public int hashCodeOf(Object obj) {
            return (obj == null) ? 0 : obj.hashCode();
        }

        public boolean areEqual(Object o1, Object o2) {
            return (o1 == null) ? (o2 == null) : (o1 == o2) || o1.equals(o2);
        }

        @SuppressWarnings("unchecked")
        public int compare(Object o1, Object o2) {
            return ((Comparable<Object>) o1).compareTo(o2);
        }

        public Default copy() {
            return this; // Unique instance always allocated on the heap.
        }

        private static final long serialVersionUID = 3071226918360740529L;
    };

    /**
     * Holds the identity comparator.
     * Two instances o1 and o2 are considered {@link #areEqual equal} 
     * if and only if <code>(o1 == o2)</code>. 
     * The {@link #compare} method throws {@link ClassCastException} if the 
     * specified objects are not {@link Comparable}.
     */
    public static final FastComparator<Object> IDENTITY = new Identity();

    private static final class Identity extends FastComparator<Object> {
        public int hashCodeOf(Object obj) {
            return System.identityHashCode(obj);
        }

        public boolean areEqual(Object o1, Object o2) {
            return o1 == o2;
        }

        @SuppressWarnings("unchecked")
        public int compare(Object o1, Object o2) {
            return ((Comparable<Object>) o1).compareTo(o2);
        }

        public Identity copy() {
            return this; // Unique instance always allocated on the heap.
        }

        private static final long serialVersionUID = 7945597637298349867L;
    };

    /**
     * Holds a lexicographic comparator for any {@link CharSequence}.
     * Hashcodes are calculated by taking a sample of few characters instead of 
     * the whole character sequence.
     */
    public static final FastComparator<CharSequence> LEXICAL = new Lexical();

    private static final class Lexical extends FastComparator<CharSequence> {

        public int hashCodeOf(CharSequence csq) {
            if (csq == null)
                return 0;
            final int length = csq.length();
            if (length == 0)
                return 0;
            return csq.charAt(0) + csq.charAt(length - 1) * 31
                    + csq.charAt(length >> 1) * 1009 + csq.charAt(length >> 2)
                    * 27583 + csq.charAt(length - 1 - (length >> 2)) * 73408859;
        }

        public boolean areEqual(CharSequence csq1, CharSequence csq2) {
            if ((csq1 == null) || (csq2 == null))
                return csq1 == csq2;
            final int length = csq1.length();
            if (csq2.length() != length)
                return false;
            for (int i = 0; i < length;) {
                if (csq1.charAt(i) != csq2.charAt(i++))
                    return false;
            }
            return true;
        }

        public int compare(CharSequence left, CharSequence right) {
            int i = 0;
            int n = Math.min(left.length(), right.length());
            while (n-- != 0) {
                char c1 = left.charAt(i);
                char c2 = right.charAt(i++);
                if (c1 != c2)
                    return c1 - c2;
            }
            return left.length() - right.length();
        }

        public Lexical copy() {
            return this; // Unique instance always allocated on the heap.
        }

        private static final long serialVersionUID = -3910379694278449866L;
    };

    /**
     * Holds an optimized comparator for <code>java.lang.String</code>
     * instances.
     */
    public static final FastComparator<String> STRING = new StringComparator();

    private static final class StringComparator extends FastComparator<String> {

        public int hashCodeOf(String str) {
            return (str != null) ? str.hashCode() : 0;
        }

        public boolean areEqual(String str1, String str2) {
            return (str1 == null) ? (str2 == null) : (str1 == str2)
                    || str1.equals(str2);
        }

        public int compare(String left, String right) {
            return left.compareTo(right);
        }

        public StringComparator copy() {
            return this; // Unique instance always allocated on the heap.
        }

        private static final long serialVersionUID = -2913706666731177359L;
    };

    /**
     * Returns the hash code for the specified object (consistent with 
     * {@link #areEqual}). Two objects considered {@link #areEqual equal} have 
     * the same hash code. The hash code of <code>null</code> is always 
     * <code>0</code>.
     * 
     * @param  obj the object to return the hashcode for.
     * @return the hashcode for the specified object.
     */
    public abstract int hashCodeOf(T obj);

    /**
     * Indicates if the specified objects can be considered equal.
     * 
     * @param o1 the first object (or <code>null</code>).
     * @param o2 the second object (or <code>null</code>).
     * @return <code>true</code> if both objects are considered equal;
     *         <code>false</code> otherwise. 
     */
    public abstract boolean areEqual(T o1, T o2);

    /**
     * Compares the specified objects for order. Returns a negative integer, 
     * zero, or a positive integer as the first argument is less than, equal to,
     * or greater than the second.
     * 
     * @param o1 the first object.
     * @param o2 the second object.
     * @return a negative integer, zero, or a positive integer as the first
     *         argument is less than, equal to, or greater than the second.
     * @throws NullPointerException if any of the specified object is 
     *         <code>null</code>.
     */
    public abstract int compare(T o1, T o2);

    private static final long serialVersionUID = 6573304489580066811L;
}