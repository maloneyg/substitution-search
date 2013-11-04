import java.io.*;

public interface DebugMessage extends Serializable
{
    public static final DebugMessage ORIENTATION = new DebugMessage() {
        public String toString() { return " rejected for orientations"; } };
    public static final DebugMessage CROSS_OPEN = new DebugMessage() {
        public String toString() { return " rejected for crossing the open edge "; } };
    public static final DebugMessage CROSS_CLOSED = new DebugMessage() {
        public String toString() { return " rejected for crossing the closed edge "; } };
    public static final DebugMessage INCIDENT_OPEN = new DebugMessage() {
        public String toString() { return " rejected for being incident to an open edge"; } };
    public static final DebugMessage INCIDENT_CLOSED = new DebugMessage() {
        public String toString() { return " rejected for being incident to a closed edge"; } };
    public static final DebugMessage FOUND = new DebugMessage() {
        public String toString() { return "finished a puzzle"; } };
    public static final DebugMessage NONE_OR_PROTOTILE = new DebugMessage() {
        public String toString() { return " rejected because we're out, or because of incompatibility with "; } };
    public static final DebugMessage TOO_CLOSE = new DebugMessage() {
        public String toString() { return " is too close to point "; } };
    public static final DebugMessage NON_CONTAINMENT = new DebugMessage() {
        public String toString() { return " rejected for non-containment in the big tile"; } };
    public static final DebugMessage OVERLAP = new DebugMessage() {
        public String toString() { return " rejected for overlapping "; } };
    public static final DebugMessage PLACING = new DebugMessage() {
        public String toString() { return "placing "; } };
    public static final DebugMessage NONE = new DebugMessage() {
        public String toString() { return "no message"; } };

    public String toString();
}
