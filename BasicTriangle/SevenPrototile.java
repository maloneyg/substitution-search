/**
*    This class implements a prototile.
*    It is not located in space.  
*/


public class SevenPrototile extends AbstractPrototile<BasicAngle, BasicOrientation, BasicPoint, BasicEdgeLength, BasicEdge, BasicTriangle> {

    // Tile type.
    private enum TileType {
        SMALLRIGHT, //
        SMALLLEFT, //
        MEDIUMRIGHT, //
        MEDIUMLEFT, //
        LARGERIGHT, //
        LARGELEFT //
    }

    private TileType type;

    /*
    * Place the prototile in space.
    * p says where to place the root vertex.
    * a says how to orient it.
    * reflect says whether or not it's reflected.  
    */
    public BasicTriangle place(BasicPoint p, BasicAngle a) {
        return createBasicTriangle();
    }

} // end of class SevenPrototile
