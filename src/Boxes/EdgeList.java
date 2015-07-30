package Boxes;

import java.util.ArrayList;

/**
 * This class supposed to store list of edges, outgoing from
 * an event. The reverse link to Event isn't stored.
 * //TODO maybe, we need to make it back inner class of EFG
 * Created by listvin on 7/29/15.
 */
class EdgeList extends ArrayList<Edge> {
    /**Counter for iterating through list.
     * Edges in this list situated to the "left" from
     * firstToExplore are ticked as explored, or Events
     * they are leading to are explored.
     * Access is out-of-the-world=(*/
    int firstToExplore = 0;
    /**Default empty constructor inherited from ArrayList of Edges*/
    EdgeList(){ super(); }
}
