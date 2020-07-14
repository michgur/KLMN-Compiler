package util;

public class Pair<K, V> {
    private K k;
    private V v;

    public Pair() {}
    protected Pair(K key, V value) {
        k = key;
        v = value;
    }

    public static<K, V> Pair<K, V> of(K key, V value) { return new Pair<>(key, value); }

    public K getKey() { return k; }
    public void setKey(K key) { k = key; }

    public V getValue() { return v; }
    public void setValue(V value) { v = value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair<?, ?> pair = (Pair<?, ?>) o;
        return (k != null ? k.equals(pair.k) : pair.k == null) &&
                (v != null ? v.equals(pair.v) : pair.v == null);
    }

    @Override
    public int hashCode() {
        int result = k != null ? k.hashCode() : 0;
        result = 31 * result + (v != null ? v.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() { return '(' + k.toString() + ", " + v.toString() + ')'; }
}
