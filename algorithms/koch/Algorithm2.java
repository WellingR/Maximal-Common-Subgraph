package algorithms.koch;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jgrapht.graph.SimpleGraph;

/**
 *
 * @author Ruud
 */
public class Algorithm2{
	
	private static boolean interruptFlag = false;

    public static <V,E> Set<V> largestClique(SimpleGraph<V,E> g){
    	interruptFlag = false;
        Set<V> result = new LinkedHashSet<V>();

        result = enumerateCliques(g, new LinkedHashSet<V>(), new LinkedHashSet<V>(g.vertexSet()),0);

        return result;
    }
    
    public static void interruptAllInstances(){
    	interruptFlag = true;
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
    	if(interruptFlag) return null;
        Set<V> result = new LinkedHashSet<V>(c);
        if(p.isEmpty() || p.size() + c.size() <= currentmaxresult){ //if p=EMPTY and s=EMPTY
        	//check for s.isEmpty is removed because the code will return the same result if only P is empty
        	//added p.size() + c.size() <= currentmaxresult, if this is true, then the new clique can not be bigger then the clique that has already been found
            return result;                                   //REPORT.CLIQUE
        } else {
            List<V> pList = new ArrayList<V>(p);
            V ut = pList.get(0);                           	 //Let ut be a vertex from P
            for(V currentVertex : p){                        //for i <- 1 to k
                if(!g.containsEdge(ut, currentVertex)){      //if ui is not adjacent ut ut
                    pList.remove(currentVertex);             //P <-P\{ui}
                    Set<V> pNext = new LinkedHashSet<V>(pList);    //P' <- P
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
        }
        return result;
    }

}
