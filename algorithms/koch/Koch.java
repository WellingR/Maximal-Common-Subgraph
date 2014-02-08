package algorithms.koch;

import java.util.Set;

import org.jgrapht.Graph;

import algorithms.koch.edgeproduct.EdgeProduct;
import algorithms.koch.edgeproduct.EdgeProductGraph;

import shared.LabeledObject;

public class Koch {

	/**
	 * @param g1 the first supergraph
	 * @param g2 the second supergraph
	 * @param connected must be true if the subgraph has to be a connected subgraph
	 * @return the maximum common subgraph of g1 and g2
	 */
	public static <V extends LabeledObject<L>,E extends LabeledObject<L> ,L> Graph<V,E> maxCommonSubgraph(Graph<V,E> g1, Graph<V,E> g2, boolean connected){
		EdgeProductGraph<V,E,L> edgeProduct = new EdgeProductGraph<V,E,L>(g1, g2);
		Set<EdgeProduct<E>> result;
		if(connected){
			//Algorithm 3 is chosen, since it is faster than algorithm 5
			result = Algorithm3.largest_C_Clique_Init(edgeProduct);
		} else {
			result = Algorithm2.largestClique(edgeProduct);
		}
		if(result == null){ //interrupted
			return null;
		} else return edgeProduct.toSubgraph(result);
		
	}
	
	public static void interruptAllInstances(){
		Algorithm2.interruptAllInstances();
		Algorithm3.interruptAllInstances();
	}
}
