// wrapper class for int[], because it doesn't implement
// equals() in any useful way.
public final class IntWrapper {

    private final int[] data;

    private IntWrapper(int[] x) {
        data = x;
    }

    public static IntWrapper createIntWrapper(int[] x) {
        return new IntWrapper(x);
    }

    // equals method.
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        IntWrapper y = (IntWrapper) obj;
        if (this.data.length != y.data.length) return false;
        for (int i = 0; i < data.length; i++) {
            if (y.data[i] != this.data[i])
                return false;
        }
        return true;
    }

    // hashCode method.
    public int hashCode() {
        int prime = 7;
        int result = 19;
        for (int i = 0; i < data.length; i++) {
            result = prime*result + data[i];
        }
        return result;
    }

} // end of IntWrapper class
