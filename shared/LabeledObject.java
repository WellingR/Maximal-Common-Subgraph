package shared;

/**
 * An object that can have any other object as a label
 * Can be used as a Labeled edge or vertex in a graph.
 * Two instances of LabeledObject with the same label are not equal.
 */
public interface LabeledObject<L> {

    /**
     * Returns the label of this object
     */
    public L getLabel();
 
}
