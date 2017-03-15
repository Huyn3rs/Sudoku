package examples;

import cspSolver.BTSolver;
import cspSolver.BTSolver.ConsistencyCheck;
import cspSolver.BTSolver.NakedCheck;
import cspSolver.BTSolver.ValueSelectionHeuristic;
import cspSolver.BTSolver.VariableSelectionHeuristic;
import sudoku.SudokuBoardGenerator;
import sudoku.SudokuBoardReader;
import sudoku.SudokuFile;

public class BTSolverExample {

	public static void main(String[] args)
	{
//		SudokuFile sf = SudokuBoardGenerator.generateBoard(12, 3, 4, 12);
		SudokuFile sf = SudokuBoardReader.readFile("ExampleSudokuFiles/PM1.txt");
		BTSolver solver = new BTSolver(sf);
		
//		solver.setConsistencyChecks(ConsistencyCheck.ForwardChecking);
//		solver.setValueSelectionHeuristic(ValueSelectionHeuristic.LeastConstrainingValue);
//		solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.MinimumRemainingValue);
//		solver.setNakedConsistency(NakedCheck.None);
		
		solver.setConsistencyChecks(ConsistencyCheck.ForwardChecking);
		solver.setValueSelectionHeuristic(ValueSelectionHeuristic.None);
		solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.None);
		solver.setNakedConsistency(NakedCheck.None);
		
		
		Thread t1 = new Thread(solver);
		try
		{
			t1.start();
			t1.join(60000);
			if(t1.isAlive())
			{
				t1.interrupt();
			}
		}catch(InterruptedException e)
		{
		}


		if(solver.hasSolution())
		{
			solver.printSolverStats();
			System.out.println(solver.getSolution());	
			boolean correct = solver.checkSolution();
			if (correct)
				System.out.println("Solution is correct.");
			else
				System.out.println("Solution is not correct.");
		}

		else
		{
			System.out.println("Failed to find a solution");
		}

	}
}
