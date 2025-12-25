package AutoDriveEditor.Classes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A generic circular list implementation that allows traversal in a circular manner.
 *
 * @param <T> the type of elements in this list
 */
@SuppressWarnings("unused")
public class CircularList<T> implements Iterable<T> {
    private final List<T> list;
    private int currentIndex = 0;

    /**
     * Constructs an empty CircularList.
     */
    public CircularList() {
        this.list = new ArrayList<>();
    }

    /**
     * Constructs a CircularList with the specified list of elements.
     *
     * @param list the list of elements to initialize the CircularList with
     */
    public CircularList(List<T> list) {
        this.list = new ArrayList<>(list);
    }

    /**
     * Adds an element to the CircularList.
     *
     * @param element the element to be added
     */
    public void add(T element) {
        list.add(element);
    }

    /**
     * Adds an element at a specific index in the CircularList.
     *
     * @param index   the index at which to add the element
     * @param element the element to be added
     */
    public void add(T element, int index) {
        if (index < 0 || index > list.size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + list.size());
        }
        list.add(index, element);
    }

    /**
     * Removes an element from the CircularList.
     *
     * @param element the element to be removed
     */
    public void remove(T element) {
        if (list.isEmpty()) {
            return;
        }
        int indexToRemove = list.indexOf(element);
        if (indexToRemove != -1) {
            list.remove(indexToRemove);
            if (currentIndex >= list.size()) {
                currentIndex = 0;
            }
        }
    }

    /**
     * Returns the current element in the CircularList.
     *
     * @return the current element, or null if the list is empty
     */
    public T get() {
        if (list.isEmpty()) {
            return null;
        }
        return list.get(currentIndex);
    }

    /**
     * Returns the next element in the CircularList and advances the current index.
     *
     * @return the next element, or null if the list is empty
     */
    public T next() {
        if (list.isEmpty()) {
            return null;
        }
//        T element = list.get(currentIndex);
//        currentIndex = (currentIndex + 1) % list.size();
//        return element;
        currentIndex = (currentIndex + 1) % list.size();
        return list.get(currentIndex);
    }

    /**
     * Returns the previous element in the CircularList and moves the current index backwards.
     *
     * @return the previous element, or null if the list is empty
     */
    public T previous() {
        if (list.isEmpty()) {
            return null;
        }
        currentIndex = (currentIndex - 1 + list.size()) % list.size();
        return list.get(currentIndex);
    }

    /**
     * Resets the current index to the beginning of the CircularList.
     */
    public void reset() {
        currentIndex = 0;
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    /**
     * Checks if the state list is empty.
     *
     * @return true if the state list is empty, false otherwise
     */
    public boolean isEmpty() {
        return list.isEmpty();
    }

    /**
     * Returns the size of the state list.
     *
     * @return the number of states in the list
     */
    public int getSize() {
        return list.size();
    }

    /**
     * Returns the current position in the state list.
     *
     * @return the index position of the current state
     */
    public int getCurrentIndex() { return currentIndex; }

    /**
     * Set the current index position of the state list.
     */
    public void setCurrentIndex(int index) {
        currentIndex = index;
    }
}
