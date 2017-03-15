package scripts;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sudoku.SudokuBoardReader;
import sudoku.SudokuFile;
import cspSolver.BTSolver;
import cspSolver.BTSolver.ConsistencyCheck;
import cspSolver.BTSolver.NakedCheck;
import cspSolver.BTSolver.ValueSelectionHeuristic;
import cspSolver.BTSolver.VariableSelectionHeuristic;

public class MyBTSolverStats {
	private static final int NUMBER_OF_EASY_PROBLEMS = 50;
	private static final int NUMBER_OF_MEDIUM_PROBLEMS = 5;
	private static final int NUMBER_OF_HARD_PROBLEMS = 5;
	private static final int NUMBER_OF_TESTS = 5;
	
	private static ArrayList<Integer> easy = new ArrayList<Integer>();
	private static ArrayList<Integer> medium = new ArrayList<Integer>();
	private static ArrayList<Integer> hard = new ArrayList<Integer>();
	private static String sep = System.getProperty("line.separator");
	
	public static void setup()
	{
		for (int i = 0; i < NUMBER_OF_EASY_PROBLEMS; i++)
			easy.add(i+1);
		for (int i = 0; i < NUMBER_OF_MEDIUM_PROBLEMS; i++)
			medium.add(i+1);
		for (int i = 0; i < NUMBER_OF_HARD_PROBLEMS; i++)
			hard.add(i+1);
		
		Collections.shuffle(easy);
		Collections.shuffle(medium);
		Collections.shuffle(hard);
		
	}

	// Change these to test different configurations for the solver. 
	static ConsistencyCheck cc = ConsistencyCheck.ForwardChecking;
	static ValueSelectionHeuristic valsh = ValueSelectionHeuristic.LeastConstrainingValue;
	static VariableSelectionHeuristic varsh = VariableSelectionHeuristic.MinimumRemainingValue;
	static NakedCheck nc = NakedCheck.None;
	
	public static List<SudokuFile> getPuzzlesFromFolder(File folder) {
	    List<SudokuFile> puzzles = new ArrayList<SudokuFile>();
		for (File fileEntry : folder.listFiles()) {
    		SudokuFile sfPE = SudokuBoardReader.readFile(fileEntry.getPath());
    		if(sfPE.getN() != 0)
    		{
    			puzzles.add(sfPE);
    		}
	    }
		return puzzles;
	}
	
	public static runStats testSolver(BTSolver solver)
	{
		solver.setConsistencyChecks(cc);
		solver.setValueSelectionHeuristic(valsh);
		solver.setVariableSelectionHeuristic(varsh);
		solver.setNakedConsistency(nc);
		
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
		long runtime;
		int numAssignments;
		int numBacktracks;
		boolean isSolution;
		runStats rs;
		runtime = solver.getTimeTaken();
		numAssignments = solver.getNumAssignments();
		numBacktracks = solver.getNumBacktracks();
		isSolution = solver.hasSolution() && solver.checkSolution(); //I MODIFIED THIS
		rs = new runStats(runtime, numAssignments, numBacktracks, isSolution);
		return rs;
	}
	
	public static void writeHeuristics(FileWriter fw)
	{
		try{
			System.out.println("ConsistencyCheck: " + cc);
			System.out.println("ValueSelectionHeuristic: " + valsh);
			System.out.println("VariableSelectionHeuristic: " + varsh);
			System.out.println("NakedCheck: " + nc);
			System.out.println();
			
			fw.write("ConsistencyCheck: " + cc + sep);
			fw.write("ValueSelectionHeuristic: " + valsh + sep);
			fw.write("VariableSelectionHeuristic: " + varsh + sep);
			fw.write("NakedCheck: " + nc + sep);
			fw.write(sep);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void writeStatistics(FileWriter fw, List<runStats> statistics)
	{
		try{
			long totalRunTime = 0;
			long totalAssignments = 0;
			long totalBackTracks = 0;
			int totalSuccessful = 0;
			int totalPuzzles = 0;
			
			for(runStats rs : statistics)
			{
				if(rs.isSolution())
				{
					totalRunTime += rs.getRuntime();
					totalAssignments += rs.getNumAssignments();
					totalBackTracks += rs.getNumBacktracks();
					totalSuccessful++;
				}
				totalPuzzles++;
			}
			System.out.println();
			System.out.println("Solution found for " + totalSuccessful + "/" + totalPuzzles + "puzzles");
			System.out.println("average runTime: " + (totalRunTime/totalSuccessful));
			System.out.println("average number of assignments per puzzle: " + (totalAssignments/totalSuccessful));
			System.out.println("average number of backtracks per puzzle: " + (totalBackTracks/totalSuccessful));
			System.out.println();
			
			fw.write(sep);
			fw.write("Solution found for " + totalSuccessful + "/" + totalPuzzles + "puzzles" + sep);
			fw.write("average runTime: " + (totalRunTime/totalSuccessful) + sep);
			fw.write("average number of assignments per puzzle: " + (totalAssignments/totalSuccessful) + sep);
			fw.write("average number of backtracks per puzzle: " + (totalBackTracks/totalSuccessful) + sep);
			fw.write(sep);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void runTestAndRecord(FileWriter fw, String name, List<Integer> numbers)
	{
		try{
			String folder = "ExampleSudokuFiles/";
			List<runStats> statistics = new ArrayList<runStats>();
			System.out.println("Difficulty: " + name);
			fw.write("Difficulty: " + name + sep);
			
			for (int i = 0; i < NUMBER_OF_TESTS; i++)
			{
				String file = "P" + name.charAt(0) + numbers.get(i) + ".txt";
				String path = folder + file;
				SudokuFile sf = SudokuBoardReader.readFile(path);
				
				BTSolver solver = new BTSolver(sf);
				runStats rs = testSolver(solver);
				statistics.add(rs);
				
				System.out.println(rs.toString()+ "(" + file + ")");
				fw.write(rs.toString() + "(" + file + ")" + sep);
				
			}
			writeStatistics(fw, statistics);
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		setup();
		File results = new File("BTSolverResults.txt");
		int count = 1;
		while(results.exists())
		{
			results = new File("BTSolverResults" + count++ + ".txt");
		}
		
		
		try {
			FileWriter fw = new FileWriter(results);
			
			writeHeuristics(fw);
			
			runTestAndRecord(fw, "Easy", easy);
			runTestAndRecord(fw, "Easy", easy);
			runTestAndRecord(fw, "Easy", easy);
//			runTestAndRecord(fw, "Medium", medium);
//			runTestAndRecord(fw, "Hard", hard);
			
			fw.flush();
			fw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
}
