/*

  (c) Nathaniel Lim
  CS 256, HW 3, #4
  Williams College, 3/3/09

  Basic Algorithm to find the locations where there should be cameras:

  - Maintain a master copy of the original graph
  - Iterate through all the nodes in the graph
  - delete the given node from the graph, and all edges
  - Perform BFS (starting at any element)
  - If the size of the BFS tree is < (n-1) the graph is no longer connected
    meaning that it was a place that needed to be passed through in a path
    between some pair of nodes
  - Add the node that was deleted to the return set

 */


import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import java.io.File;
import java.lang.Integer;
import java.util.ArrayList;
import java.util.Collections;

class Graph {

    HashMap<Node, HashSet<Node>> adjList = new HashMap<Node, HashSet<Node>>();
    
    public Graph(){
    }

    public void addEdge(Node x, Node y){
	if (!adjList.containsKey(x) || !adjList.containsKey(y)){
	    return;
	}
	HashSet<Node> s = adjList.get(x);
	HashSet<Node> r = adjList.get(y);
	s.add(y);
	r.add(x);
    }

    public void addNode(Node x){
	adjList.put (x, new HashSet<Node>());
    }

    public HashSet<Node> getAdj(Node x){
	return adjList.get(x);
    }

    public Set<Node> getNodes(){
	return adjList.keySet();
    }

    
    @SuppressWarnings("unchecked")
    public Graph clone(){
	Graph output = new Graph();
	Set<Node> s = this.getNodes();
	for (Node i: s){
	    output.adjList.put(i, (HashSet<Node>)this.getAdj(i).clone());
	}
	return output;
    }


    public Node removeNode(Node x){
	if (adjList.containsKey(x)){
	    HashSet<Node> adjNodes = adjList.get(x);
	    // Removing all edges referencing x
	    for (Node n: adjNodes){
		HashSet<Node> s = adjList.get(n);
		s.remove(x);
	    }

	    //Remove the node x itself from graph
	    adjList.remove(x);
	    return x;		
	} else {
	    return null;
	}


    }

    public String toString(){
	String output = "";
	Set<Node> s = adjList.keySet();
	for (Node n: s){
	    output += n + ": {";
	    Set<Node> a = adjList.get(n);
	    for (Node b: a){
		output+=b.label +", ";
	    }
	    output+="}\n";
	}
	return output;
    }
}


class Node  {

    String label;

    public String toString(){
	return label;
    }
    
    public Node(String label){
	this.label = label;
    }

  
    
    public boolean equals(Object o){
	if (o instanceof Node){
	    Node a = (Node)o;
	    return this.label.equals(a.label);
	} else {
	    return false;
	}
    }

    public int hashCode(){
	return this.label.hashCode();
    }  


}


class Main {


    public static void printSet(Set x){
	String output = "{";
	for (Object d: x){
	    output+=", " + d;
	}
	output+="}";
	
	System.out.println(output);
    }

    public static void main (String [] args) throws java.io.FileNotFoundException {
	Scanner s = new Scanner(System.in);	    
	int map = 1;
	while (true){
	    int n = Integer.parseInt(s.nextLine());
	    if (n ==0){
		s.close();
		return;
	    }
	    Graph g = new Graph();
	    for (int i = 0; i < n; i++){
		Node temp = new Node(s.nextLine());
		g.addNode(temp);		
	    }
	    
	    
	    int e = Integer.parseInt(s.nextLine());
	    for (int i = 0; i < e; i++){
		String [] result = s.nextLine().split(" ");
		g.addEdge(new Node(result[0]), new Node(result[1]));				
	    }
	    
	    
	    Set<Node> cameras = new HashSet<Node>();
	    
	    // Graph Set up, now iterate through nodes, deleting each;
	    
	    Graph tempGraph;
	    Set<Node> nodes = g.getNodes();
	    int graphSize = nodes.size();
	    
	    
	    for (Node i: nodes){
		tempGraph = g.clone();		    
		tempGraph.removeNode(i); 
		
		Set<Node> remaining = tempGraph.getNodes();
		Set<Node> discovered = new HashSet<Node>();
		Set<Node> tempDiscovered = new HashSet<Node>();
		//Choose any element in remaining set of nodes
		//PERFORM BFS, adding nodes to discovered
		// when done, if discovered.size() < size - 1
		// then the graph: (g - i) disjoint
		
		Object[] remArray = remaining.toArray();
		Node start = (Node)remArray[0];
		tempDiscovered.add(start);
		discovered.add(start);
		
		
		
		//BFS
		while (tempDiscovered.size() > 0){
		    discovered.addAll(tempDiscovered);			
		    tempDiscovered = new HashSet<Node>();
		    for (Node q: discovered){
			HashSet<Node> tempAdj = tempGraph.getAdj(q);
			for (Node a: tempAdj){
			    if (!discovered.contains(a)){
				//If an adjacent node hasn't been discovered
				//add it to discovered
				tempDiscovered.add(a);
			    }
			}
		    }
		}
		
		
		//  If tempGraph was connected, it should be of size: graphSize -1;
		if (discovered.size() < graphSize - 1){
		    cameras.add(i); //The Node originally removed			
		}		    
		
	    }
	    
	    
	    System.out.println("City map #" + map + ": " + cameras.size() + " camera(s) found");
	    
	    
	    ArrayList<String> toSort = new ArrayList<String>();
	    //Put into alphabetical order
	    for (Node c: cameras){
		toSort.add(c.label);
	    }
	    Collections.sort(toSort);
	    for (String p: toSort){
		System.out.println(p);
	    }
	    System.out.println("");
	    map++;;
	}
	
	
    }

}