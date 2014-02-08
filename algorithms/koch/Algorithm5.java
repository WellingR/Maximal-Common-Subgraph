package algorithms.koch;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jgrapht.graph.SimpleGraph;

import algorithms.koch.edgeproduct.CompatibilityEdge;


/**
 *
 * Includes the initialisation Algorithm4 as described by Koch
 */
public class Algorithm5 {

    public static <V> Set<V> largest_C_Clique(SimpleGraph<V,CompatibilityEdge> g){
    	Set<V> result = new LinkedHashSet<V>();
        //set of vertices which have already been used for the initialization of Enumerate_C_Cliques()
    	Set<V> t = new LinkedHashSet<V>();		// T <- Empty
    	Set<V> p, d, n;
    	int currentmaxresult = 0;
    	//int i=0;
    	//System.out.println("total set size:" + g.vertexSet().size());
    	for(V u:g.vertexSet()){				//for all u ELEMENTOF V
    		//System.out.print(i + " ");
    		p = new LinkedHashSet<V>();			// P <- Empty
    		d = new LinkedHashSet<V>();			// D <- Empty
    		n = neighbourVertices(g, u);	// N <- {v ELEMENTOF V | {u,v} ELEMENTOF E}
    		for(V v : n){					// for each v ELEMENTOF N
    			if(hasC_Edge(g, u, v)){		// if u and v are adjacent via a c-edge
    				if(!t.contains(v)){		// then if v ELEMENTOF T
    					p.add(v);			// else P <- P UNION {v}
    				}
    			} else if (hasD_Edge(g, u, v)){// else if u and v are adjacent via a d-edge
    				d.add(v);				// D <- D UNION {v}
    			}
    		}
    		Set<V> c = new LinkedHashSet<V>();
    		c.add(u);
    		Set<V> subresult = enumerate_C_Cliques(g, c, p, d, t, currentmaxresult); //ENUMERATE....
    		if(subresult.size() > result.size()){
    			result = subresult;
                currentmaxresult = subresult.size();
    		}
    		t.add(u);						// T <- T UNION {v}
    		//i++;
    	}
    	//System.out.println();
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
    private static <V> Set<V> enumerate_C_Cliques(SimpleGraph<V,CompatibilityEdge> g, Set<V> c, Set<V> p, Set<V> d, Set<V> t, int currentmaxresult){
        Set<V> result = new LinkedHashSet<V>(c);
        if(p.isEmpty() || p.size() + c.size() + d.size() <= currentmaxresult){//if p=EMPTY
            return result;                               //REPORT.CLIQUE
        } else {
        	List<V> pList = new ArrayList<V>(p);
            V ut = pList.get(0);      
            for(V ui : p){                    			 //for i <- 1 to k
            	Set<V> target = new LinkedHashSet<V>(d);
            	target.removeAll(neighbourVertices(g, ut)); //target is all vertices from D that are not adjacent to ut
            	if(!g.containsEdge(ut, ui) ||			 // if ui is not adjacent to ut
            			hasCPath(g, ui, target, new LinkedHashSet<V>())){ //or ui is connected via a c-path to a
            														//vertex form D that is not adjacent to ut
            		
	                pList.remove(ui);             			 //P <-P\{ui}
	                Set<V> pNext = new LinkedHashSet<V>(pList);    //P' <- P
	                Set<V> dNext = new LinkedHashSet<V>(d);		 //D' <- D
	                Set<V> n = neighbourVertices(g,ui);//N <- { v ELEMENTOF V | {ui,v} ELEMENTOF E }
	                
	                for(V v : d){							 // for all v ELEMENTOF D'
	                	//(note that D and D' are the same at this point, to allow concurrent modification we loop over D)
	                	if(p.contains(v)){					 //if v ELEMENTOF P
	                		pNext.add(v);					 // then P' = P' UNION {v}
	                	} else if(d.contains(v)){			 // else if v ELEMENTOF D   \\can v be added to P?
	                		if(hasC_Edge(g, ui, v)){		 // then if v and ui are adjacent via a C-edge
	                										 //  \\is v an initializing vertex
	                			if(!t.contains(v)){			 // if v ELEMENTOF T
	                			} else {
	                				pNext.add(v);			 // else P'=P' UNION {v}
	                			}
	                			dNext.remove(v);
	                		}
	                	}
	                }
	                
	                Set<V> cNext = new LinkedHashSet<V>(c);
	                cNext.add(ui);               			 //C UNION {ui}
	                pNext.retainAll(n);                      //P' INTERSECTION N
	                dNext.retainAll(n);						 //D' INTERSECTION N
	                Set<V> clique = enumerate_C_Cliques(g, cNext, pNext, dNext, t, currentmaxresult); //ENUMERATE.C_CLIQUES....
	                if(clique.size() > result.size()){
	                	result = clique;
                        currentmaxresult = clique.size();
	                }
            	}
            }
        }
        return result;
    }
    
    private static <V> boolean hasCPath(SimpleGraph<V,CompatibilityEdge> g, V source, Set<V> target, Set<V> exclude){
    	//first check if there is a C_Edge from source to any element of target
    	for(V v : target){
    		if(hasC_Edge(g, source, v)){
    			return true;
    		}
    	}
    	boolean result = false;
    	//add source to the exclude list (no edge from source to any element from target exists)
    	exclude.add(source);
    	//check the same for every C_Neighbour of source
    	Set<V> neighbours = neighbour_C_Vertices(g, source);
    	//Remove all neighbours that have already been checked
    	neighbours.removeAll(exclude);
    	for(V neighbour : neighbours){
    		//if there is a C-Path from a C-Neighbour of source to a vertex in target,
    		//then there is a C_Path form source to the same vertex in target
    		result |= hasCPath(g, neighbour, target, exclude);
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
    
    /**
     * return only the vertices that neighbour u with C_Edges
     * @param u a Vertex of g
     * @return {v ELEMENTOF V | {u,v} ELEMENTOF E}
     */
    private static <V> Set<V> neighbour_C_Vertices(SimpleGraph<V,CompatibilityEdge> g, V u){
    	Set<V> result = new LinkedHashSet<V>();
    	for(CompatibilityEdge edge : g.edgesOf(u)){
            if(edge.isC_Edge()){
	    		V neighbour = g.getEdgeSource(edge);
	            if(neighbour.equals(u)){
	                neighbour = g.getEdgeTarget(edge);
	            }
	            result.add(neighbour);
            }
        }
    	return result;
    }

}
