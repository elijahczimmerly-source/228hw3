package edu.iastate.cs2280.hw3;

import java.util.AbstractSequentialList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.prefs.NodeChangeEvent;

/**
 * Implementation of the list interface based on linked nodes
 * that store multiple items per node.  Rules for adding and removing
 * elements ensure that each node (except possibly the last one)
 * is at least half full.
 */
public class StoutList<E extends Comparable<? super E>> extends AbstractSequentialList<E>
{
  /**
   * Default number of elements that may be stored in each node.
   */
  private static final int DEFAULT_NODESIZE = 4;
  
  /**
   * Number of elements that can be stored in each node.
   */
  private final int nodeSize;
  
  /**
   * Dummy node for head.  It should be private but set to public here only  
   * for grading purpose.  In practice, you should always make the head of a 
   * linked list a private instance variable.  
   */
  public Node head;
  
  /**
   * Dummy node for tail.
   */
  private Node tail;
  
  /**
   * Number of elements in the list.
   */
  private int size;
  
  /**
   * Constructs an empty list with the default node size.
   */
  public StoutList()
  {
    this(DEFAULT_NODESIZE);
  }

  /**
   * Constructs an empty list with the given node size.
   * @param nodeSize number of elements that may be stored in each node, must be 
   *   an even number
   */
  public StoutList(int nodeSize)
  {
    if (nodeSize <= 0 || nodeSize % 2 != 0) throw new IllegalArgumentException();
    
    // dummy nodes
    head = new Node();
    tail = new Node();
    head.next = tail;
    tail.previous = head;
    this.nodeSize = nodeSize;
  }
  
  /**
   * Constructor for grading only.  Fully implemented. 
   * @param head
   * @param tail
   * @param nodeSize
   * @param size
   */
  public StoutList(Node head, Node tail, int nodeSize, int size)
  {
	  this.head = head; 
	  this.tail = tail; 
	  this.nodeSize = nodeSize; 
	  this.size = size; 
  }

  @Override
  public int size()
  {
    return size;
  }
  
  //returns a NodeOffset object storing the node and offset for a given logical position
  private NodeOffset find(int target) {
	  if(target == size) return new NodeOffset(tail, 0);
	  Node node = head.next;
	  int index = 0;
	  while (index + node.count < target) {
		  index += node.count;
		  node = node.next;
	  }
	  int offset = target - index;
	  return new NodeOffset(node, offset);
  }
  
  @Override
  public boolean add(E item)
  {
    addHelper(tail.previous, tail.previous.count, item);
    return true;
  }

  @Override
  public void add(int pos, E item)
  {
	NodeOffset nodeAndOffset = find(pos);
	Node node = nodeAndOffset.node;
	int offset = nodeAndOffset.offset;
    addHelper(node, offset, item);
  }
  
  private void addHelper(Node node, int offset, E item) {
	  if(size == 0) {
		  Node newNode = new Node();
		  newNode.addItem(item);
	  }
	  else if(offset == 0) {
		  if(node != head && node.previous.count < nodeSize) node.previous.addItem(item);
		  else if(node == tail && node.previous.count == nodeSize) {
			  Node newNode = new Node();
			  newNode.addItem(item);
		  }
	  }
	  else if(node.count < nodeSize) {
		  node.addItem(offset, item);
	  }
	  else{
		  Node successor = new Node();
		  successor.next = node.next;
		  node.next.previous = successor;
		  node.next = successor;
		  successor.previous = node;
		  for(int i = nodeSize / 2; i < nodeSize; i++) {
			  successor.data[i - nodeSize / 2] = node.data[i];
		  }
		  if(offset <= nodeSize / 2) node.addItem(offset, item);
		  else successor.addItem(offset - nodeSize / 2, item);
	  }
	  size++;
  }

  @Override
  public E remove(int pos)
  {
    NodeOffset nodeAndOffset = find(pos);
    Node node = nodeAndOffset.node;
    int offset = nodeAndOffset.offset;
  	
  	E element = node.data[offset];
  	removeHelper(node, offset);
	return element;
  }
  
  private void removeHelper(Node node, int offset) {
	  boolean isLastNode = (node == tail.previous);
		if(isLastNode && node.count == 1) {
			node.previous.next = tail;
			tail.previous = node.previous;
		}
		else if(isLastNode || node.count > nodeSize / 2) {
			node.removeItem(offset);
		}
		else {
			Node successor = node.next;
			E element;
			if(successor.count > nodeSize / 2) {
				element = successor.data[0];
				successor.removeItem(0);
				node.addItem(element);
			}
			else {
				for(int i = 0; i < successor.count; i++) {
					element = successor.data[i];
					successor.removeItem(i);
					node.addItem(element);
				}
			}
		}
  }
  

  /**
   * Sort all elements in the stout list in the NON-DECREASING order. You may do the following. 
   * Traverse the list and copy its elements into an array, deleting every visited node along 
   * the way.  Then, sort the array by calling the insertionSort() method.  (Note that sorting 
   * efficiency is not a concern for this project.)  Finally, copy all elements from the array 
   * back to the stout list, creating new nodes for storage. After sorting, all nodes but 
   * (possibly) the last one must be full of elements.  
   *  
   * Comparator<E> must have been implemented for calling insertionSort().    
   */
  public void sort()
  {
	  E[] arr = moveToArray();
	  Comparator<E> comp = (a, b) -> a.compareTo(b);
	  insertionSort(arr, comp);
	  addFromArray(arr);
  }
  
  /**
   * Sort all elements in the stout list in the NON-INCREASING order. Call the bubbleSort()
   * method.  After sorting, all but (possibly) the last nodes must be filled with elements.  
   *  
   * Comparable<? super E> must be implemented for calling bubbleSort(). 
   */
  public void sortReverse() 
  {
	  E[] arr = moveToArray();
	  bubbleSort(arr);
	  addFromArray(arr);
  }
  
  private E[] moveToArray() {
	  E[] arr = (E[]) new Comparable[size];
	  int i = 0;
	  for(E item : this) arr[i++] = item;
	  head.next = tail;
	  tail.previous = head;
	  return arr;
  }
  
  private void addFromArray(E[] arr) {
	  int numNodes = Math.ceilDiv(size, nodeSize);
	  Node current = head;
	  int index = 0;
	  for(int i = 0; i < numNodes; i++) {
		  Node newNode = new Node();
		  current.next.previous = newNode;
		  newNode.next = current.next;
		  current.next = newNode;
		  newNode.previous = current;
		  current = newNode;
		  for(int j = 0; j < nodeSize; j++) {
			  if(index >= size) return;
			  current.addItem(arr[index]);
			  index++;
		  }
	  }
  }
  
  @Override
  public Iterator<E> iterator()
  {
	  return listIterator();
  }

  @Override
  public ListIterator<E> listIterator()
  {
    ListIterator<E> iterator = new StoutListIterator();
    return iterator;
  }

  @Override
  public ListIterator<E> listIterator(int index)
  {
    ListIterator<E> iterator = new StoutListIterator(index);
    return iterator;
  }
  
  /**
   * Returns a string representation of this list showing
   * the internal structure of the nodes.
   */
  public String toStringInternal()
  {
    return toStringInternal(listIterator());
  }

  /**
   * Returns a string representation of this list showing the internal
   * structure of the nodes and the position of the iterator.
   *
   * @param iter
   *            an iterator for this list
   */
  public String toStringInternal(ListIterator<E> iter) 
  {
      int count = 0;
      int position = -1;
      if (iter != null) {
          position = iter.nextIndex();
      }

      StringBuilder sb = new StringBuilder();
      sb.append('[');
      Node current = head.next;
      while (current != tail) {
          sb.append('(');
          E data = current.data[0];
          if (data == null) {
              sb.append("-");
          } else {
              if (position == count) {
                  sb.append("| ");
                  position = -1;
              }
              sb.append(data.toString());
              ++count;
          }

          for (int i = 1; i < nodeSize; ++i) {
             sb.append(", ");
              data = current.data[i];
              if (data == null) {
                  sb.append("-");
              } else {
                  if (position == count) {
                      sb.append("| ");
                      position = -1;
                  }
                  sb.append(data.toString());
                  ++count;

                  // iterator at end
                  if (position == size && count == size) {
                      sb.append(" |");
                      position = -1;
                  }
             }
          }
          sb.append(')');
          current = current.next;
          if (current != tail)
              sb.append(", ");
      }
      sb.append("]");
      return sb.toString();
  }
  
  //stores logical positions as a node and an offset
  private class NodeOffset{
	  public Node node;
	  public int offset;
	  
	  private NodeOffset(Node node, int offset){
		  this.node = node;
		  this.offset = offset;
	  }
  }

  /**
   * Node type for this list.  Each node holds a maximum
   * of nodeSize elements in an array.  Empty slots
   * are null.
   */
  private class Node
  {    /**
     * Array of actual data elements.
     */
    // Unchecked warning unavoidable.
    public E[] data = (E[]) new Comparable[nodeSize];
    
    /**
     * Link to next node.
     */
    public Node next;
    
    /**
     * Link to previous node;
     */
    public Node previous;
    
    /**
     * Index of the next available offset in this node, also 
     * equal to the number of elements in this node.
     */
    public int count;

    /**
     * Adds an item to this node at the first available offset.
     * Precondition: count < nodeSize
     * @param item element to be added
     */
    void addItem(E item)
    {
      if (count >= nodeSize)
      {
        throw new IllegalStateException();
      }
      data[count++] = item;
      //useful for debugging
      //      System.out.println("Added " + item.toString() + " at index " + count + " to node "  + Arrays.toString(data));
    }
  
    /**
     * Adds an item to this node at the indicated offset, shifting
     * elements to the right as necessary.
     * 
     * Precondition: count < nodeSize
     * @param offset array index at which to put the new element
     * @param item element to be added
     */
    void addItem(int offset, E item)
    {
      if (count >= nodeSize)
      {
    	  throw new IllegalStateException();
      }
      for (int i = count - 1; i >= offset; --i)
      {
        data[i + 1] = data[i];
      }
      ++count;
      data[offset] = item;
      //useful for debugging 
//      System.out.println("Added " + item.toString() + " at index " + offset + " to node: "  + Arrays.toString(data));
    }

    /**
     * Deletes an element from this node at the indicated offset, 
     * shifting elements left as necessary.
     * Precondition: 0 <= offset < count
     * @param offset
     */
    void removeItem(int offset)
    {
      E item = data[offset];
      for (int i = offset + 1; i < nodeSize; ++i)
      {
        data[i - 1] = data[i];
      }
      data[count - 1] = null;
      --count;
    }    
  }
 
  private class StoutListIterator implements ListIterator<E>
  {
	// constants you possibly use ...   
	
	// instance variables ...
	Node current;
	int offset;
	Node lastReturnedNode;
	int lastReturnedOffset;
	boolean nextCalled;
	int index;
	
    /**
     * Default constructor 
     */
    public StoutListIterator()
    {
    	nextCalled = false;
    	index = 0;
    	current = head.next;
    	offset = 0;
    }

    /**
     * Constructor finds node at a given position.
     * @param pos
     */
    public StoutListIterator(int pos)
    {
    	nextCalled = false;
    	index = pos;
    	NodeOffset nodeAndOffset = find(pos);
        current = nodeAndOffset.node;
        offset = nodeAndOffset.offset;
    }

    @Override
    public boolean hasNext()
    {
    	if(offset < current.count - 1) return true;
    	if(current.next == null) return false;
    	if (current.next.count > 0) return true;
    	return false;
    }

    @Override
    public E next()
    {
    	if (!hasNext()) throw new NoSuchElementException();
    	
    	lastReturnedOffset = offset;
		lastReturnedNode = current;
		
    	if(offset >= current.count - 1) {
    		offset = 0;
    		current = current.next;
    	}
    	else offset++;
    	
    	index++;
    	return lastReturnedNode.data[lastReturnedOffset];
    }

    @Override
    public void remove()
    {
    	removeHelper(lastReturnedNode, lastReturnedOffset);
    	lastReturnedNode = null;
    	lastReturnedOffset = -1;
    	if(nextCalled) index --;
    }

	@Override
	public boolean hasPrevious() {
		if(offset > 0) return true;
    	if(current.previous == null) return false;
    	if (current.previous.count > 0) return true;
    	return false;
	}

	@Override
	public E previous() {
		if (!hasPrevious()) throw new NoSuchElementException();
    	
    	lastReturnedOffset = offset;
		lastReturnedNode = current;
		
    	if(offset == 0) {
    		current = current.previous;
    		offset = current.count - 1;
    	}
    	else offset--;
    	
    	index--;
    	return lastReturnedNode.data[lastReturnedOffset];
	}

	@Override
	public int nextIndex() {
		return index + 1;
	}

	@Override
	public int previousIndex() {
		return index - 1;
	}

	@Override
	public void set(E e) {
		if(lastReturnedNode == null) throw new IllegalStateException();
		lastReturnedNode.data[lastReturnedOffset] = e;
	}

	@Override
	public void add(E e) {
		if(e == null) throw new NullPointerException();
		addHelper(current, offset, e);
		index++;
		lastReturnedNode = null;
		lastReturnedOffset  = -1;
		nextCalled = false;
		
		NodeOffset nodeAndOffset = find(index);
		this.current = nodeAndOffset.node;
		this.offset = nodeAndOffset.offset;
	}
    
    // Other methods you may want to add or override that could possibly facilitate 
    // other operations, for instance, addition, access to the previous element, etc.
    // 
    // ...
    // 
  }
  

  /**
   * Sort an array arr[] using the insertion sort algorithm in the NON-DECREASING order. 
   * @param arr   array storing elements from the list 
   * @param comp  comparator used in sorting 
   */
  private void insertionSort(E[] arr, Comparator<? super E> comp)
  {
	  for(int i = 1; i < arr.length; i++) {
		  E item = arr[i];
		  for(int j = 0; j < i; j++) {
			  if(comp.compare(item, arr[j]) >= 0) {
				  for(int k = i; k > j; k--) {
					  arr[k] = arr[k-1];
				  }
				  arr[j] = item;
			  }
		  }
	  }
  }
  
  /**
   * Sort arr[] using the bubble sort algorithm in the NON-INCREASING order. For a 
   * description of bubble sort please refer to Section 6.1 in the project description. 
   * You must use the compareTo() method from an implementation of the Comparable 
   * interface by the class E or ? super E. 
   * @param arr  array holding elements from the list
   */
  private void bubbleSort(E[] arr)
  {
	  int numPasses = size - 1;
	  int sortedIndex = -1;
	  for(int i = 0; i < numPasses; i++) {
		  for(int j = size - 1; j > sortedIndex + 1; i--) {
			  if(arr[j].compareTo(arr[j-1]) > 0) {
				  E temp = arr[j];
				  arr[j] = arr[j-1];
				  arr[j-1] = temp;
			  }
		  }
	  }
  }
 

}