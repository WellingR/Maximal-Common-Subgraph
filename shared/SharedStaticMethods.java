package shared;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.graph.Multigraph;

public class SharedStaticMethods {
	
	/**
	 * Searches for all vertices connected to 'vertex' in graph 'g'
	 * @param g the graph that should be searched
	 * @param vertex the vertex of which all connected vertices should be found
	 * @return A Set containing vertices: for every pair of vertices v1,v2
	 * where v1 != v2, there exists a path in Graph g (ignoring edge direction) from v1 to v2.
	 * This set will at least contain 'vertex'
	 * For every vertex vg in Graph g that is not in the resulting Set, there exists no path (ignoring edge direction)
	 * from vg to 'vertex'
	 */
	public static <V,E> Set<V> getConnectedVertices(Graph<V,E> g, V vertex){
		Set<V> result = new HashSet<V>();
		result.add(vertex);
		expandConnectedVertices(g, vertex, result);
		return result;
	}
	
	/**
	 * 
	 * @param g
	 * @param newVertex
	 * @param connectedVertices
	 */
	public static <V,E> void expandConnectedVertices(Graph<V,E> g, V newVertex, Set<V> connectedVertices){
		//for every edge (ignoring direction) connected newVertex
		for(E edge : g.edgesOf(newVertex)){
			V connectedVertex; //find the other vertex connected to newVertex
			if(g.getEdgeSource(edge).equals(newVertex)){
				connectedVertex = g.getEdgeTarget(edge);
			} else {
				connectedVertex = g.getEdgeSource(edge);
			}
			if(!connectedVertices.contains(connectedVertex)){ //if this vertex is not in connectedVertices
				connectedVertices.add(connectedVertex); //add it and search for more connected Vertices
				expandConnectedVertices(g, connectedVertex, connectedVertices);
			}
		}
	}
	
	public static Graph<StringLabeledObject,StringLabeledObject> createEmptyMultiGraph(boolean directed){
		Graph<StringLabeledObject,StringLabeledObject> result;
		if(directed){
			result = new DirectedMultigraph<StringLabeledObject, StringLabeledObject>(StringLabeledObject.class);
		} else {
			result = new Multigraph<StringLabeledObject, StringLabeledObject>(StringLabeledObject.class);
		}
		return result;
	}

	public static <V> boolean containsV(List<Set<V>> list, V v){
		for(int i=0;i<list.size();i++){
			if(list.get(i).contains(v)) return true;
		}
		return false;
	}
}
