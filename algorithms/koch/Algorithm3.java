package algorithms.koch;

import java.util.LinkedHashSet;
import java.util.Set;
import org.jgrapht.graph.SimpleGraph;

import algorithms.koch.edgeproduct.CompatibilityEdge;


/**
 *
 * Includes the initialisation Algorithm4 as described by Koch
 */
public class Algorithm3 {
	
	private static boolean interruptFlag = false;
	
	public static void interruptAllInstances(){
		interruptFlag = true;
	}

    public static <V> Set<V> largest_C_Clique_Init(SimpleGraph<V,CompatibilityEdge> g){
    	interruptFlag = false;
    	Set<V> result = new LinkedHashSet<V>();
        //set of vertices which have already been used for the initialization of Enumerate_C_Cliques()
    	Set<V> t = new LinkedHashSet<V>();		// T <- Empty
    	Set<V> p, d, n;
    	int currentmaxresult = 0;
    	for(V u:g.vertexSet()){				//for all u ELEMENTOF V
    		if(interruptFlag) return null; //Stop immediately and return null when interrupted
    		p = new LinkedHashSet<V>();			// P <- Empty
    		d = new LinkedHashSet<V>();			// D <- Empty
    		//s = new LinkedHashSet<V>();			// S <- Empty
    		n = neighbourVertices(g, u);	// N <- {v ELEMENTOF V | {u,v} ELEMENTOF E}
    		for(V v : n){					// for each v ELEMENTOF N
    			if(hasC_Edge(g, u, v)){		// if u and v are adjacent via a c-edge
    				if(t.contains(v)){		// then if v ELEMENTOF T
    					//s.add(v);			// S <- S UNION {v}
    				}else {
    					p.add(v);			// else P <- P UNION {v}
    				}
    			} else if (hasD_Edge(g, u, v)){// else if u and v are adjacent via a d-edge
    				d.add(v);				// D <- D UNION {v}
    			}
    		}
    		Set<V> c = new LinkedHashSet<V>();
    		c.add(u);
    		Set<V> subresult = largest_C_Clique(g, c, p, d, currentmaxresult); //ENUMERATE....
    		if(subresult != null && subresult.size() > result.size()){
    			result = subresult;
                currentmaxresult = result.size();
    		}
    		t.add(u);						// T <- T UNION {v}
    	}
    	return result;
    }
    
    /**
     * 
     * @param g The graph where the largest clique needs to be found
     * @param c Set of vertices belonging to the current clique
     * @param p Set of vertices which can be added to C, because they are neighbours of vertex u via C-Edges
     * @param d Set of vertices which cannot directly be added to C because they are neighbours of u via D-Edges
     * @param s set of vertices which are not allowed to be added to C
     * @return the largest clique in graph g
     */
    private static <V> Set<V> largest_C_Clique(SimpleGraph<V,CompatibilityEdge> g, Set<V> c, Set<V> p, Set<V> d, int currentmaxresult){
    	if(interruptFlag) return null; //Stop immediately and return null when interrupted
        Set<V> result = new LinkedHashSet<V>(c);
        if(p.isEmpty() || p.size() + c.size() + d.size() <= currentmaxresult){ //if p=EMPTY and s=EMPTY
            return result;                               //REPORT.CLIQUE
        } else {
            Set<V> pCopy = new LinkedHashSet<V>(p);
            for(V ui : p){                    			 //for i <- 1 to k
                pCopy.remove(ui);             			 //P <-P\{ui}
                Set<V> pNext = new LinkedHashSet<V>(pCopy);    //P' <- P
                Set<V> dNext = new LinkedHashSet<V>(d);		 //D' <- D
                Set<V> n = neighbourVertices(g,ui);//N <- { v ELEMENTOF V | {ui,v} ELEMENTOF E }
                
                for(V v : d){							 // for all v ELEMENTOF D'
                	//(note that D and D' are the same at this point, to allow concurrent modification we loop over D)
                	if(hasC_Edge(g, ui, v)){	 		 // if v and ui are adjacent via a c-edge
                		pNext.add(v);					 //P' <- P' UNION {v}
                		dNext.remove(v);				 //D' <- D'\{v}
                	}
                }
                
                Set<V> cNext = new LinkedHashSet<V>(c);
                cNext.add(ui);               			 //C UNION {ui}
                pNext.retainAll(n);                      //P' INTERSECTION N
                dNext.retainAll(n);						 //D' INTERSECTION N
                Set<V> clique = largest_C_Clique(g, cNext, pNext, dNext, currentmaxresult); //ENUMERATE.C_CLIQUES....
                if(clique != null && clique.size() > result.size()){
                	result = clique;
                    currentmaxresult = clique.size();
                }
            }
        }
        return result;
    }
    
    private static <V> boolean hasC_Edge(SimpleGraph<V,CompatibilityEdge> g, V u, V v){    	
    	for(CompatibilityEdge e:g.getAllEdges(u, v)){
    		if(e.isC_Edge()){
    			return true;
    		}
    	}
    	return false;
    }
    
    private static <V> boolean hasD_Edge(SimpleGraph<V,CompatibilityEdge> g, V v1, V v2){    	
    	for(CompatibilityEdge e:g.getAllEdges(v1, v2)){
    		if(e.isD_Edge()){
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * 
     * @param u a Vertex of g
     * @return {v ELEMENTOF V | {u,v} ELEMENTOF E}
     */
    private static <V,E> Set<V> neighbourVertices(SimpleGraph<V,E> g, V u){
    	Set<V> result = new LinkedHashSet<V>();
    	for(E edge : g.edgesOf(u)){
            V neighbour = g.getEdgeSource(edge);
            if(neighbour.equals(u)){
                neighbour = g.getEdgeTarget(edge);
            }
            result.add(neighbour);
            
        }
    	return result;
    }

}
