package test;

import java.io.File;
import java.io.FileNotFoundException;

import org.jgrapht.Graph;

import algorithms.koch.Koch;
import algorithms.mcgregor.Mcgregor;

import shared.StringLabeledObject;
import view.GraphViewer;
import database.importexport.ImportExport;

public class AlgorithmTest {
	
	public static void main(String[] args){
		
		Graph<StringLabeledObject, StringLabeledObject> g1 = null;
    	Graph<StringLabeledObject, StringLabeledObject> g2 = null;
		try {
			g1 = ImportExport.importGraph(new File("37_s1.gml"));
			g2 = ImportExport.importGraph(new File("37_s2.gml"));
		} catch (FileNotFoundException e1) {
			throw new RuntimeException(e1);
		}

        //GraphViewer.showGraph(g1, "g1");
        //GraphViewer.showGraph(g2, "g2");
        long start = System.nanoTime();
        Graph<StringLabeledObject, StringLabeledObject> resultM1 = Mcgregor.maxCommonSubgraph(g1, g2, true);
        long time = System.nanoTime() - start;
        System.out.println("\n" + time);
        start = System.nanoTime();
        Graph<StringLabeledObject, StringLabeledObject> resultM2 = Koch.maxCommonSubgraph(g1, g2, true);
        time = System.nanoTime() - start;
        System.out.println("\n" + time);
        
        GraphViewer.showGraph(g1, "g1");
        GraphViewer.showGraph(g2, "g2");
        GraphViewer.showGraph(resultM1, "Mcgregor");
        GraphViewer.showGraph(resultM2, "Koch");
        System.out.println("\nMcgregor:" +resultM1.edgeSet().size() + " Koch3:" + resultM2.edgeSet().size());

		
		
	}
}
