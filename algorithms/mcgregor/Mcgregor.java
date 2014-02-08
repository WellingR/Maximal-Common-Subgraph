package algorithms.mcgregor;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;

import shared.LabeledObject;

public class Mcgregor<V extends LabeledObject<L>,E extends LabeledObject<L> ,L> {
	
	private List<V> vertices1;
	private List<V> vertices2;
	/**
	 * nodesTried[n1][n2] is true if node n2 has been tried for node n1
	 */
	private boolean[][] nodesTried;
	/**
	 * priority[i1][n2] is true if a node n2 is part of the priority subset for node i1
	 */
	private boolean[][] priority;
	private Marcs<V,E,L> marcs;
	private Map<Integer,Marcs<V,E,L>> marcsCopies;
	private int arcsLeft,bestArcsLeft;
	private boolean findConnectedSubgraphOnly;
	private List<L> labels;
	//remembers how often labels from List 'labels' occur in the graph where they least occur
	//for example, if g1 has 5 vertices with label X and g2 has 3 vertices with label X labelsize for label X will be 3
	private int[] labelsSize;
	//Array with one boolean value for each vertex of g1, if the value is true, then an untried node has been found which did not have the same label
	//this means that if no more untried nodes exist for that vertex, search must still continue for the next vertex (backtrack is not yet needed)
	private boolean[] noLabelMatch;
	private boolean interruptFlag = false;
	private static Set<Mcgregor<?,?,?>> instances = new HashSet<Mcgregor<?,?,?>>();
	
	/**
	 * @param g1 the first supergraph
	 * @param g2 the second supergraph
	 * @param connected must be true if the subgraph has to be a connected subgraph
	 * @return the maximum common subgraph of g1 and g2
	 */
	public static <V extends LabeledObject<L>,E extends LabeledObject<L> ,L> Graph<V,E> maxCommonSubgraph(Graph<V,E> g1, Graph<V,E> g2, boolean connected){
		if(g1.vertexSet().size() > g2.vertexSet().size()){
			return maxCommonSubgraph(g2, g1, connected); //the algorithm asumes g1.vertexSet().size() <= g2.vertexSet().size()
		}
		Mcgregor<V,E,L> m = new Mcgregor<V,E,L>(g1, g2, connected);
		instances.add(m);
		Graph<V, E> result = m.findMaximumCommonSubgraph();
		instances.remove(m);
		return result;
		
	}
	
	public static void interuptAllInstances(){
		for(Mcgregor<?,?,?> m : instances){
			m.interruptInstance();
		}
	}
	
	private void interruptInstance(){
		this.interruptFlag = true;
	}
	
	
	private Mcgregor(Graph<V,E> g1, Graph<V,E> g2, boolean connected){
		vertices1 = new ArrayList<V>(g1.vertexSet());
		vertices2 = new ArrayList<V>(g2.vertexSet());
		//initialize the List that remembers which nodes have been tried
		nodesTried = new boolean[vertices1.size()][vertices2.size()];
		priority = new boolean[vertices1.size()][vertices2.size()];
		
		noLabelMatch = new boolean[vertices1.size()];
		
		//initialize the list with labels
		labels = new ArrayList<L>();
		//Some temporary variables for reuse
		int i;
		L l;
		for(i=0;i<vertices1.size();i++){
			l = vertices1.get(i).getLabel();
			if(!labels.contains(l)){
				labels.add(l);
			}
			//initialize noLabelMatch in the same loop
			noLabelMatch[i]=false;
		}
		for(i=0;i<vertices2.size();i++){
			l = vertices2.get(i).getLabel();
			if(!labels.contains(l)){
				labels.add(l);
			}
		}
		//'labels' now contains all labels in g1 and g2
		//initialize the matrices with labelSize and labelCount
		//temporarily used to count labels from g1
		labelsSize = new int[labels.size()];
		//temporarily used to count labels from g2
		int[] labelsG2 = new int[labels.size()];
		for(i=0;i<vertices1.size();i++){
			l = vertices1.get(i).getLabel();
			labelsSize[labels.indexOf(l)]++;
		}
		for(i=0;i<vertices2.size();i++){
			l = vertices2.get(i).getLabel();
			labelsG2[labels.indexOf(l)]++; 
		}
		//make sure labelSize contains the smallest values
		for(i=0;i<labels.size();i++){
			if(labelsG2[i] < labelsSize[i]){
				labelsSize[i] = labelsG2[i];
				
			}
		}

		//initialize marcs and other variables
		marcs = new Marcs<V,E,L>(g1, g2, connected, labels.size());
		marcsCopies = new HashMap<Integer, Marcs<V,E,L>>();
		marcsCopies.put(0, marcs.getCopy()); //put a copy of the default marcs at 0
		arcsLeft = g1.edgeSet().size();
		bestArcsLeft = 0;
		findConnectedSubgraphOnly = connected;
		
		
	}
	
	private Graph<V,E> findMaximumCommonSubgraph(){
		//initalize marcs and other variables
		Marcs<V,E,L> marcsTemp;
		Marcs<V,E,L> bestMarcs = null;
		
		//current Node
		int iNode1 = 0;
		markAllAsUntried(iNode1); //Mark all nodes of G2 as untried for node i;
		//System.out.println("Starting at node " + iNode1 + "label:" + vertices1.get(iNode1).getLabel());
		
		while(iNode1>=0){ 	//repeat .... until i==-1    (i==0 changed to i==-1 because we start at 0 instead of 1)
			if(interruptFlag) return null; //Stop and return null when interrupted
						//if there are any untried nodes in G2 to which node i of G1 may correspond
						//xi := one of these nodes
			int xiNode2 = getUntriedNode(iNode1);
			//System.out.println("Fetched untried node" + xiNode2);
			
			if(xiNode2 != -1){
				nodesTried[iNode1][xiNode2] = true;	//Mark node xi as tried for node i
				
				if(!vertices1.get(iNode1).getLabel().equals(vertices2.get(xiNode2).getLabel())){//if the labels do not batch
					noLabelMatch[iNode1] = true;
					// do not attempt to pair these nodes continue trying other nodes
				} else {
				
					//System.out.println("node pair:" + iNode1 + "," + xiNode2 + " before \n" + marcs);
					marcsTemp = marcs.getCopy(); //store a temporary copy of marcs, so it can be restored when arcsleft <= bestarcsleft for this node
					marcs.refine(vertices1.get(iNode1), vertices2.get(xiNode2));				//refine MARCS on the basis of this tentative correspondence for node i;
					//update the label
					marcs.incrementLabelCount(labels.indexOf(vertices1.get(iNode1).getLabel()));
					arcsLeft = marcs.getArcsLeft(); //update arcsLeft;
					//System.out.println("after:" + marcs + "\n Arcsleft" + arcsLeft + "\n");
					if(arcsLeft > bestArcsLeft){
						//System.out.println(iNode1);
						if(iNode1 == vertices1.size()-1 || allPossibleNodesMapped()){ 	//if i = p1   (p1 is g1.vertexSet().size(). Because java arrays start at 0 we use size-1)
							if(!findConnectedSubgraphOnly || (findConnectedSubgraphOnly && marcs.getArcsInBiggestConnectedGraph() > bestArcsLeft)){ //this makes sure connected graphs are found only
								//System.out.println("partial solution found!");
								bestMarcs = marcs.getCopy();					// take note  of  xl, x2,  ...,  xp1,  MARCS;  (x1 ..xp1 are stored in MARCS)
								bestArcsLeft = arcsLeft;
							}
						} else{
							iNode1++;
							marcsCopies.put(iNode1, marcs.getCopy()); 		//store a copy of MARCS, arcsleft  in  the workspace associated with node i 
							markAllAsUntried(iNode1); //mark all nodes of G2 as untried for node i
						}
					} else {
						marcs = marcsTemp;
						//System.out.println("Restored Marcs");
					}
				}
			} else {
				//if there are still untried nodes, but with a different label
				//(exclude this test for the last node, because there is no next node to search for)
				if(noLabelMatch[iNode1] && iNode1 != vertices1.size()-1){ 
					noLabelMatch[iNode1] = false; //reset the boolean
					//System.out.println("all untried nodes have different labels");
					iNode1++;
					marcsCopies.put(iNode1, marcs.getCopy()); 		//store a copy of MARCS, arcsleft  in  the workspace associated with node i 
					markAllAsUntried(iNode1); //mark all nodes of G2 as untried for node i
				} else {
					iNode1--;									// i := i-1
					if(iNode1 >= 0) marcs = marcsCopies.get(iNode1).getCopy();				// restore marcs, arcsleft from the workspace associated with node i (arcsleft is ignored here, it is updated after refining)
				}
			}
		}
		
		if(bestMarcs == null){
			//not supposed to happen, this exception is thrown because it explains the cause better then the nullpointer that would else be thrown
			//TODO check if this happens for empty graphs, or graphs that do not have any compatible edges or vertices
			throw new RuntimeException("Mcgregor algorithm failed to find any solution");
		}
		return bestMarcs.toGraph();
	}
	
	private boolean allPossibleNodesMapped(){
		boolean result = true;
		//System.out.println("Checking labelcount.....");
		for(int i=0;i<labels.size();i++){
			//System.out.println("testing (size==count): " + (labelsSize[i]) + "==" + marcs.getLabelCount(i));
			result &= (labelsSize[i]) == marcs.getLabelCount(i);
		}
		//System.out.println("Done checking labelcount result = " +result);
		//if(result) System.err.println(result + "---------------------------------");
		return result;
	}
	
	/**
	 * if there are any untried nodes in G2 to with node i (in this case v1) of G1 may correspond
	 * Does NOT check if the node labels match
	 * @param iNode1 a node number from g1
	 * @return a node number from g2 which is UNTRIED and has the same label as v1, or -1 when no untried node exists
	 */
	private int getUntriedNode(int iNode1){
		for(int v2=0;v2<vertices2.size();v2++){
			if(priority[iNode1][v2] && !nodesTried[iNode1][v2]){ //if this node combination is untried and in the priority subset
				return v2; 
			}
		}
		//when all nodes from the priorityList have been tried
		for(int v2=0;v2<vertices2.size();v2++){
			if(!nodesTried[iNode1][v2]){ //if this node combination has not been tried
				return v2; 
			}
		}
		return -1;
	}
	
	/**
	 * 
	 * @param iNode1 a node number from g1
	 */
	private void markAllAsUntried(int iNode1){
		for(int iv =0;iv<vertices2.size();iv++){ //Mark all nodes of G2 as untried for node i;
			nodesTried[iNode1][iv]= false;
			priority[iNode1][iv] = false;
		}
		for(V v2 : marcs.getPrioritySubset(vertices1.get(iNode1))){
			priority[iNode1][vertices2.indexOf(v2)] = true;
		}
	}
}
