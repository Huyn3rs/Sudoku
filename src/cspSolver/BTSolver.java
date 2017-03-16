package cspSolver;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import sudoku.Converter;
import sudoku.SudokuFile;
/**
 * Backtracking solver. 
 *
 */
public class BTSolver implements Runnable{

	//===============================================================================
	// Properties
	//===============================================================================

	private ConstraintNetwork network;
	private static Trail trail = Trail.getTrail();
	private boolean hasSolution = false;
	private SudokuFile sudokuGrid;

	private int numAssignments;
	private int numBacktracks;
	private long startTime;
	private long endTime;
	
	public enum VariableSelectionHeuristic 	{ None, MinimumRemainingValue, Degree }
	public enum ValueSelectionHeuristic 		{ None, LeastConstrainingValue }
	public enum ConsistencyCheck				{ None, ForwardChecking, ArcConsistency }
        public enum NakedCheck    { None, NakedPairs, NakedTriples }
	
	private VariableSelectionHeuristic varHeuristics;
	private ValueSelectionHeuristic valHeuristics;
	private ConsistencyCheck cChecks;
        private NakedCheck nCheck;
	//===============================================================================
	// Constructors
	//===============================================================================

	public BTSolver(SudokuFile sf)
	{
		this.network = Converter.SudokuFileToConstraintNetwork(sf);
		this.sudokuGrid = sf;
		numAssignments = 0;
		numBacktracks = 0;
	}

	//===============================================================================
	// Modifiers
	//===============================================================================
	
	public void setVariableSelectionHeuristic(VariableSelectionHeuristic vsh)
	{
		this.varHeuristics = vsh;
	}
	
	public void setValueSelectionHeuristic(ValueSelectionHeuristic vsh)
	{
		this.valHeuristics = vsh;
	}
	
	public void setConsistencyChecks(ConsistencyCheck cc)
	{
		this.cChecks = cc;
	}

        public void setNakedConsistency(NakedCheck nck)
        {
                this.nCheck = nck;
        }
	//===============================================================================
	// Accessors
	//===============================================================================

	/** 
	 * @return true if a solution has been found, false otherwise. 
	 */
	public boolean hasSolution()
	{
		return hasSolution;
	}

	/**
	 * @return solution if a solution has been found, otherwise returns the unsolved puzzle.
	 */
	public SudokuFile getSolution()
	{
		return sudokuGrid;
	}

	public void printSolverStats()
	{
		System.out.println("Time taken:" + (endTime-startTime) + " ms");
		System.out.println("Number of assignments: " + numAssignments);
		System.out.println("Number of backtracks: " + numBacktracks);
	}

	/**
	 * 
	 * @return time required for the solver to attain in seconds
	 */
	public long getTimeTaken()
	{
		return endTime-startTime;
	}

	public int getNumAssignments()
	{
		return numAssignments;
	}

	public int getNumBacktracks()
	{
		return numBacktracks;
	}

	public ConstraintNetwork getNetwork()
	{
		return network;
	}

	//===============================================================================
	// Helper Methods
	//===============================================================================

	/**
	 * Checks whether the changes from the last time this method was called are consistent. 
	 * @return true if consistent, false otherwise
	 */
	private boolean checkConsistency()
	{
		boolean isConsistent = false;
		switch(cChecks)
		{
		case None: 				isConsistent = assignmentsCheck();
		break;
		case ForwardChecking: 	isConsistent = forwardChecking();
		break;
		case ArcConsistency: 	isConsistent = arcConsistency();
		break;
		default: 				isConsistent = assignmentsCheck();
		break;
		}
		return isConsistent;
	}

        /**
	 * Checks whether the changes from the last time this method was called are consistent. 
	 * @return true if consistent, false otherwise
	 */
	private boolean checkNakedConsistency()
	{
		boolean isConsistent = false;
		switch(nCheck)
		{
		case None: 				isConsistent = true;
		break;
                case NakedPairs:    isConsistent = nakedPairs();
                break;
                case NakedTriples:    isConsistent = nakedTriples();
                break;
		default: 				isConsistent = true;
		break;
		}
		return isConsistent;
	}
	
	/**
	 * default consistency check. Ensures no two variables are assigned to the same value.
	 * @return true if consistent, false otherwise. 
	 */
	private boolean assignmentsCheck()
	{
		for(Variable v : network.getVariables())
		{
			if(v.isAssigned())
			{
				for(Variable vOther : network.getNeighborsOfVariable(v))
				{
					if (v.getAssignment() == vOther.getAssignment())
					{
						return false;
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * TODO: Implement forward checking. 
	 */
	
	// Fix this!!
	private boolean forwardChecking()
	{
		for(Variable v : network.getVariables())
		{
			if(v.isAssigned())
			{
				for(Variable vOther : network.getNeighborsOfVariable(v))
				{	
					if (vOther.getDomain().contains(v.getAssignment()))		
						vOther.removeValueFromDomain(v.getAssignment());
					
					if (vOther.size() == 0)
					{
						return false;
					}
					
				}
			}
		}
		return true;
	}
	
	/**
	 * TODO: Implement Maintaining Arc Consistency.
	 */
	private boolean arcConsistency()
	{
		
		return false;
	}

	/**
	 * TODO: Implement naked pairs. 
	 */
	private boolean nakedPairs()
	{
		Variable skip = null;
		for (Variable x: network.getVariables())
		{
			if (skip == x)
				continue;
			if (x.getDomain().size() == 2 && !x.isAssigned())
			{
				for (Variable y: network.getNeighborsOfVariable(x))
				{
					if (y.getDomain().size() == 2 && !y.isAssigned()){
						if (x.getDomain().getValues().containsAll(y.getDomain().getValues())){
							skip = y;
							for (Variable v: network.getVariables()){
								if (!v.isAssigned() && 
									network.getNeighborsOfVariable(x).contains(v) &&
									network.getNeighborsOfVariable(y).contains(v))
								{
									v.removeValueFromDomain(x.getDomain().getValues().get(0));
									v.removeValueFromDomain(x.getDomain().getValues().get(1));
								}
							}
						}
					}
				}
				
			}
			
		}
		return assignmentsCheck();
	}
	
	/**
	 * TODO: Implement naked triples.
	 */
	private boolean nakedTriples()
	{
		return false;
	}

	/**
	 * Selects the next variable to check.
	 * @return next variable to check. null if there are no more variables to check. 
	 */
	private Variable selectNextVariable()
	{
		Variable next = null;
		switch(varHeuristics)
		{
		case None: 					next = getfirstUnassignedVariable();
		break;
		case MinimumRemainingValue: next = getMRV();
		break;
		case Degree:				next = getDegree();
		break;
		default:					next = getfirstUnassignedVariable();
		break;
		}
		return next;
	}
	
	/**
	 * default next variable selection heuristic. Selects the first unassigned variable. 
	 * @return first unassigned variable. null if no variables are unassigned. 
	 */
	private Variable getfirstUnassignedVariable()
	{
		for(Variable v : network.getVariables())
		{
			if(!v.isAssigned())
			{
				return v;
			}
		}
		return null;
	}

	/**
	 * TODO: Implement MRV heuristic
	 * @return variable with minimum remaining values that isn't assigned, null if all variables are assigned. 
	 */
	private Variable getMRV()
	{
		Variable temp = null;
		int minimum = Integer.MAX_VALUE;
		for(Variable v : network.getVariables())
		{
			if(!v.isAssigned() && v.size() < minimum )
			{
				temp = v;
				minimum = v.size();
			}
		}
		return temp;
	}
	
	/**
	 * TODO: Implement Degree heuristic
	 * @return variable constrained by the most unassigned variables, null if all variables are assigned.
	 */
	private Variable getDegree()
	{
		Variable temp = null;
		int maximum = -1;
		for(Variable v : network.getVariables())
		{
			if(!v.isAssigned())
			{
				int max = 0;
				for(Variable vOther : network.getNeighborsOfVariable(v))
				{
					if (!vOther.isAssigned()) // if vOther is not assigned
					{
						max++;
					}
				}
				
				if (max > maximum)
				{
					temp = v;
					maximum = max;
				}
			}
		}
//		if (temp != null)
//			System.out.println(temp.toString() + " " + maximum);
		return temp;
	}
	
	/**
	 * Value Selection Heuristics. Orders the values in the domain of the variable 
	 * passed as a parameter and returns them as a list.
	 * @return List of values in the domain of a variable in a specified order. 
	 */
	public List<Integer> getNextValues(Variable v)
	{
		List<Integer> orderedValues;
		switch(valHeuristics)
		{
		case None: 						orderedValues = getValuesInOrder(v);
		break;
		case LeastConstrainingValue: 	orderedValues = getValuesLCVOrder(v);
		break;
		default:						orderedValues = getValuesInOrder(v);
		break;
		}
		return orderedValues;
	}
	
	/**
	 * Default value ordering. 
	 * @param v Variable whose values need to be ordered
	 * @return values ordered by lowest to highest. 
	 */
	public List<Integer> getValuesInOrder(Variable v)
	{
		List<Integer> values = v.getDomain().getValues();
		
		Comparator<Integer> valueComparator = new Comparator<Integer>(){

			@Override
			public int compare(Integer i1, Integer i2) {
				return i1.compareTo(i2);
			}
		};
		Collections.sort(values, valueComparator);
		return values;
	}
	
	/**
	 * TODO: LCV heuristic
	 */
	public List<Integer> getValuesLCVOrder(final Variable v)
	{
		List<Integer> values = v.getDomain().getValues();
		
		Comparator<Integer> valueComparator = new Comparator<Integer>(){
	
			@Override
			public int compare(Integer i1, Integer i2) {
				Integer x = 0;
				Integer y = 0;
				
				for(Variable vOther : network.getNeighborsOfVariable(v))
				{
					if (vOther.getDomain().contains(i1))
						x++;
					if (vOther.getDomain().contains(i2))
						y++;				
				}
					
				return x.compareTo(y);
			}
		};
		Collections.sort(values, valueComparator);
		return values;
	}
	/**
	 * Called when solver finds a solution
	 */
	private void success()
	{
		hasSolution = true;
		sudokuGrid = Converter.ConstraintNetworkToSudokuFile(network, sudokuGrid.getN(), sudokuGrid.getP(), sudokuGrid.getQ());
	}

	//===============================================================================
	// Solver
	//===============================================================================

	/**
	 * Method to start the solver
	 */
	public void solve()
	{
		startTime = System.currentTimeMillis();
		try {
			solve(0);
		}catch (VariableSelectionException e)
		{
			System.out.println("error with variable selection heuristic.");
		}
		endTime = System.currentTimeMillis();
		Trail.clearTrail();
	}

	/**
	 * Solver
	 * @param level How deep the solver is in its recursion. 
	 * @throws VariableSelectionException 
	 */

	private void solve(int level) throws VariableSelectionException
	{
		if(!Thread.currentThread().isInterrupted())

		{//Check if assignment is completed
			if(hasSolution)
			{
				return;
			}

			//Select unassigned variable
			Variable v = selectNextVariable();		

			//check if the assignment is complete
			if(v == null)
			{
				for(Variable var : network.getVariables())
				{
					if(!var.isAssigned())
					{
						throw new VariableSelectionException("Something happened with the variable selection heuristic");
					}
				}
				success();
				return;
			}

			//loop through the values of the variable being checked LCV

			
			for(Integer i : getNextValues(v))
			{
				trail.placeBreadCrumb();

				//check a value
				v.updateDomain(new Domain(i));
				numAssignments++;
				boolean isConsistent = checkConsistency() && checkNakedConsistency();
				
				//move to the next assignment
				if(isConsistent)
				{		
					solve(level + 1);
				}

				//if this assignment failed at any stage, backtrack
				if(!hasSolution)
				{
					trail.undo();
					numBacktracks++;
				}
				
				else
				{
					return;
				}
			}	
		}	
	}
	public boolean checkSolution()
	{
		int[][] board = sudokuGrid.getBoard();
		int N = sudokuGrid.getN();
		int p = sudokuGrid.getP();
		int q = sudokuGrid.getQ();
		 for (int i = 0; i < N; i++) {

		        int[] row = new int[N];
		        int[] square = new int[N];
		        int[] column = board[i].clone();

		        for (int j = 0; j < N; j ++) {
		            row[j] = board[j][i];
		            square[j] = board[(i / p) * p + j / q][i * q % N + j % q];
		        }
		        if (!(validate(column) && validate(row) && validate(square)))
		            return false;
		    }
		    return true;
		
	}
	private boolean validate(int[] check) {
	    int i = 0;
	    Arrays.sort(check);
	    for (int number : check) {
	        if (number != ++i)
	            return false;
	    }
	    return true;
	}

	@Override
	public void run() {
		solve();
	}
}
