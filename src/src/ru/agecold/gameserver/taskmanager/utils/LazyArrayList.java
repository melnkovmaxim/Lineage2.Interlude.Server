package ru.agecold.gameserver.taskmanager.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * ���������� ���������� ������� � ����������� <tt>List</tt>
 * �� ������-���������� ������. ������� ��������� ��� �������� �� �����������. �
 * �������� ��������� ��� ���������� ��������� ����� ���� <tt>null</tt>.
 * <p>
 * � �������� ��������� ��� �������� ������� <tt>LazyArrayList</tt> ��������
 * ��������� ������ ������� ��������� <tt>initialCapacity</tt>.
 * </p>
 * ��� ���������� ���������, � ������ ������������ �������, ������ �������������
 * �� <tt>capacity * 1.5</tt>
 * ��� �������� ��������, ������ �� �����������, ������ ����� ��������� �������
 * ������� ���������� �� ����� ����������.
 * <p>
 * ��� ������������� ��������� ������������ <tt>==</tt>, � ��
 * {@link Object#equals(Object) Object.equals(Object)}.
 * </p>
 *
 * @author G1ta0
 */
@SuppressWarnings("unchecked")
public class LazyArrayList<E> implements List<E>, RandomAccess, Cloneable, java.io.Serializable {

    private static final long serialVersionUID = 8683452581122892189L;

    @SuppressWarnings("rawtypes")
    private static class PoolableLazyArrayListFactory implements PoolableObjectFactory {

        @Override
        public Object makeObject() throws Exception {
            return new LazyArrayList();
        }

        @Override
        public void destroyObject(Object obj) throws Exception {
            ((LazyArrayList) obj).clear();
        }

        @Override
        public boolean validateObject(Object obj) {
            return true;
        }

        @Override
        public void activateObject(Object obj) throws Exception {

        }

        @Override
        public void passivateObject(Object obj) throws Exception {
            ((LazyArrayList) obj).clear();
        }
    }

    private static final int POOL_SIZE = Integer.parseInt(System.getProperty("lazyarraylist.poolsize", "-1"));
    private static final ObjectPool POOL = new GenericObjectPool(new PoolableLazyArrayListFactory(), POOL_SIZE, GenericObjectPool.WHEN_EXHAUSTED_GROW, 0L, -1);

    /**
     * �������� ������ LazyArrayList �� ����. � ������, ���� � ���� ���
     * ��������� ��������, ����� ������ �����.
     *
     * @return ������ LazyArrayList, ��������� � ����������� ��-���������
     * @see #recycle
     */
    public static <E> LazyArrayList<E> newInstance() {
        try {
            return (LazyArrayList<E>) POOL.borrowObject();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new LazyArrayList<E>();
    }

    /**
     * �������� ������ LazyArrayList ������� � ���.
     *
     * @param obj ������ LazyArrayList
     * @see #newInstance
     */
    public static <E> void recycle(LazyArrayList<E> obj) {
        try {
            POOL.returnObject(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final int L = 1 << 3;
    private static final int H = 1 << 10;

    protected transient Object[] elementData;
    protected transient int size = 0;
    protected transient int capacity = L;

    /**
     * ������� ����� ������, � ��������� �������� ����������� �������
     * <tt>initialCapacity</tt>
     *
     * @param initialCapacity ��������� ������ ������
     */
    public LazyArrayList(int initialCapacity) {
        if (initialCapacity < H) {
            while (capacity < initialCapacity) {
                capacity <<= 1;
            }
        } else {
            capacity = initialCapacity;
        }
    }

    public LazyArrayList() {
        this(8);
    }

    /**
     * �������� ������� � ������
     *
     * @param element �������, ������� ����������� � ������
     */
    @Override
    public boolean add(E element) {
        ensureCapacity(size + 1);
        elementData[size++] = element;

        return true;
    }

    /**
     * �������� ������� ������ � �������� �������
     *
     * @param index, ������� � ������� ���������� �������� �������
     * @param element �������, ������� ������� ���������� � �������� �������
     * @return ���������� ������� ������ � �������� �������
     * @throws IndexOutOfBoundsException � ������, ���� �������� ������� �������
     * �� ������� ����������� ������
     */
    @Override
    public E set(int index, E element) {
        rangeCheck(index);

        E e = null;
        e = (E) elementData[index];
        elementData[index] = element;

        return e;
    }

    /**
     * �������� ��������� ������� � ��������� ������� ������, ��� ���� ���
     * �������� � ���� ������� ���������� �������
     *
     * @param index �������, � ������� ���������� �������� ��������� �������
     * @param element ������� ��� �������
     * @throws IndexOutOfBoundsException � ������, ���� �������� ������� �������
     * �� ������� ����������� ������
     */
    @Override
    public void add(int index, E element) {
        rangeCheck(index);

        ensureCapacity(size + 1);
        System.arraycopy(elementData, index, elementData, index + 1, size - index);
        elementData[index] = element;
        size++;
    }

    /**
     * �������� �������� ��������� � ��������� ������� ������, ��� ���� ���
     * �������� � ���� ������� ���������� �������. ��� ��������� ���������
     * ��������� ������������ �����
     * {@link Collection#toArray() Collection.toArray()}
     *
     * @param index �������, � ������� ���������� �������� �������� ���������
     * ���������
     * @param c ���������, ������� �������� �������� ��� �������
     * @return true, ���� ������ ��� �������
     * @throws IndexOutOfBoundsException � ������, ���� �������� ������� �������
     * �� ������� ����������� ������
     */
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        rangeCheck(index);

        if (c == null || c.isEmpty()) {
            return false;
        }

        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacity(size + numNew);

        int numMoved = size - index;
        if (numMoved > 0) {
            System.arraycopy(elementData, index, elementData, index + numNew, numMoved);
        }
        System.arraycopy(a, 0, elementData, index, numNew);
        size += numNew;

        return true;
    }

    /**
     * ��������� ���������� ������ ���, ����� �� ���� ���������� ��� �������
     * <b>newSize</b> ���������
     *
     * @param newSize ����������� ����������� ������ �������
     */
    protected void ensureCapacity(int newSize) {
        if (newSize > capacity) {
            if (newSize < H) {
                while (capacity < newSize) {
                    capacity <<= 1;
                }
            } else {
                while (capacity < newSize) {
                    capacity = capacity * 3 / 2;
                }
            }

            Object[] elementDataResized = new Object[capacity];
            if (elementData != null) {
                System.arraycopy(elementData, 0, elementDataResized, 0, size);
            }
            elementData = elementDataResized;
        } else // ������������� �������
        if (elementData == null) {
            elementData = new Object[capacity];
        }
    }

    protected void rangeCheck(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    /**
     * ������� ������� � �������� �������. ��� ���� �������� �� ����������
     * �����, � � ��������� �������, ������ ����������, ���������� ������� �
     * ����� ������
     *
     * @param index �������, � ������� ������� ������� �������
     * @return �������, ��������� �� ��������� �������
     * @throws IndexOutOfBoundsException � ������, ���� �������� ������� �������
     * �� ������� ����������� ������
     */
    @Override
    public E remove(int index) {
        rangeCheck(index);

        E e = null;
        size--;
        e = (E) elementData[index];
        elementData[index] = elementData[size];
        elementData[size] = null;

        trim();

        return e;
    }

    /**
     * ������� �� ������ ������ ��������� �������, ����� ���� <tt>null</tt>, ���
     * �������� ������������� ������������ �������� <tt>==</tt>
     *
     * @param o ������, ������� ������� ������� �� ������
     * @return true, ���� ������ ��������� � ������
     */
    @Override
    public boolean remove(Object o) {
        if (size == 0) {
            return false;
        }

        int index = -1;
        for (int i = 0; i < size; i++) {
            if (elementData[i] == o) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            return false;
        }

        size--;
        elementData[index] = elementData[size];
        elementData[size] = null;

        trim();

        return true;
    }

    /**
     * ���������� true, ���� ������ ���������� � ������, � �������� ���������
     * ����� ���� <tt>null</tt>, ��� �������� ������������� ������������
     * �������� <tt>==</tt>
     *
     * @param o ������, ����������� �������� ����������� � ������
     * @return true, ���� ������ ��������� � ������
     */
    @Override
    public boolean contains(Object o) {
        if (size == 0) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            if (elementData[i] == o) {
                return true;
            }
        }

        return false;
    }

    /**
     * ���������� ������� ������� ��������� ������� � ������, � ��������
     * ��������� ����� ���� <tt>null</tt>, ��� �������� �������������
     * ������������ �������� <tt>==</tt>, ���� ������ �� ������, ���������� -1
     *
     * @param o ������ ��� ������ � ������
     * @return �������, � ������� ��������� ������ � ������, ���� -1, ����
     * ������ �� ������
     */
    @Override
    public int indexOf(Object o) {
        if (size == 0) {
            return -1;
        }

        int index = -1;
        for (int i = 0; i < size; i++) {
            if (elementData[i] == o) {
                index = i;
                break;
            }
        }

        return index;
    }

    /**
     * ���������� ������� ���������� ��������� ������� � ������, � ��������
     * ��������� ����� ���� <tt>null</tt>, ��� �������� �������������
     * ������������ �������� <tt>==</tt>, ���� ������ �� ������, ���������� -1
     *
     * @param o ������ ��� ������ � ������
     * @return ��������� �������, � ������� ��������� ������ � ������, ���� -1,
     * ���� ������ �� ������
     */
    @Override
    public int lastIndexOf(Object o) {
        if (size == 0) {
            return -1;
        }

        int index = -1;
        for (int i = 0; i < size; i++) {
            if (elementData[i] == o) {
                index = i;
            }
        }

        return index;
    }

    protected void trim() {

    }

    /**
     * �������� ������� ������ � �������� �������
     *
     * @param index ������� ������, ������� �� ������� ���������� ��������
     * @return ���������� ������� ������ � �������� �������
     * @throws IndexOutOfBoundsException � ������, ���� �������� ������� �������
     * �� ������� ����������� ������
     */
    @Override
    public E get(int index) {
        rangeCheck(index);

        return (E) elementData[index];
    }

    /**
     * �������� ����� ������
     *
     * @return ������, � ����������� � ������� ��������� ��������
     */
    @Override
    public Object clone() {
        LazyArrayList<E> clone = new LazyArrayList<E>();
        if (size > 0) {
            clone.capacity = capacity;
            clone.elementData = new Object[elementData.length];
            System.arraycopy(elementData, 0, clone.elementData, 0, size);
        }
        return clone;
    }

    /**
     * �������� ������
     */
    @Override
    public void clear() {
        if (size == 0) {
            return;
        }

        for (int i = 0; i < size; i++) {
            elementData[i] = null;
        }

        size = 0;
        trim();
    }

    /**
     * ���������� ���������� ��������� � ������
     *
     * @return ���������� ��������� � ������
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * ���������� true, ���� ������ �� �������� ���������
     *
     * @return true, ���� ������ ����
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * ���������� ������ ����������� ������� ������
     *
     * @return ������ ����������� �������
     */
    public int capacity() {
        return capacity;
    }

    /**
     * �������� ��� �������� ��������� � ������. ��� ��������� ���������
     * ��������� ������������ �����
     * {@link Collection#toArray() Collection.toArray()}
     *
     * @param c ���������, ������� �������� �������� ��� ����������
     * @return true, ���� ������ ��� �������
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        if (c == null || c.isEmpty()) {
            return false;
        }
        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacity(size + numNew);
        System.arraycopy(a, 0, elementData, size, numNew);
        size += numNew;
        return true;
    }

    /**
     * ���������, ���������� �� ��� �������� ��������� � ������
     *
     * @param c ���������, ������� �������� �������� ��� �������� ���������� �
     * ������
     * @return true, ���� ������ �������� ��� �������� ���������
     * @see #contains(Object)
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        if (c == null) {
            return false;
        }
        if (c.isEmpty()) {
            return true;
        }
        Iterator<?> e = c.iterator();
        while (e.hasNext()) {
            if (!contains(e.next())) {
                return false;
            }
        }
        return true;
    }

    /**
     * ������� �� ������ ��� ��������, ������� �� ���������� � ��������
     * ���������, ��� �������� ���������� �������� � ��������� ������������
     * ����� ���������
     * {@link Collection#contains(Object) Collection.contains(Object)}
     *
     * @param c ���������, ������� �������� ��������, ������� ����������
     * �������� � ������
     * @return true, ���� ������ ��� �������
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        if (c == null) {
            return false;
        }
        boolean modified = false;
        Iterator<E> e = iterator();
        while (e.hasNext()) {
            if (!c.contains(e.next())) {
                e.remove();
                modified = true;
            }
        }
        return modified;
    }

    /**
     * ������� �� ������ ��� ��������, ������� ���������� � �������� ���������,
     * ��� �������� ���������� �������� � ��������� ������������ ����� ���������
     * {@link Collection#contains(Object) Collection.contains(Object)}
     *
     * @param c ���������, ������� �������� �������� ��� �������� �� ������
     * @return true, ���� ������ ��� �������
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        if (c == null || c.isEmpty()) {
            return false;
        }
        boolean modified = false;
        Iterator<?> e = iterator();
        while (e.hasNext()) {
            if (c.contains(e.next())) {
                e.remove();
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public Object[] toArray() {
        Object[] r = new Object[size];
        if (size > 0) {
            System.arraycopy(elementData, 0, r, 0, size);
        }
        return r;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        T[] r = a.length >= size ? a : (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        if (size > 0) {
            System.arraycopy(elementData, 0, r, 0, size);
        }
        if (r.length > size) {
            r[size] = null;
        }
        return r;
    }

    @Override
    public Iterator<E> iterator() {
        return new LazyItr();
    }

    @Override
    public ListIterator<E> listIterator() {
        return new LazyListItr(0);
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return new LazyListItr(index);
    }

    private class LazyItr implements Iterator<E> {

        int cursor = 0;
        int lastRet = -1;

        @Override
        public boolean hasNext() {
            return cursor < size();
        }

        @Override
        public E next() {
            E next = get(cursor);
            lastRet = cursor++;
            return next;
        }

        @Override
        public void remove() {
            if (lastRet == -1) {
                throw new IllegalStateException();
            }
            LazyArrayList.this.remove(lastRet);
            if (lastRet < cursor) {
                cursor--;
            }
            lastRet = -1;
        }
    }

    private class LazyListItr extends LazyItr implements ListIterator<E> {

        LazyListItr(int index) {
            cursor = index;
        }

        @Override
        public boolean hasPrevious() {
            return cursor > 0;
        }

        @Override
        public E previous() {
            int i = cursor - 1;
            E previous = get(i);
            lastRet = cursor = i;
            return previous;
        }

        @Override
        public int nextIndex() {
            return cursor;
        }

        @Override
        public int previousIndex() {
            return cursor - 1;
        }

        @Override
        public void set(E e) {
            if (lastRet == -1) {
                throw new IllegalStateException();
            }
            LazyArrayList.this.set(lastRet, e);
        }

        @Override
        public void add(E e) {
            LazyArrayList.this.add(cursor++, e);
            lastRet = -1;
        }
    }

    @Override
    public String toString() {
        if (size == 0) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < size; i++) {
            Object e = elementData[i];
            sb.append(e == this ? "this" : e);

            if (i == size - 1) {
                sb.append(']');
            } else {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    /**
     * ����� �� ����������
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }
}
