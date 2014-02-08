package generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.jgrapht.Graph;

import algorithms.koch.Koch;

import shared.SharedStaticMethods;
import shared.StringLabeledObject;

public abstract class GraphGenerator {
	
	public static final String RANDOMLYCONNECTEDGRAPH = "RandomlyConnectedGraph";
	public static final String REGULARMESH = "RegularMesh";
	
	protected final boolean directed;
	/**
	 * Size in number of edges
	 */
	protected final int totalsize;
	/**
	 * Size in number of edges
	 */
	protected final int subgraphsize;
	/**
	 * The generated subgraph will contain this percentage of edges from the supergraphs
	 */
	protected final double subgraphpercent;
	protected final Set<String> alphabet;
	
	protected final boolean checkresult;
	
	protected Graph<StringLabeledObject,StringLabeledObject> subgraph, supergraph1, supergraph2;
	
	/**
	 * 
	 * @param directed
	 * @param totalsize the supergraph size in number of edges
	 * @param subgraphpercent the percentage of the subgraph size in relation to the supergraph (this is relative to the edge size)
	 * @param alphabetSize
	 */
	public GraphGenerator(boolean directed, int totalsize, double subgraphpercent, int alphabetSize, boolean checkresult){
		this.directed = directed;
		this.totalsize = totalsize;
		this.subgraphpercent = subgraphpercent;
		this.subgraphsize = (int) (totalsize * subgraphpercent);
		alphabet = new HashSet<String>();
		for(int i=0; i < alphabetSize; i++){
			alphabet.add("L" + i); //all labels from this generator start with L followed by a number
		}
		this.checkresult = checkresult;
		
	}
	
	/**
	 * Tests if Graph g is connected and adds edges to connect it if it is not
	 * @param g the graph that should be connected
	 * @return the number of edges added
	 */
	protected int connectGraph(Graph<StringLabeledObject,StringLabeledObject> g){
		int result = 0;
		//The List with Sets of Vertices that are connected to each other
		List<Set<StringLabeledObject>> connectedVertices = new ArrayList<Set<StringLabeledObject>>();
		//fill the List with sets
		for(StringLabeledObject vertex : g.vertexSet()){
			if(!SharedStaticMethods.containsV(connectedVertices, vertex)){
				connectedVertices.add(SharedStaticMethods.getConnectedVertices(g, vertex));
			}
		}
		
		while(connectedVertices.size() > 1){
			Set<StringLabeledObject> setA, setB;
			setA = getRandomElement(connectedVertices);
			setB = getRandomElement(connectedVertices);
			if(!setA.equals(setB)){ //do nothing if the sets are the same
				//connect two random vertices form the set
				StringLabeledObject va, vb;
				va = getRandomElement(setA);
				vb = getRandomElement(setB);
				//there is no need to randomize the direction (for directed graphs) since the Sets were already randomly chosen
				g.addEdge(va, vb, StringLabeledObject.getRandomStringLabeledObject(alphabet));
				result++; //increment result for every edge added
				setA.addAll(setB); //add elements of setB to setA
				connectedVertices.remove(setB); //remove setB from connectedvertices (its elements are already in setA
			}
		}
		
		return result;
	}


	public Graph<StringLabeledObject,StringLabeledObject> getSubgraph() {
		if(subgraph == null) resetAndGenerate();
		return subgraph;
	}
	
	/**
	 * @return the supergraph1
	 */
	public Graph<StringLabeledObject, StringLabeledObject> getSupergraph1() {
		if(supergraph1 == null) resetAndGenerate();
		return supergraph1;
	}

	/**
	 * @return the supergraph2
	 */
	public Graph<StringLabeledObject, StringLabeledObject> getSupergraph2() {
		if(supergraph2 == null) resetAndGenerate();
		return supergraph2;
	}
	
	protected static <T> T getRandomElement(Collection<T> coll){
		int random = new Random().nextInt(coll.size());
		Iterator<T> it = coll.iterator();
		for(int i=0;i<random;i++){
			it.next();
		}
		return it.next();
	}
	
	/**
	 * Creates a shallow copy of Graph g, a new graph is created, the edge and vertex sets are copied,
	 * but the same edge and vertex object are used
	 * @param g the Graph that should be copied
	 * @return a copy of g
	 */
	protected Graph<StringLabeledObject,StringLabeledObject> copyGraph(Graph<StringLabeledObject,StringLabeledObject> g){
		Graph<StringLabeledObject,StringLabeledObject> result = SharedStaticMethods.createEmptyMultiGraph(directed);
		for(StringLabeledObject v: g.vertexSet()){
			result.addVertex(v);
		}
		for(StringLabeledObject e : g.edgeSet()){
			result.addEdge(g.getEdgeSource(e), g.getEdgeTarget(e), e);
		}
		return result;
	}
	
	/**
	 * Generate a subgraph
	 * @return
	 */
	protected abstract Graph<StringLabeledObject,StringLabeledObject> generateSubgraph();
	
	/**
	 * Generates a supergraph of which 'subgraph' is a subgraph. The local variable subgraph may not be null when this method is called
	 * @return
	 */
	protected abstract Graph<StringLabeledObject,StringLabeledObject> generateSupergraph();
	
	/**
	 * 
	 * @return a string containing the settings of this generator, useful for file name prefixes
	 */
	public abstract String getSettingString();
	
	/**
	 * Resets the graph generator and generates a new graph with exactly the same parameters
	 */
	public void resetAndGenerate(){
		this.subgraph = null;
		this.supergraph1 = null;
		this.supergraph2 = null;
		
		this.subgraph = generateSubgraph();
		this.supergraph1 = generateSupergraph();
		this.supergraph2 = generateSupergraph();
		
		if(checkresult){
			if(subgraph.edgeSet().size() != subgraphsize){
				System.err.println("Failed to generate supergraph: size" + subgraph.edgeSet().size()
						+ " should be " + totalsize + "\r\n retrying...");
				resetAndGenerate();
				return;
			}
			if(supergraph1.edgeSet().size() != totalsize || supergraph2.edgeSet().size() != totalsize){
				System.err.println("Failed to generate supergraph: sizes" + supergraph1.edgeSet().size() + " , "
						+ supergraph2.edgeSet().size() + " should be " + totalsize + "\r\n retrying...");
				resetAndGenerate();
				return;
			}
			Graph<StringLabeledObject,StringLabeledObject> testSubgraph = Koch.maxCommonSubgraph(supergraph1, supergraph2, true);
			if(testSubgraph.edgeSet().size() != subgraphsize){
				System.err.println("Test subgraph is too large: size" + testSubgraph.edgeSet().size() + " should be "
						+ subgraphsize + "\r\n retrying...");
				resetAndGenerate();
				return;
			}
		}
	}
}
