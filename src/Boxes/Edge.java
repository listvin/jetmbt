package Boxes;

/**
 * Package-private, supposed to be accessed only by EFG.
 * //TODO maybe, we need to make it back inner class of EFG
 * Created by listvin on 7/29/15.
 */
class Edge extends Tickable{
    /**This is target node.*/
    public final Event destination;
    /**@param destination target node.*/
    Edge(Event destination) {
        this.destination = destination;
    }
}
