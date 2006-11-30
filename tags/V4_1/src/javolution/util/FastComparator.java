package javolution.util;

import javolution.Javolution;
import javolution.lang.Configurable;
import javolution.text.Text;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;
import j2me.io.Serializable;
import j2me.lang.CharSequence;
import j2me.lang.Comparable;
import j2me.util.Comparator;

/**
 * <p> This class represents a comparator to be used for equality as well as 
 *     for ordering; instances of this class provide a hashcode function 
 *     consistent with equal (if two objects {@link #areEqual
 *     are equal}, they have the same {@link #hashCodeOf hashcode}),
 *     equality with <code>null</code> values is supported.</p>
 *     
 * <p> {@link FastComparator} can be employed with {@link FastMap} (e.g. custom 
 *     key comparators for identity maps, value retrieval using keys of a 
 *     different class that the map keys) or with {@link FastCollection}
 *     classes.</p>
 *     
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.4, September 20, 2005
 */
public abstract class FastComparator/*<T>*/implements Comparator/*<T>*/,
        Serializable {

    /**
     * Holds the default XML representation for FastComparator instances
     * (format ensures unicity of predefined comparator).
     */
    protected static final XMLFormat/*<FastComparator>*/ XML = new XMLFormat(
            Javolution.j2meGetClass("javolution.util.FastComparator")) {

        public Object newInstance(Class cls, javolution.xml.XMLFormat.InputElement xml) throws XMLStreamException {
            if (cls == FastComparator.DEFAULT.getClass())
                return FastComparator.DEFAULT;
            if (cls == FastComparator.DIRECT.getClass())
                return FastComparator.DIRECT;
            if (cls == FastComparator.IDENTITY.getClass())
                return FastComparator.IDENTITY;
            if (cls == FastComparator.LEXICAL.getClass())
                return FastComparator.LEXICAL;
            if (cls == FastComparator.REHASH.getClass())
                return FastComparator.REHASH;
            return super.newInstance(cls, xml);
        }

        public void read(InputElement xml, Object obj) throws XMLStreamException {
            // Do nothing.
        }

        public void write(Object obj, OutputElement xml) throws XMLStreamException {
            // Do nothing.
        }
    };

    /**
     * Indicates if the system hash code should be rehashed 
     * (see <a href="{@docRoot}/overview-summary.html#configuration">
     * Javolution Configuration</a> for details).
     */
    public static final Configurable/*<Boolean>*/ REHASH_SYSTEM_HASHCODE 
         = new Configurable(isPoorSystemHash()) {
        protected void notifyChange() {
            _Rehash = ((Boolean)get()).booleanValue();
        }
    };
    static boolean _Rehash 
         = ((Boolean)REHASH_SYSTEM_HASHCODE.get()).booleanValue();

    /**
     * Holds the default object comparator; rehash is performed if the 
     * system hash code (platform dependent) is not evenly distributed.
     * @see <a href="{@docRoot}/overview-summary.html#configuration">
     *      Javolution Configuration</a> 
     */
    public static final FastComparator DEFAULT = new Default();

    private static final class Default extends FastComparator {

        public int hashCodeOf(Object obj) {
            return (_Rehash ? REHASH.hashCodeOf(obj) : obj.hashCode());
        }

        public boolean areEqual(Object o1, Object o2) {
            return (o1 == null) ? (o2 == null) : (o1 == o2) || o1.equals(o2);
        }

        public int compare(Object o1, Object o2) {
            return ((Comparable) o1).compareTo(o2);
        }

        public String toString() {
            return "default";
        }

    };

    /**
     * Holds the direct object comparator; no rehash is performed.
     * Two objects o1 and o2 are considered {@link #areEqual equal} if and
     * only if <code>o1.equals(o2)</code>. The {@link #compare} method 
     * throws {@link ClassCastException} if the specified objects are not
     * {@link Comparable}. 
     */
    public static final FastComparator DIRECT = new Direct();

    private static final class Direct extends FastComparator {
        public int hashCodeOf(Object obj) {
            return obj.hashCode();
        }

        public boolean areEqual(Object o1, Object o2) {
            return (o1 == null) ? (o2 == null) : (o1 == o2) || o1.equals(o2);
        }

        public int compare(Object o1, Object o2) {
            return ((Comparable) o1).compareTo(o2);
        }

        public String toString() {
            return "direct";
        }

    };

    /**
     * Holds the comparator for objects with uneven hash distribution; objects
     * hashcodes are rehashed. Two objects o1 and o2 are considered 
     * {@link #areEqual equal} if and only if <code>o1.equals(o2)</code>.
     * The {@link #compare} method throws {@link ClassCastException} if the
     * specified objects are not {@link Comparable}.
     */
    public static final FastComparator REHASH = new Rehash();

    private static final class Rehash extends FastComparator {
        public int hashCodeOf(Object obj) {
            // Formula identical <code>java.util.HashMap</code> to ensures
            // similar behavior for ill-conditioned hashcode keys. 
            int h = obj.hashCode();
            h += ~(h << 9);
            h ^= (h >>> 14);
            h += (h << 4);
            return h ^ (h >>> 10);
        }

        public boolean areEqual(Object o1, Object o2) {
            return (o1 == null) ? (o2 == null) : (o1 == o2) || o1.equals(o2);
        }

        public int compare(Object o1, Object o2) {
            return ((Comparable) o1).compareTo(o2);
        }

        public String toString() {
            return "rehash";
        }

    };

    /**
     * Holds the identity comparator; poorly distributed system hashcodes are
     * rehashed. Two objects o1 and o2 are considered {@link #areEqual equal} 
     * if and only if <code>(o1 == o2)</code>. The {@link #compare} method 
     * throws {@link ClassCastException} if the specified objects are not
     * {@link Comparable}.
     */
    public static final FastComparator IDENTITY = new Identity();

    private static final class Identity extends FastComparator {
        public int hashCodeOf(Object obj) {
            int h = System.identityHashCode(obj);
            if (!_Rehash)
                return h;
            h += ~(h << 9);
            h ^= (h >>> 14);
            h += (h << 4);
            return h ^ (h >>> 10);

        }

        public boolean areEqual(Object o1, Object o2) {
            return o1 == o2;
        }

        public int compare(Object o1, Object o2) {
            return ((Comparable) o1).compareTo(o2);
        }

        public String toString() {
            return "identity";
        }
    };

    /**
     * Holds a lexicographic comparator for any {@link CharSequence} or 
     * {@link String} instances. 
     * Two objects are considered {@link #areEqual equal} if and only if they 
     * represents the same character sequence). The hashcode is calculated
     * using the following formula (same as for <code>java.lang.String</code>):
     * <code>s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]</code>
     */
    public static final FastComparator LEXICAL = new Lexical();

    private static final class Lexical extends FastComparator {

        public int hashCodeOf(Object obj) {
            if ((obj instanceof String) || (obj instanceof Text))
                return obj.hashCode();
            CharSequence chars = (CharSequence) obj;
            int h = 0;
            final int length = chars.length();
            for (int i = 0; i < length;) {
                h = 31 * h + chars.charAt(i++);
            }
            return h;
        }

        public boolean areEqual(Object o1, Object o2) {
            if ((o1 instanceof String) && (o2 instanceof String))
                return o1.equals(o2);
            if ((o1 instanceof CharSequence) && (o2 instanceof String)) {
                final CharSequence csq = (CharSequence) o1;
                final String str = (String) o2;
                final int length = str.length();
                if (csq.length() != length)
                    return false;
                for (int i = 0; i < length;) {
                    if (str.charAt(i) != csq.charAt(i++))
                        return false;
                }
                return true;
            }
            if ((o1 instanceof String) && (o2 instanceof CharSequence)) {
                final CharSequence csq = (CharSequence) o2;
                final String str = (String) o1;
                final int length = str.length();
                if (csq.length() != length)
                    return false;
                for (int i = 0; i < length;) {
                    if (str.charAt(i) != csq.charAt(i++))
                        return false;
                }
                return true;
            }
            if ((o1 == null) || (o2 == null)) 
                return o1 == o2;
            final CharSequence csq1 = (CharSequence) o1;
            final CharSequence csq2 = (CharSequence) o2;
            final int length = csq1.length();
            if (csq2.length() != length)
                return false;
            for (int i = 0; i < length;) {
                if (csq1.charAt(i) != csq2.charAt(i++))
                    return false;
            }
            return true;
        }

        public int compare(Object left, Object right) {
            if (left instanceof String) {
                if (right instanceof String)
                    return ((String) left).compareTo((String) right);
                // Right must be a CharSequence.
                String seq1 = (String) left;
                CharSequence seq2 = (CharSequence) right;
                int i = 0;
                int n = Math.min(seq1.length(), seq2.length());
                while (n-- != 0) {
                    char c1 = seq1.charAt(i);
                    char c2 = seq2.charAt(i++);
                    if (c1 != c2) {
                        return c1 - c2;
                    }
                }
                return seq1.length() - seq2.length();
            }
            if (right instanceof String)
                return -compare(right, left);

            // Both are CharSequence.
            CharSequence seq1 = (CharSequence) left;
            CharSequence seq2 = (CharSequence) right;
            int i = 0;
            int n = Math.min(seq1.length(), seq2.length());
            while (n-- != 0) {
                char c1 = seq1.charAt(i);
                char c2 = seq2.charAt(i++);
                if (c1 != c2) {
                    return c1 - c2;
                }
            }
            return seq1.length() - seq2.length();
        }

        public String toString() {
            return "lexical";
        }

    };

    /**
     * Returns the hash code for the specified object (consistent with 
     * {@link #areEqual}). Two objects considered {@link #areEqual equal} have 
     * the same hash code. 
     * 
     * @param  obj the object to return the hashcode for.
     * @return the hashcode for the specified object.
     * @throws NullPointerException if the specified object is 
     *         <code>null</code>.
     */
    public abstract int hashCodeOf(Object/*{T}*/obj);

    /**
     * Indicates if the specified objects can be considered equal.
     * 
     * @param o1 the first object (or <code>null</code>).
     * @param o2 the second object (or <code>null</code>).
     * @return <code>true</code> if both objects are considered equal;
     *         <code>false</code> otherwise. 
     */
    public abstract boolean areEqual(Object/*{T}*/o1, Object/*{T}*/o2);

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
    public abstract int compare(Object/*{T}*/o1, Object/*{T}*/o2);

    /**
     * Test the system hash code.
     * 
     * @return <code>true</code> if the system hash code is not evenly 
     *         distributed; <code>false<code> otherwise.
     */
    private static Boolean isPoorSystemHash() {
        boolean[] dist = new boolean[32]; // Length power of 2.
        for (int i = 0; i < dist.length; i++) {
            dist[new Object().hashCode() & (dist.length - 1)] = true;
        }
        int holes = 0;
        for (int i = 0; i < dist.length; i++) {
            if (!dist[i])
                holes++; // Count holes.
        }
        return new Boolean(holes > (dist.length >> 1));
    }
}