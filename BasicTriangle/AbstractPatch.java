/**
*    This interface represents the properties of a collection of triangles.
*/


public interface AbstractPatch<A extends AbstractAngle, L extends AbstractEdgeLength<A,L>, E extends AbstractEdge<A,L,E>, T extends AbstractTriangle, X extends AbstractPatch> {

    /*
    * construct a new patch that is the same as this one, with 
    * triangle t added to it.
    * This is what we do after compatible(t) returns true.
    */
    public X placeTriangle(T t);

    /*
    * Get the next edge.
    * Presumably, the word `next' implies some kind of order,
    * probably from shortest to longest, although other ideas 
    * are conceivable.  We really want to return the edge 
    * that is most difficult to cover, because that will lead
    * to a quicker rejection of the configuration, if it is
    * already uncompletable.
    */
    public E getNextEdge();

    /*
    * The big test.
    * Presumably this is where all of the work will happen.
    * We check and see if the triangle t fits in this patch.
    * This will involve applying many different tests to it.
    * Presumably there will be many other private functions
    * called in the execution of this one.  
    */
    public boolean compatible(T t);

} // end of interface AbstractPatch
