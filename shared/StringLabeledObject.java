package shared;

import java.util.Iterator;
import java.util.Set;



/**
 *
 * @author Ruud
 */
public class StringLabeledObject implements LabeledObject<String>{
    
    private final String label;

    public StringLabeledObject(String label){
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String toString(){
        return label;
    }
    
    public static StringLabeledObject getRandomStringLabeledObject(Set<String> alphabet){
		if(alphabet == null || alphabet.isEmpty()){
			throw new RuntimeException("Invalid alphabet");
		}
		int i = (int) (Math.random() * alphabet.size());
		Iterator<String> it = alphabet.iterator();
		for(int c=0; c<i;c++){
			it.next();
		}
		return new StringLabeledObject(it.next());
    }

}
