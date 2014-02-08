package test;

import java.io.File;
import java.io.IOException;

import org.jgrapht.Graph;

import database.importexport.ImportExport;

import generator.mesh.RegularMeshGenerator;
import generator.randomlyconnected.RandomlyConnectedGraphGenerator;

import shared.StringLabeledObject;

public class ExportTest {
	public static void main(String[] args){
		Graph<StringLabeledObject, StringLabeledObject> random1 = new RandomlyConnectedGraphGenerator(true, 10, 7, 0.4, 1, false).getSubgraph();
		Graph<StringLabeledObject, StringLabeledObject> random2 = new RandomlyConnectedGraphGenerator(true, 10, 7, 0.4, 1, false).getSubgraph();
		Graph<StringLabeledObject, StringLabeledObject> mesh1 = new RegularMeshGenerator(false, 5, 0.8, 1, 2, false).getSubgraph();
		Graph<StringLabeledObject, StringLabeledObject> mesh2 = new RegularMeshGenerator(false, 5, 0.8, 1, 2, false).getSubgraph();
		
		try {
			ImportExport.exportGraph(random1, new File("Random1.gml"));
			ImportExport.exportGraph(random2, new File("Random2.gml"));
			ImportExport.exportGraph(mesh1, new File("mesh1.gml"));
			ImportExport.exportGraph(mesh2, new File("mesh2.gml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Done");
	}
}
