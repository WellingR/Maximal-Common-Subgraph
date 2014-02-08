package generator.randomlyconnected;


import generator.GraphGenerator;

import org.jgrapht.Graph;

import shared.SharedStaticMethods;
import shared.StringLabeledObject;

public class RandomlyConnectedGraphGenerator extends GraphGenerator {
	
	protected final double edgeDensity;
	
	/**
	 * @param directed true if the result should be a directed graph
	 * @param totalsize the total numer of vertices that the pair of supergraphs should have
	 * @param subgraphsize the total numer of vertices that the subgraph should have 
	 * @param edgeDensity the percentage of the total amount of edges in the complete subgraph
	 * @param checkresult if true, the resulting graphs will be checked to ensure the right number of edges in sub and supergraphs
	 * this may cause infinite (or extremely long) loops in cases with small alphabet sizes and subgraph percentages
	 * Because the larges common subgraph will be checked, generating will take much longer (or extremely long for big graphs)
	 * (with 1 edge between every vertex pair) that should be added to the graph
	 */
	public RandomlyConnectedGraphGenerator(boolean directed, int totalsize, double subgraphpercent, double edgeDensity, int alphabetSize, boolean checkresult){
		super(directed, totalsize, subgraphpercent, alphabetSize, checkresult);
		this.edgeDensity = edgeDensity;
		int vertices = (int) (Math.sqrt(subgraphsize / edgeDensity * 2) + 1);
		if(vertices+1 > subgraphsize){
			throw new IllegalArgumentException("Edge density, subgraph size combination not possible");
		}
		vertices = (int) (Math.sqrt(totalsize / edgeDensity * 2) + 1);
		if(vertices+1 > totalsize){
			throw new IllegalArgumentException("Edge density, totalsize combination not possible");
		}
	}
	
	public String getSettingString(){
		String result = RANDOMLYCONNECTEDGRAPH;
		if(directed){
			result += "_t";
		} else {
			result += "_f";
		}
		result += "_" + totalsize;
		result += "_" + subgraphpercent;
		result += "_" + alphabet.size();
		result += "_" + edgeDensity;
		return result;
	}
	
	protected Graph<StringLabeledObject,StringLabeledObject> generateSupergraph(){
		Graph<StringLabeledObject,StringLabeledObject> result = copyGraph(subgraph);
		int vertices = (int) (Math.sqrt(totalsize / edgeDensity * 2) + 1);
		for(int i=result.vertexSet().size();i< vertices; i++){
			result.addVertex(StringLabeledObject.getRandomStringLabeledObject(alphabet));
		}
		
		connectGraph(result);
		
		while(result.edgeSet().size()< totalsize){
			StringLabeledObject v1 = getRandomElement(result.vertexSet());
			StringLabeledObject v2 = getRandomElement(result.vertexSet());
			if(!v2.equals(v1)){
				StringLabeledObject edge = StringLabeledObject.getRandomStringLabeledObject(alphabet);
				String label = edge.getLabel();
				//Test if an edge from v1 to v2 with the same label already exists
				boolean edgeExists = false;
				for(StringLabeledObject edge2 : result.getAllEdges(v1, v2)){
					edgeExists |= edge2.getLabel().equals(label);
				}
				if(!edgeExists){
					//add the edge
					result.addEdge(v1, v2, edge);
				}
			}
		}
		
		return result;
	}
	
	
	protected Graph<StringLabeledObject,StringLabeledObject> generateSubgraph(){
		Graph<StringLabeledObject,StringLabeledObject> result = SharedStaticMethods.createEmptyMultiGraph(directed);
		int vertices = (int) (Math.sqrt(subgraphsize / edgeDensity * 2) + 1);
		for(int i=0;i< vertices; i++){
			result.addVertex(StringLabeledObject.getRandomStringLabeledObject(alphabet));
		}
		connectGraph(result); //make sure the final result will be connected
		
		while(result.edgeSet().size() < subgraphsize){
			StringLabeledObject v1 = getRandomElement(result.vertexSet());
			StringLabeledObject v2 = getRandomElement(result.vertexSet());
			if(!v2.equals(v1)){
				StringLabeledObject edge = StringLabeledObject.getRandomStringLabeledObject(alphabet);
				String label = edge.getLabel();
				//Test if an edge from v1 to v2 with the same label already exists
				boolean edgeExists = false;
				for(StringLabeledObject edge2 : result.getAllEdges(v1, v2)){
					edgeExists |= edge2.getLabel().equals(label);
				}
				if(!edgeExists){
					//add the edge
					result.addEdge(v1, v2, edge);
				}
			}
		}
		
		return result;
	}
	
}
