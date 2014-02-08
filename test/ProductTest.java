package test;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedMultigraph;

import algorithms.koch.edgeproduct.EdgeProduct;
import algorithms.koch.edgeproduct.EdgeProductGraph;
//import org.jgrapht.graph.DirectedMultigraph;

import database.importexport.ImportExport;
import shared.StringLabeledObject;
import view.GraphViewer;

/**
 *
 * @author Ruud
 */
public class ProductTest {




    public static void main(String[] args){
//        DirectedMultigraph<StringLabeledObject,StringLabeledObject> g1 =
//            new DirectedMultigraph<StringLabeledObject,StringLabeledObject>(StringLabeledObject.class);
//
//        DirectedMultigraph<StringLabeledObject,StringLabeledObject> g2 =
//        	new DirectedMultigraph<StringLabeledObject,StringLabeledObject>(StringLabeledObject.class);
//        StringLabeledObject
//                v11 = new StringLabeledObject("a"),
//                v12 = new StringLabeledObject("b"),
//                v13 = new StringLabeledObject("b"),
//                v14 = new StringLabeledObject("b"),
//                v21 = new StringLabeledObject("a"),
//                v22 = new StringLabeledObject("a"),
//                v23 = new StringLabeledObject("a"),
//                v24 = new StringLabeledObject("b");
//        g1.addVertex(v11);
//        g1.addVertex(v12);
//        g1.addVertex(v13);
//        g1.addVertex(v14);
//        g2.addVertex(v21);
//        g2.addVertex(v22);
//        g2.addVertex(v23);
//        g2.addVertex(v24);
//
//        g1.addEdge(v11, v12, new StringLabeledObject("A"));
//        g1.addEdge(v11, v14, new StringLabeledObject("B"));
//        g1.addEdge(v11, v13, new StringLabeledObject("B"));
//
//        g2.addEdge(v21, v22, new StringLabeledObject("A"));
//        g2.addEdge(v21, v24, new StringLabeledObject("B"));
//        g2.addEdge(v22, v23, new StringLabeledObject("C"));
    	Graph<StringLabeledObject, StringLabeledObject> g1 = null;
    	Graph<StringLabeledObject, StringLabeledObject> g2 = null;
		try {
			g1 = ImportExport.importGraph(new File("g1.gml"));
			g2 = ImportExport.importGraph(new File("g2.gml"));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

        GraphViewer.showGraph(g1, "g1");
        GraphViewer.showGraph(g2, "g2");

        EdgeProductGraph<StringLabeledObject,StringLabeledObject,String> ep = new EdgeProductGraph<StringLabeledObject,StringLabeledObject,String>(g1, g2);

        GraphViewer.showGraph(ep, "ProductGraph");

        Set<EdgeProduct<StringLabeledObject>> vertices = algorithms.koch.Algorithm5.largest_C_Clique(ep);

        Graph<StringLabeledObject, StringLabeledObject> result = ep.toSubgraph(vertices);
        GraphViewer.showGraph(result, "Result");
        
        try {
        	ImportExport.exportGraph(g1, new File("labeltest1.gml"));
        	ImportExport.exportGraph(g2, new File("labeltest2.gml"));
			ImportExport.exportGraph(result, new File("result.gml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
