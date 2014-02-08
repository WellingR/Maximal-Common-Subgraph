package database.importexport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.ext.GmlExporter;
import org.jgrapht.ext.IntegerEdgeNameProvider;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.graph.Multigraph;

import shared.StringLabeledObject;

public class ImportExport {
	
	public static final String DIRECTED = "directed";
	public static final String NODE = "node";
	public static final String EDGE = "edge";
	public static final String ID = "id";
	public static final String LABEL = "label";
	public static final String SOURCE = "source";
	public static final String TARGET = "target";
	
	public static void exportGraph(Graph<StringLabeledObject,StringLabeledObject> g, File f) throws IOException{
		GmlExporter<StringLabeledObject,StringLabeledObject> export =
			new GmlExporter<StringLabeledObject,StringLabeledObject>(
					new IntegerNameProvider<StringLabeledObject>(), null,
					new IntegerEdgeNameProvider<StringLabeledObject>(), null);
		export.setPrintLabels(GmlExporter.PRINT_EDGE_VERTEX_LABELS);
		
		FileWriter out = new FileWriter(f);
		if(g instanceof DirectedGraph){
			export.export(out, (DirectedGraph<StringLabeledObject, StringLabeledObject>) g);
		} else if(g instanceof UndirectedGraph){
			export.export(out, (UndirectedGraph<StringLabeledObject, StringLabeledObject>) g);
		}
		out.close();
	}
	
	
	public static Graph<StringLabeledObject,StringLabeledObject> importGraph(File f) throws FileNotFoundException{
		//System.err.println("File " +f.getName());
		Graph<StringLabeledObject,StringLabeledObject> result = null;
		
		Scanner scan = new Scanner(f);
		
		while(scan.hasNext() && !scan.next().equals(DIRECTED)); //go to directed
		int directed = scan.nextInt();
		//System.err.println("Directed:" + directed);
		if(directed == 1){
			result = new DirectedMultigraph<StringLabeledObject, StringLabeledObject>(StringLabeledObject.class);
		} else {
			result = new Multigraph<StringLabeledObject, StringLabeledObject>(StringLabeledObject.class);
		}
		Map<Integer,StringLabeledObject> vertices = new HashMap<Integer, StringLabeledObject>();
		
		while(scan.hasNext()){
			String s = scan.next();
			if(s.equals(NODE)){
				while(scan.hasNext() && !scan.next().equals(ID)); //go to id
				int id = scan.nextInt();
				String label = getLabel(scan);
				StringLabeledObject node = new StringLabeledObject(label);
				result.addVertex(node);
				vertices.put(id, node);
				//System.err.println("Found NODE " + id + ", label: " + label);
				
			} else if(s.equals(EDGE)){
				while(scan.hasNext() && !scan.next().equals(ID)); //go to id
				//int id = scan.nextInt();
				while(scan.hasNext() && !scan.next().equals(SOURCE)); //go to id
				int source = scan.nextInt();
				while(scan.hasNext() && !scan.next().equals(TARGET)); //go to id
				int target = scan.nextInt();
				String label = getLabel(scan);
				StringLabeledObject edge = new StringLabeledObject(label);
				result.addEdge(vertices.get(source), vertices.get(target), edge);
				//System.err.println("Found EDGE " + id + ", from " + source + ", to " + target + ", label: " + label);
			}
		}
		scan.close();
		
		
		return result;
		
	}
	
	private static final String getLabel(Scanner scan){
		while(scan.hasNext() && !scan.next().equals(LABEL)); //go to label
		String label = scan.next();
		return label.substring(1, label.length()-1);
	}
}
