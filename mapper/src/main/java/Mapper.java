import java.util.Iterator;
import java.util.function.Function;

public class Mapper<In, Out> {
    public Iterable<Out> map(Iterable<In> iterable, Function<?super In, ?extends Out> action) {
        return new MapperIterable(iterable, action);
    }

    class MapperIterable implements Iterable<Out> {
        protected Function<?super In, ?extends Out> action;
        protected Iterable<In> iterable;

        protected MapperIterable(Iterable<In> iterable, Function<?super In, ?extends Out> action) {
            this.iterable = iterable;
            this.action = action;
        }

        @Override
        public Iterator<Out> iterator() {
            return new MapperIterator();
        }

        class MapperIterator implements Iterator<Out> {
            private Iterator<In> iterator;
            protected MapperIterator() {
                iterator = iterable.iterator();
            }
            public boolean hasNext() {
                return iterator.hasNext();
            }
            public Out next() {
                return action.apply(iterator.next());
            }
        }
    }
}
