package test;

import org.jgrapht.Graph;

import shared.StringLabeledObject;
import algorithms.koch.Koch;
import database.DatabasePair;
import database.DatabaseReader;

public class DatabaseTest {
	
	public static void main(String[] args){
		new DatabaseTest().startTest();
	}
	
	private void startTest(){
		
		Graph<StringLabeledObject, StringLabeledObject> subgraph = null;
		for(DatabasePair pair: new DatabaseReader()){;
			if(pair.getGraph1().edgeSet().size() != pair.getSupergraphSize() || pair.getGraph2().edgeSet().size() != pair.getSupergraphSize()){
				//System.err.println(pair.getSourceDirectory().getName() + " " + pair.getNumberInDirectory());
				//System.err.println("Edges found: " + pair.getGraph1().edgeSet().size() + " , " + pair.getGraph2().edgeSet().size());
			} else {
				subgraph = Koch.maxCommonSubgraph(pair.getGraph1(), pair.getGraph2(), true);
				int size = (int) (pair.getSupergraphSize() * pair.getSubgraphPercent());
				if(subgraph.edgeSet().size() != size){
					System.err.println(pair.getSourceDirectory().getName() + " " + pair.getGraphPairNumber());
					System.err.println("Subgraph edges found: " + subgraph.edgeSet().size() + " should be " + size);
				} else {
					System.out.println(pair.getSourceDirectory().getName() + " " + pair.getGraphPairNumber());
				}
			}
			
			
		}
	}
}
