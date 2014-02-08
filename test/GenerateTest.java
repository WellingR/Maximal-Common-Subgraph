package test;

import java.io.File;
import java.io.IOException;

import database.importexport.ImportExport;
import algorithms.koch.Koch;
import algorithms.mcgregor.Mcgregor;

import generator.mesh.RegularMeshGenerator;
import view.GraphViewer;

public class GenerateTest {
	public static void main(String[] args) throws IOException{
		//RandomlyConnectedGraphGenerator r = new RandomlyConnectedGraphGenerator(true, 12, 0.5, 0.2, 3);
		RegularMeshGenerator m = new RegularMeshGenerator(false, 9, 0.7, 2, 2, false);
		
		GraphViewer.showGraph(m.getSubgraph(), "generatedsubgraph");
		GraphViewer.showGraph(m.getSupergraph1(), "super1");
		GraphViewer.showGraph(m.getSupergraph2(), "super2");
		ImportExport.exportGraph(m.getSupergraph1(), new File("graph1.gml"));
		ImportExport.exportGraph(m.getSupergraph2(), new File("graph.gml"));
		GraphViewer.showGraph(Koch.maxCommonSubgraph(m.getSupergraph1(), m.getSupergraph2(), true), "KochSubgraph");
		GraphViewer.showGraph(Mcgregor.maxCommonSubgraph(m.getSupergraph1(), m.getSupergraph2(), true), "McGregorSubgraph");
		//GraphViewer.showGraph(mesh, "mesh");
	}
}
