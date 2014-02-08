package algorithms.mcgregor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.graph.Multigraph;
import org.jgrapht.graph.Subgraph;

import shared.LabeledObject;
import shared.SharedStaticMethods;


public class Marcs<V extends LabeledObject<L>,E extends LabeledObject<L> ,L> {
	
	private List<E> edgeList1, edgeList2;
	private List<V> mappedVerticesFromG1;
	private List<V> mappedVerticesFromG2;
	private Graph<V,E> g1, g2;
	private boolean[][] matrix;
	private boolean[] rowOrs;
	private int dimx, dimy;
	private boolean directed, connectedOnly;
	private Graph<V,E> subgraph = null;
	//remembers the amount of times a label has been used in the mappings
	private int[] labelCount;
	
	
	private Marcs(Marcs<V,E,L> toCopy){
		this.dimx = toCopy.dimx;
		this.dimy = toCopy.dimy;
		this.edgeList1 = toCopy.edgeList1;
		this.edgeList2 = toCopy.edgeList2;
		this.mappedVerticesFromG1 = new ArrayList<V>(toCopy.mappedVerticesFromG1);
		this.mappedVerticesFromG2 = new ArrayList<V>(toCopy.mappedVerticesFromG2);
		matrix = new boolean[dimx][dimy];
		rowOrs = toCopy.rowOrs.clone();
		for(int x=0;x<dimx;x++){
			for(int y=0;y<dimy;y++){
				this.matrix[x][y] = toCopy.matrix[x][y];
			}
		}
		this.directed = toCopy.directed;
		this.connectedOnly = toCopy.connectedOnly;
		this.g1 = toCopy.g1;
		this.g2 = toCopy.g2;
		labelCount = toCopy.labelCount.clone();
	}
	
	public Marcs(Graph<V,E> g1, Graph<V,E> g2, boolean connectedOnly, int labelSize){
		this.g1 = g1;
		this.g2 = g2;
		edgeList1 = new ArrayList<E>(g1.edgeSet());
		edgeList2 = new ArrayList<E>(g2.edgeSet());
		mappedVerticesFromG1 = new ArrayList<V>();
		mappedVerticesFromG2 = new ArrayList<V>();
		dimx = edgeList1.size();
		dimy = edgeList2.size();
		directed = g1 instanceof DirectedGraph && g2 instanceof DirectedGraph;
		this.connectedOnly = connectedOnly;
		matrix = new boolean[dimx][dimy];
		rowOrs = new boolean[dimx];
		for(int x=0;x<dimx;x++){
			rowOrs[x] = true;
			for(int y=0;y<dimy;y++){
				matrix[x][y] = edgesCompatible(x,y);
			}
		}
		labelCount = new int[labelSize];
		for(int i=0;i<labelSize;i++){
			labelCount[i] =0;
		}
	}
	
	public void incrementLabelCount(int i){
		labelCount[i]++;
	}
	
	public int getLabelCount(int index){
		return labelCount[index];
	}
	
	/**
	 * Returns a List with unique elements, 
	 * @param v1 vertex from g1
	 * @return
	 */
	public List<V> getPrioritySubset(V v1){
		//The resulting list
		List<V> result = new ArrayList<V>();
		
		//list of already mapped vertices that neighbour v1
		List<V> v1Others = new ArrayList<V>();
		
		V v1other;
		V v2other;
		for(E e1 : g1.edgesOf(v1)){
			v1other = Graphs.getOppositeVertex(g1, e1, v1);
			if(mappedVerticesFromG1.contains(v1other)){
				v1Others.add(v1other);
			}
		}
		for(V v2 : g2.vertexSet()){
			//if v2's label is the same of v1's label and v2 has not been mapped yet
			if(v1.getLabel().equals(v2.getLabel()) && !mappedVerticesFromG2.contains(v2)){
				//test if there is an edge to a vertex which has already been mapped
				for(E e2 : g2.edgesOf(v2)){
					v2other = Graphs.getOppositeVertex(g2, e2, v2);
					//if the vertex v2other has already been mapped
					if(mappedVerticesFromG2.contains(v2other)){
						//labels are not checked, this is done at a later stage anyway and doing it twice is not needed and takes too much time
						result.add(v2);
						break;
					}
				}
			}
		}
		return result;
	}
	
	private boolean edgesCompatible(int edge1, int edge2){
		E e1 = edgeList1.get(edge1);
		E e2 = edgeList2.get(edge2);
		boolean result = false;
		//edge label must be the same
		if(e1.getLabel().equals(e2.getLabel())){
			//check connecting vertex labels
			L v1SourceLbl = g1.getEdgeSource(e1).getLabel(), v1TargetLbl = g1.getEdgeTarget(e1).getLabel(),
			v2SourceLbl = g2.getEdgeSource(e2).getLabel(), v2TargetLbl = g2.getEdgeTarget(e2).getLabel();
			//checks if the pair of source vertices have the same label, and checks the same for the target vertices
			boolean sourceTargetMatch = v1SourceLbl.equals(v2SourceLbl) && v1TargetLbl.equals(v2TargetLbl);
			if(directed){
				result = sourceTargetMatch;
			} else{
				//checks if source1,target2 have the same label and if target1,source2 have the same label
				boolean sourceTargetInverseMatch = v1SourceLbl.equals(v2TargetLbl) && v1TargetLbl.equals(v2SourceLbl);
				result = (sourceTargetMatch || sourceTargetInverseMatch);
			}
		}
		return result;
	}
	
	/**
	 * @return the arcsLeft
	 * The variable arcsleft is used to keep track of the number of arcs which
	 * could still correspond in a node correspondeces based on the current partial correspondence.
	 * Wheenever a refinement of MARCS results in a row being set to zero (this happens when two adjacent nodes in G1,
	 * have been tentatively mapped to nonadjacent nodes in G2) arcsleft is decremented by one.
	 */
	public int getArcsLeft() {
		//this method takes the maximum value of arcsleft ( edgeList1.size() ) and
		//decrements for every row that is completely set to false
		int result = edgeList1.size();
		boolean rowOr;
		for(int x=0;x<dimx;x++){
			if(!rowOrs[x]){
				result--;
				continue;
			}
			rowOr = false;
			for(int y=0;y<dimy;y++){
				rowOr|= matrix[x][y];
			}
			rowOrs[x] = rowOr;
			if(!rowOr) result--;
		}		
		return result;
	}
	
	public int getArcsInBiggestConnectedGraph(){
		return toGraph().edgeSet().size();
	}
	
	/**
	 * @return the amount of arcs that are connected on both sides to nodes that have been mapped so far
	 */
	public int getArcsLeftForMappedVertices() {
		int result = 0;
		boolean columnOr;
		for(int y=0;y<dimy;y++){
			columnOr = false;
			for(int x=0;x<dimx;x++){
				columnOr|= matrix[x][y];
			}
			if(columnOr){
				//if Edge edgeList2.get(y) is connected (on both sides) to vertices in mappedVerticesFromG2
				if(mappedVerticesFromG2.contains(g2.getEdgeSource(edgeList2.get(y)))
						&& mappedVerticesFromG2.contains(g2.getEdgeSource(edgeList2.get(y)))){
					result++;
				}
			}
			
		}
		return result;
	}
	
	/**
	 * 
	 * @param v1 node from G1
	 * @param v2 new node from G2, this node is remembered as an added node
	 */
	/* Each 
	time a node in G1 is tentatively paired with a node in G2, MARCS is refined on the 
	basis of this node correspondence. Say node i  in G1 is tentatively paired with node j in 
	G2. Then any arc r connected to node i in G1 can correspond only to arcs which are 
	connected to node j in G2. This is represented in MARCS by setting to zero any bit 
	(r, s) such that arc r is connected to node i in G, and arc s is not connected to node j in 
	G2. */
	public void refine(V v1, V v2){
		//marcs is modified, make sure subgraph is set to null (to prevent errors)
		subgraph = null;
		
		mappedVerticesFromG1.add(v1);
		mappedVerticesFromG2.add(v2);
		List<Integer> edgesV1Ints = null;
		List<Integer> edgesV2Ints = null;
		List<Integer> edgesV1OutInts = null;
		List<Integer> edgesV1InInts = null;
		List<Integer> edgesV2OutInts = null;
		List<Integer> edgesV2InInts = null;
		if(!directed){
			edgesV1Ints = new ArrayList<Integer>();
			edgesV2Ints = new ArrayList<Integer>();
			for(E e1 : g1.edgesOf(v1)){
				edgesV1Ints.add(edgeList1.indexOf(e1));
			}
			for(E e2 : g2.edgesOf(v2)){
				edgesV2Ints.add(edgeList2.indexOf(e2));
			}
		} else { //directed
			edgesV1OutInts = new ArrayList<Integer>();
			edgesV1InInts = new ArrayList<Integer>();
			edgesV2OutInts = new ArrayList<Integer>();
			edgesV2InInts = new ArrayList<Integer>();
			for(E e1 : ((DirectedGraph<V,E>) g1).outgoingEdgesOf(v1)){
				edgesV1OutInts.add(edgeList1.indexOf(e1));
			}
			for(E e1 : ((DirectedGraph<V,E>) g1).incomingEdgesOf(v1)){
				edgesV1InInts.add(edgeList1.indexOf(e1));
			}
			for(E e2 : ((DirectedGraph<V,E>) g2).outgoingEdgesOf(v2)){
				edgesV2OutInts.add(edgeList2.indexOf(e2));
			}
			for(E e2 : ((DirectedGraph<V,E>) g2).incomingEdgesOf(v2)){
				edgesV2InInts.add(edgeList2.indexOf(e2));
			}
		}
		for(int x=0;x<dimx;x++){
			if(!rowOrs[x]) continue;
			for(int y=0;y<dimy;y++){
				//if the only one of the two edges x and y are connected to v1 or v2 then x and y are not compatible and the matrix should be set to false
				if(matrix[x][y]){
					if(directed){
						if((edgesV1OutInts.contains(x) && !edgesV2OutInts.contains(y))
								|| (!edgesV1OutInts.contains(x) && edgesV2OutInts.contains(y))
								|| (edgesV1InInts.contains(x) && !edgesV2InInts.contains(y))
								|| (!edgesV1InInts.contains(x) && edgesV2InInts.contains(y))){
							matrix[x][y] = false;							
						}
					} else { //undirected
						if((edgesV1Ints.contains(x) && !edgesV2Ints.contains(y))
								|| (!edgesV1Ints.contains(x) && edgesV2Ints.contains(y))){
							matrix[x][y] = false;
						}
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @param connectedOnly if true, the result will be a connected graph
	 * @return
	 */
	public Graph<V,E> toGraph(){
		if(subgraph != null) return subgraph;
		if(directed){
			subgraph = new DirectedMultigraph<V,E>(g2.getEdgeFactory());
		} else {
			subgraph = new Multigraph<V,E>(g2.getEdgeFactory());
		}
		
		E edge;
		V source;
		V target;
		for(int x=0;x<dimx;x++){
			for(int y=0;y<dimy;y++){
				if(matrix[x][y]){
					edge = edgeList2.get(y);
					source = g2.getEdgeSource(edge);
					target = g2.getEdgeTarget(edge);
					if(mappedVerticesFromG2.contains(source) && mappedVerticesFromG2.contains(target)){
						//make sure the source and target vertices have been added, then add the edge
						subgraph.addVertex(source);
						subgraph.addVertex(target);
						subgraph.addEdge(source, target, edge);
					}
					
				}
			}
		}
		
		if(connectedOnly){
			//make sure this subgraph is connected, if it is not return the largest connected part
			List<Set<V>> connectedVertices = new ArrayList<Set<V>>();
			for(V v : subgraph.vertexSet()){
				if(!SharedStaticMethods.containsV(connectedVertices, v)){
					connectedVertices.add(SharedStaticMethods.getConnectedVertices(subgraph, v));
				}
			}
			//ConnectedVertices now contains Sets of connected vertices every vertex of the subgraph is contained exactly once in the list
			//if there is more then 1 set, then this method should return the largest connected part of the graph
			if(connectedVertices.size() > 1){
				Graph<V,E> largestResult = null;
				Graph<V,E> currentGraph;
				int largestSize = -1;
				Set<V> currentSet;
				for(int i=0;i<connectedVertices.size();i++){
					currentSet = connectedVertices.get(i);
					/*note that 'subgraph' is the result from the Mcgregor algorithm, 'currentGraph' is an
					 * induced subgraph of 'subgraph'. 'currentGraph' is connected, because the vertices in
					 * 'currentSet' are connected with edges in 'subgraph'
					 */
					currentGraph = new Subgraph<V,E,Graph<V,E>>(subgraph, currentSet);
					if(currentGraph.edgeSet().size() > largestSize){
						largestResult = currentGraph;
					}
				}
				
				return largestResult;
			}
		}
		
		
		return subgraph;
	}
	
	public Marcs<V,E,L> getCopy(){
		return new Marcs<V, E, L>(this);
	}
	
	public String toString(){
		String result = "";
		int width = 1 + 4*dimy;
		String hline = "\n";
		for(int i=0;i<width;i++){
			hline += "-";
		}
		for(int x=0;x<dimx;x++){
			result += hline + "\n";
			result += "|";
			for(int y=0;y<dimy;y++){
				if(matrix[x][y]) {
					result += " 1 |";
				} else {
					result += " 0 |";
				}
			}
		}
		result +=  hline;
		return result;
	}
}
