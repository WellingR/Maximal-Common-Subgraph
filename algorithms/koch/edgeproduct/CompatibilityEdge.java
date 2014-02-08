package algorithms.koch.edgeproduct;

import org.jgraph.graph.DefaultEdge;

public class CompatibilityEdge extends DefaultEdge{
	
	private static final long serialVersionUID = -7684973340178881312L;

	private final EdgeType type;
	
	protected CompatibilityEdge(EdgeType type){
		this.type = type;
	}

	public EdgeType getType() {
		return type;
	}
	
	public boolean isC_Edge(){
		return type == EdgeType.C_EDGE;
	}
	
	public boolean isD_Edge(){
		return type == EdgeType.D_EDGE;
	}
	
	public String toString(){
		return "" + type;
	}
	
}
