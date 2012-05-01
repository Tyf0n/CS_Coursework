package search;

import java.util.Iterator;
import java.util.List;

public class SearchProgram {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("Arguments are:");
			System.out.println("        algorithm: dls, bfs or astar");
			System.out.println("        puzzle: 5Puzzle, coins, water or 8Queens");
			System.out.println("        startString: start state description for puzzle");
			System.out.println("        depth: depth of search for dls");
			return;
		}
		String algString = args[0];
		String puzzleString = args[1];
		String stateString = args[2];
		ProblemGraph puzzle = null;
		if (puzzleString.equals("5Puzzle")) {
			puzzle = new FivePuzzle();
		}
		else if (puzzleString.equals("coins")) {
		    puzzle = new CoinPuzzle();

		}
		else if (puzzleString.equals("water")) {
		    puzzle = new JugsPuzzle();

		}
		else if (puzzleString.equals("8Queens")) {
		    puzzle = new EightQueensPuzzle(); 
		}
		else {
			System.out.println("Invalid puzzle choice.");
			return;
		}

		Search algorithm = null;
		if (algString.equals("dls")) {
			if (args.length < 4) {
				System.out.println("Error: dls requires a depth parameter.");
			}
			String depthString = args[3];
			int depth = Integer.parseInt(depthString);
			algorithm = new DepthLimitedSearch(depth);
		} else if (algString.equals("bfs")) {
		    algorithm = new BreadthFirstSearch();
		    
		} else if (algString.equals("astar")) {
		    algorithm = new AStarSearch();
		    
		} else {
			System.out.println("Invalid algorithm choice.");
			return;
		}

		List<State> result = algorithm.pathSearch(puzzle, stateString);
        if(result != null) {
	    String output = ")";
            
            for (Iterator<State> iter = result.iterator(); iter.hasNext();) {
		State element = iter.next();
		output = element.toString() + output;
            }
	    output = "(" + output;
            System.out.println(output);
        } else {
            System.out.println("The search did not result in a solution.");
        }

	}

}
