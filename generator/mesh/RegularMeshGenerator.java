package generator.mesh;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import generator.GraphGenerator;

import org.jgrapht.Graph;

import shared.SharedStaticMethods;
import shared.StringLabeledObject;

public class RegularMeshGenerator extends GraphGenerator {
	
	private final int dimension;
	//The set with the coordinates of the Vertices
	private Set<Coordinate> subgraphCoordinates;
	private Set<Coordinate> subgraphBorderCoordinates;
	private Map<Coordinate,StringLabeledObject> subgraphVertices;
	
	public RegularMeshGenerator(boolean directed, int totalsize, double subgraphpercent, int alphabetSize, int dimension, boolean checkresult) {
		super(directed, totalsize, subgraphpercent, alphabetSize, checkresult);
		if(dimension < 1) throw new RuntimeException("Dimension needs to be at least 1");
		this.dimension = dimension;
	}
	
	public String getSettingString(){
		String result = REGULARMESH;
		if(directed){
			result += "_t";
		} else {
			result += "_f";
		}
		result += "_" + totalsize;
		result += "_" + subgraphpercent;
		result += "_" + alphabet.size();
		result += "_" + dimension;
		return result;
	}

	protected Graph<StringLabeledObject,StringLabeledObject> generateSubgraph(){
		subgraphVertices = new HashMap<Coordinate, StringLabeledObject>();
		subgraphBorderCoordinates = new HashSet<Coordinate>();
		subgraphCoordinates  = new HashSet<Coordinate>();
		Graph<StringLabeledObject,StringLabeledObject> result = SharedStaticMethods.createEmptyMultiGraph(directed);
		//the set with coordinates that can be added to the graph (neighbour coordinates of the ones that are in the graph)
		subgraphBorderCoordinates.add(Coordinate.getOriginCoordinate(dimension));
		while(result.edgeSet().size() < subgraphsize){
			Coordinate c = getRandomElement(subgraphBorderCoordinates);
			int edgesToAdd = 0;
			for(Coordinate c2:c.getNeighbours()){
				//if a neighbour is in the Map (the vertex has already been created) then add an edge
				if(subgraphVertices.containsKey(c2)){
					edgesToAdd++;
				}
			}
			if(edgesToAdd + result.edgeSet().size() > subgraphsize){
				continue;
			}
			subgraphCoordinates.add(c);
			updateBorderCoordinates(subgraphBorderCoordinates, c, null);
			
			//create a vertex for the new coordinate
			StringLabeledObject vertex = StringLabeledObject.getRandomStringLabeledObject(alphabet);
			result.addVertex(vertex);
			subgraphVertices.put(c, vertex);
			for(Coordinate c2:c.getNeighbours()){
				//if a neighbour is in the Map (the vertex has already been created) then add an edge
				if(subgraphVertices.containsKey(c2)){
					//Create edge with a random label
					StringLabeledObject edge = StringLabeledObject.getRandomStringLabeledObject(alphabet);
					if(!directed || Math.random()<0.5){
						//if the graph is undirected OR random has chosen c->c2
						result.addEdge(subgraphVertices.get(c), subgraphVertices.get(c2), edge);
					} else{
						// c2->c
						result.addEdge(subgraphVertices.get(c2), subgraphVertices.get(c), edge);
					}
				}
			}
		}
		
		return result;
	}
	
	protected Graph<StringLabeledObject,StringLabeledObject> generateSupergraph(){
		Graph<StringLabeledObject,StringLabeledObject> result = copyGraph(subgraph);
		Set<Coordinate> addedCoordinates = new HashSet<Coordinate>();
		//the set with coordinates that can be added to the graph (neighbour coordinates of the ones that are in the graph)
		Set<Coordinate> borderCoordinates = new HashSet<Coordinate>(subgraphBorderCoordinates);
		borderCoordinates.add(Coordinate.getOriginCoordinate(dimension));
		Map<Coordinate,StringLabeledObject> supergraphVertices = new HashMap<Coordinate, StringLabeledObject>(subgraphVertices);
		while(result.edgeSet().size() < totalsize){
			Coordinate c = getRandomElement(borderCoordinates);
			int edgesToAdd = 0;
			for(Coordinate c2:c.getNeighbours()){
				//if a neighbour is in the Map (the vertex has already been created) then add an edge
				if(supergraphVertices.containsKey(c2)){
					edgesToAdd++;
				}
			}
			if(edgesToAdd + result.edgeSet().size() > totalsize){
				continue;
			}
			addedCoordinates.add(c);
			updateBorderCoordinates(borderCoordinates, c, addedCoordinates);
			
			//create a vertex for the new coordinate
			StringLabeledObject vertex = StringLabeledObject.getRandomStringLabeledObject(alphabet);
			result.addVertex(vertex);
			supergraphVertices.put(c, vertex);
			for(Coordinate c2:c.getNeighbours()){
				//if a neighbour is in the Map (the vertex has already been created) then add an edge
				if(supergraphVertices.containsKey(c2)){
					//Create edge with a random label
					StringLabeledObject edge = StringLabeledObject.getRandomStringLabeledObject(alphabet);
					if(!directed || Math.random()<0.5){
						//if the graph is undirected OR random has chosen c->c2
						result.addEdge(supergraphVertices.get(c), supergraphVertices.get(c2), edge);
					} else{
						// c2->c
						result.addEdge(supergraphVertices.get(c2), supergraphVertices.get(c), edge);
					}
				}
			}
		}
		
		return result;
	}
	
	private void updateBorderCoordinates(Set<Coordinate> borderCoordinates, Coordinate removedElement, Set<Coordinate> exclude){
		//make sure that the "removed element" is actually removed
		borderCoordinates.remove(removedElement);
		//get all neighbouring coordinates of removedElement
		Set<Coordinate> neighbours = removedElement.getNeighbours();
		//Remove the coordinates that are already in the subgraphCoordinates
		neighbours.removeAll(subgraphCoordinates);
		if(exclude != null){
			neighbours.removeAll(exclude);
		}
		borderCoordinates.addAll(neighbours);
	}
}
