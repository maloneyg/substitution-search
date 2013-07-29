/**
*    This class implements an edge length.
*/


public enum BasicEdgeLength implements AbstractEdgeLength<BasicPoint> {

    SHORT, MEDIUM, LONG;

//    public boolean equals(Object obj) {
//        if (obj == null || getClass() != obj.getClass())
//            return false;
//        BasicEdgeLength l = (BasicEdgeLength) obj;
//        return this == l;
//    }

    public BasicPoint getAsVector() {
        int[] starter = {1, 0, 0, 0, 0, 0};
        BasicPoint p = BasicPoint.createBasicPoint(starter);
        switch (this) {
            case SHORT:
                return p;
            case MEDIUM:
                return p.inflate();
            case LONG:
                return p.inflate().inflate().subtract(p);
            default:
                return p;
        }
    }

} // end of class BasicEdgeLength
