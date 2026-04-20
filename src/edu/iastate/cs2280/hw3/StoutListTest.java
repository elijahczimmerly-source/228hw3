package edu.iastate.cs2280.hw3;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * JUnit 5 test suite for StoutList.
 * Focuses on the "Letter of the Instructions" from HW3.pdf.
 */
public class StoutListTest {

    private StoutList<String> list;
    private final int M = 4; // Node size

    @BeforeEach
    void setUp() {
        list = new StoutList<>(M);
    }

    @Test
    @DisplayName("Section 2: Constructor and Basic Add Logic")
    void testBasicAdd() {
        assertThrows(IllegalArgumentException.class, () -> new StoutList<>(3), "Odd nodeSize must throw IAE");
        
        list.add("A");
        list.add("B");
        assertEquals(2, list.size());
        assertEquals("[(A, B, -, -)]", list.toStringInternal(), "Initial adds should fill first node");
        
        assertThrows(NullPointerException.class, () -> list.add(null), "Adding null must throw NPE");
    }

    @Test
    @DisplayName("Section 5.1: Splitting Rule (Case 4)")
    void testSplittingRule() {
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");
        System.out.println(list.toStringInternal());
        assertEquals("[(A, B, C, D)]", list.toStringInternal());

        // Adding 'E' to a full node should split it
        // Last M/2 (C, D) move to new node. E added to new node at offset 0.
        list.add("E"); 
        assertEquals(5, list.size());
        assertEquals("[(A, B, -, -), (C, D, E, -)]", list.toStringInternal(), 
            "Split should move M/2 elements to new node");
    }

    @Test
    @DisplayName("Section 5.1: Rule 2 (Offset 0 logic)")
    void testAddAtOffsetZero() {
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");
        list.add("F"); // Causes split: [(A,B), (C,D,F)]
        
        // Add 'E' at index 2 (Offset 0 of Node 1)
        // Rule 2a: If predecessor (Node 0) has space, put it there.
        list.add(2, "E"); 
        assertEquals("[(A, B, E, -), (C, D, F, -)]", list.toStringInternal(), 
            "Add at offset 0 should prefer predecessor if space exists");
    }

    @Test
    @DisplayName("Section 5.2: Removal Rule (Mini-merge/Stealing)")
    void testMiniMerge() {
        // Setup: [(A, B, C, D), (E, F, G, -)]
        for (String s : new String[]{"A", "B", "C", "D", "E", "F", "G"}) list.add(s);
        
        // Remove 'B' from Node 0. Node 0 count becomes 3 (>= M/2). No merge.
        list.remove(1);
        assertEquals("[(A, C, D, -), (E, F, G, -)]", list.toStringInternal());

        // Remove 'A' from Node 0. Node 0 count becomes 1 (< M/2). 
        // Successor has 3 elements (> M/2), so steal 'E' from successor.
        list.remove(0);
        assertEquals("[(C, D, E, -), (F, G, -, -)]", list.toStringInternal(), 
            "Should steal from successor if it has more than M/2 elements");
    }

    @Test
    @DisplayName("Section 5.2: Removal Rule (Full Merge)")
    void testFullMerge() {
        // Setup: [(A, B, -, -), (C, D, -, -)]
        list.add("A"); list.add("B"); 
        list.add("C"); list.add("D"); // Triggers split: [(A,B), (C,D)]
        
        // Remove 'A'. Node 0 count = 1 (< M/2).
        // Successor Node 1 has 2 elements (== M/2).
        // Perform Full Merge.
        list.remove(0);
        assertEquals("[(B, C, D, -)]", list.toStringInternal(), 
            "Should merge nodes if successor has M/2 or fewer elements");
    }

    @Test
    @DisplayName("Section 4: ListIterator Forward and Backward")
    void testIteratorNavigation() {
        list.add("A"); list.add("B"); list.add("C");
        ListIterator<String> iter = list.listIterator();

        assertTrue(iter.hasNext());
        assertEquals(0, iter.nextIndex());
        assertEquals("A", iter.next());
        assertEquals("B", iter.next());
        
        assertTrue(iter.hasPrevious());
        assertEquals("B", iter.previous());
        assertEquals(1, iter.nextIndex());
    }

    @Test
    @DisplayName("Section 4: ListIterator add/remove/set state rules")
    void testIteratorState() {
        list.add("A");
        ListIterator<String> iter = list.listIterator();
        
        assertThrows(IllegalStateException.class, iter::remove, "Remove before next() must fail");
        
        iter.next();
        iter.set("Z");
        assertEquals("Z", list.get(0));
        
        iter.add("Y"); // Cursor now between Z and Y
        assertThrows(IllegalStateException.class, iter::remove, "Remove after add() must fail");
        assertThrows(IllegalStateException.class, () -> iter.set("X"), "Set after add() must fail");
    }

    @Test
    @DisplayName("Section 6: Sorting - Nodes must be full")
    void testSortingFullNodes() {
        list.add("D"); list.add("C"); list.add("B"); list.add("A");
        list.add("E"); // List currently has splits/gaps
        
        list.sort(); // Natural order: A, B, C, D, E
        
        // Section 6: "After sorting, all nodes but (possibly) the last one must be full"
        assertEquals("[(A, B, C, D), (E, -, -, -)]", list.toStringInternal(), 
            "Sorting must rebuild list with full nodes");
    }

    @Test
    @DisplayName("Section 6.1: Reverse Sorting")
    void testReverseSorting() {
        list.add("A"); list.add("B"); list.add("C"); list.add("D");
        
        list.sortReverse(); // Bubble sort: D, C, B, A
        
        assertEquals("D", list.get(0));
        assertEquals("A", list.get(3));
        assertEquals("[(D, C, B, A)]", list.toStringInternal());
    }
}
