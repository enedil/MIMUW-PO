import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class UniversalMap implements Iterable<?extends T> {
    protected Function<? extends T, ? super T> f;

    public UniversalMap(Function<? extends T, ? super T> f) {
        this.f = f;
    }

    @Override
    public Iterator<? extends T> iterator() {
        return null;
    }


    private class MapIterator implements Iterator<? super T> {
        private Iterator<? extends T> iterator;

        public MapIterator(Iterator<? extends T> iterator) {
            this.iterator = iterator;
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public Integer next() {
            if (this.hasNext()) {
                return f(this.iterator.remove());
            }
            throw new NoSuchElementException();
        }

    }
}
