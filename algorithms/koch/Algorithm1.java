package algorithms.koch;

import java.util.LinkedHashSet;
import java.util.Set;
import org.jgrapht.graph.SimpleGraph;

/**
 *
 * @author Ruud
 */
public class Algorithm1 {

    public static <V,E> Set<V> largestClique(SimpleGraph<V,E> g){
        Set<V> result = new LinkedHashSet<V>();

        result = enumerateCliques(g, new LinkedHashSet<V>(), new LinkedHashSet<V>(g.vertexSet()), 0);

        return result;
    }
    
    /**
     * 
     * @param g The graph where the largest clique needs to be found
     * @param c Set of vertices belonging to the current clique
     * @param p Set of vertices which can be added to C
     * @param s set of vertices which are not allowed to be added to C
     * @return the largest clique in graph g
     */
    private static <V,E> Set<V> enumerateCliques(SimpleGraph<V,E> g, Set<V> c, Set<V> p, int currentmaxresult){
        Set<V> result = new LinkedHashSet<V>(c);
        if(p.isEmpty() || p.size() + c.size() <= currentmaxresult){   //if p=EMPTY and s=EMPTY
        	//check for s.isEmpty is removed because the code will return the same result if only P is empty
            return result;                               //REPORT.CLIQUE
        } else {
            Set<V> pCopy = new LinkedHashSet<V>(p);
            for(V currentVertex : p){                    //for i <- 1 to k
                pCopy.remove(currentVertex);             //P <-P\{ui}
                Set<V> pNext = new LinkedHashSet<V>(pCopy);    //P' <- P
                Set<V> n = new LinkedHashSet<V>();             
                for(E edge : g.edgesOf(currentVertex)){
                    V neighbour = g.getEdgeSource(edge);
                    if(neighbour.equals(currentVertex)){
                        neighbour = g.getEdgeTarget(edge);
                    }
                    n.add(neighbour);
                }                                        //N <- { v ELEMENTOF V | {ui,v} ELEMENTOF E }

                Set<V> cNext = new LinkedHashSet<V>(c);
                cNext.add(currentVertex);                //C UNION {ui}
                pNext.retainAll(n);                      //P' INTERSECTION N
                
                Set<V> clique = enumerateCliques(g, cNext, pNext, currentmaxresult); //ENUMERATE.CLIQUES....
                if(clique.size() > result.size()){
                    result = clique;
                    currentmaxresult = clique.size();
                }
            }
        }
        return result;
    }

}
