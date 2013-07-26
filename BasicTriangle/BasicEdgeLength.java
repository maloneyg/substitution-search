/**
*    This class implements an edge length.
*/


public class BasicEdgeLength implements AbstractEdgeLength {

    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        BasicEdgeLength l = (BasicEdgeLength) obj;
        return this == l;
    }

} // end of class BasicEdgeLength
