package com.bigcustard.util;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class WatchableList<E> extends Notifier<WatchableList<E>> implements Iterable<E> {
    private final List<E> list;

    public WatchableList(List<E> list) {
        this.list = list;
    }

    public boolean add(E e) {
        list.add(0, e);
        notify(this);
        return true;
    }

    public boolean remove(Object o) {
        boolean remove = list.remove(o);
        notify(this);
        return remove;
    }

    public Stream<E> stream() {
        return list.stream();
    }

    @Override
    public void watch(Consumer<WatchableList<E>> listener) {
        super.watch(listener);
        notify(this);
    }

    @Override
    public Iterator<E> iterator() {
        return list.iterator();
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        list.forEach(action);
    }

    @Override
    public Spliterator<E> spliterator() {
        return list.spliterator();
    }
}