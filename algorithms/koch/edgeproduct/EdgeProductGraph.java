package algorithms.koch.edgeproduct;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.graph.Multigraph;
import org.jgrapht.graph.SimpleGraph;

import shared.LabeledObject;

/**
 *
 * @author Ruud
 */
public class EdgeProductGraph<V extends LabeledObject<L>,E extends LabeledObject<L> ,L> extends SimpleGraph< EdgeProduct<E> , CompatibilityEdge> {

	private static final long serialVersionUID = 1102690729877939502L;
	
	private Graph<V,E> g1, g2;
	private boolean directed;

    /**
     * Constructs a product graph
     */
    public EdgeProductGraph(Graph<V,E> gLeft, Graph<V,E> gRight){
        super(CompatibilityEdge.class);
        directed = g1 instanceof DirectedGraph && g2 instanceof DirectedGraph;

        this.g1 = gLeft;
        this.g2 = gRight;
        
        for(E e1 : g1.edgeSet()){
            for(E e2 : g2.edgeSet()){
                //Only add the edge product vertex if the edge labels and end vertex labels are the same
                if(e1.getLabel().equals(e2.getLabel())){
                	if(g1.getEdgeSource(e1).getLabel().equals(g2.getEdgeSource(e2).getLabel())
                            && g1.getEdgeTarget(e1).getLabel().equals(g2.getEdgeTarget(e2).getLabel())){
                        this.addVertex(new EdgeProduct<E>(e1,e2));
                    } else {
                    	//also add edges when source and target are interchanged (for undirected graphs only)
                    	if(!directed &&g1.getEdgeSource(e1).getLabel().equals(g2.getEdgeTarget(e2).getLabel())
                                && g1.getEdgeTarget(e1).getLabel().equals(g2.getEdgeSource(e2).getLabel())){
                    		this.addVertex(new EdgeProduct<E>(e1,e2));
                    	}
                    }
                }
                        
            }
        } //all Edge Products have been added

        List<EdgeProduct<E>> toCompare = new ArrayList<EdgeProduct<E>>();
        toCompare.addAll(vertexSet());
        while(!toCompare.isEmpty()){
            EdgeProduct<E> v1 = toCompare.remove(0);
            for (EdgeProduct<E> v2 : toCompare) {
            	EdgeType compatibleType = edgePairsCompatible(v1, v2);
                if(compatibleType != null){
                    //add an edge between every compatible vertex pair
                    this.addEdge(v1, v2, new CompatibilityEdge(compatibleType));
                }
            }
        }
    }

    /**
     * Returns true when two edge pairs (e1,e2) and (f1,f2) are compatible
     *
     * There is an edge between two vertices eH,fH in VH with eH =(e1,e2) and fH =(f1,f2), if
     * 1) e1 != f1 and e2 != f2, and
     * 2) if either e1,f1 in G1 are connected via a vertex of the same label as the vertex shared by e2,f2 in G2,
     * 3) or e1,f1 and e2,f2 are not adjacent in G1 and in G2, respectively
     **/
    private EdgeType edgePairsCompatible(EdgeProduct<E> p1, EdgeProduct<E> p2){
        //either e1,f1 in G1 are connected via a vertex of the same label as the vertex shared by e2,f2 in G2
        //or e1,f1 and e2,f2 are not adjacent in G1 and in G2, respectively
        E e1, e2, f1, f2;
        e1 = p1.getLeft(); //Edge in G1
        e2 = p1.getRight(); //Edge in G2
        f1 = p2.getLeft(); //Edge in G1
        f2 = p2.getRight(); //Edge in G2

        //check condition 1)
        if(e1.equals(f1) || e2.equals(f2)){
            //condition 1 not satisfied, edges are not compatible
            return null;
        }

        Set<V> possibleVerticesG1 = commonVertices(g1, e1, f1);
        Set<V> possibleVerticesG2 = commonVertices(g2, e2, f2);

        if(possibleVerticesG1.isEmpty() && possibleVerticesG2.isEmpty()){
            //e1,f1 and e2,f2 are not adjacent in G1 and in G2, respectively
        	//Create a D_Edge
            return EdgeType.D_EDGE;
        }

        for(LabeledObject<L> v1 : possibleVerticesG1){
            for(LabeledObject<L> v2 : possibleVerticesG2){
                if(v1.getLabel().equals(v2.getLabel())){
                     // e1,f1 in G1 are connected via a vertex of
                     // the same label as the vertex shared by e2,f2 in G2.
                	 //A C_edge shuold be created
                    return EdgeType.C_EDGE;
                }
            }
        }

        //The edge pairs are not compatible
        return null;
    }

    /**
     * Returns a set with the common vertices of edge E1 and E2 in Graph g
     * The result will be a Set of size 0, 1 or 2
     */
    public Set<V> commonVertices(Graph<V,E> g,
            E e1, E e2){
        Set<V> commonVertices = new LinkedHashSet<V>();

        V vertexE1EdgeSource = g.getEdgeSource(e1);
        V vertexE1EdgeTarget = g.getEdgeTarget(e1);
        if(vertexE1EdgeSource.equals(g.getEdgeSource(e2))) commonVertices.add(vertexE1EdgeSource);
        if(vertexE1EdgeSource.equals(g.getEdgeTarget(e2))) commonVertices.add(vertexE1EdgeSource);
        if(vertexE1EdgeTarget.equals(g.getEdgeSource(e2))) commonVertices.add(vertexE1EdgeTarget);
        if(vertexE1EdgeTarget.equals(g.getEdgeTarget(e2))) commonVertices.add(vertexE1EdgeTarget);
        return commonVertices;
    }

    /**
     * Creates the subgraph of g1 containing all the edges from the edge product in the vertices of this EdgeProductGraph
     * @param edgeProductVertices if (and only if) these vertices induce a complete subgraph in this EdgeProductGraph, then the result
     * will be the a common subgraph of g1 and g2.
     * @return a subgraph of g1
     */
    public Graph<V,E> toSubgraph(Set<EdgeProduct<E>> edgeProductVertices){
        Graph<V,E> result;
        if(g1 instanceof DirectedGraph){
            result = new DirectedMultigraph<V,E>(g1.getEdgeFactory());
        } else {
            result = new Multigraph<V,E>(g1.getEdgeFactory());
        }

        //Add the left Edge (including vertices) from all the EdgeProducts in vertices
        for(EdgeProduct<E> ep: edgeProductVertices){
            E edge = ep.getLeft();
            V vSource = g1.getEdgeSource(edge);
            V vTarget = g1.getEdgeTarget(edge);
            result.addVertex(vSource);
            result.addVertex(vTarget);
            result.addEdge(vSource, vTarget, edge);
        }

        return result;
    }
}