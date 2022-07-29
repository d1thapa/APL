/**
 * A parse-tree representation of a fun program.
 */
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

class FunProgram implements ParseTree {
	/** symbol table of the program */
	private Map<String, EvalResult> symtab;

	/** the statements of the program */
	private ArrayList<ParseTree> stmntList;

	/** a handy variable for return void responses */
	private EvalResult voidResult;

	public FunProgram()
	{
		symtab = new HashMap<String, EvalResult>();
		stmntList = new ArrayList<ParseTree>();
		voidResult = new EvalResult();
		voidResult.type = EvalType.VOID;
		voidResult.value = null;
	}

	/** Add a statement to the program. */
	public void add(ParseTree stmnt) {
		stmntList.add(stmnt);
	}


	/**
	 * Get the number size of the program.
	 * @return The number of statements in the program.
	 */
	public int size() {
		return stmntList.size();
	}

	/**
	 * Evaluate the program.
	 * @return A void EvalResult.
	 */
	@Override
	public EvalResult eval()
	{
		//evaluate each statement
		for(int i=0; i<size(); i++) {
			stmntList.get(i).eval();
		}

		return voidResult;
	}

	/**
	 * This inner class represents a variable reference.
	 */
	public class VarRef implements ParseTree {
		public String name;

		/**
		 * Construct the variable reference from its name.
		 */
		public VarRef(String name) {
			this.name = name;
		}


		/**
		 * Assign a value to the variable.
		 */
		public void assign(EvalResult value) {
			// add the variable to the symbol table
			symtab.put(name, value);
		}


		/**
		 * Evaluate a variable reference.
		 * @return The value stored in the variable.
		 */
		@Override
		public EvalResult eval() {
			return symtab.get(name);
		}
	}

	public class Assign implements ParseTree {
		private VarRef variable;
		private Expr expr;

		/**
		 * Construct the assignment from its parts
		 */
		public Assign(VarRef variable, Expr expr) {
			this.variable = variable;
			this.expr = expr;
		}


		/**
		 * Evaluate the assignment, and return a void response.
		 * @return A void result.
		 */
		@Override
		public EvalResult eval() {
			variable.assign(expr.eval());

			return voidResult;
		}
	}

	 /**
     * This inner class represents an Proc statement.
     */
    public class Proc implements ParseTree {
        // private VarRef variable;
        // private Expr expr;

        /**
         * Construct the proc from its parts
         */
        public Proc() {
            // this.variable = variable;
            // this.expr = expr;
        }



        /**
         * Evaluate the proc, and return a void response.
         * @return A void result.
         */
        @Override
        public EvalResult eval() {
            //variable.proc(expr.eval());

            return voidResult;
        }
    }

	public class Swap implements ParseTree {
		private VarRef variable;
		private Expr expr;

		/**
		 * Construct the assignment from its parts
		 */
		public Swap(VarRef variable, Expr expr) {
			this.variable = variable;
			this.expr = expr;
		}


		/**
		 * Evaluate the assignment, and return a void response.
		 * @return A void result.
		 */
		@Override
		public EvalResult eval() {
			variable.assign(expr.eval());

			return voidResult;
		}
	}

	/**
	 * This is an empty class to begin the hierarchy of expression types.
	 */
	public abstract class Expr implements ParseTree {
		/* this space left intentionally blank */
	}

	/**
	 * This is the next step in the expression tree, mainly contributing a type
	 * system for binary operations.
	 */
	public abstract class BinOp extends Expr {
		// left hand side of the binary operation
		protected EvalResult lhs;

		// right hand side of the binary operation
		protected EvalResult rhs;

		protected String op = "";

		// computes the return type of this binary operation
		protected EvalType returnType() {
			System.out.println(lhs.type);
			System.out.println(rhs.type);
			// we do the standard type widening
			if(lhs.type == EvalType.INTEGER && rhs.type == EvalType.INTEGER && op == "") {
				return EvalType.INTEGER;
			} else if(lhs.type == EvalType.FLOAT && rhs.type == EvalType.INTEGER && op == "") {
				return EvalType.FLOAT;
			} else if(lhs.type == EvalType.INTEGER && rhs.type == EvalType.FLOAT && op == "") {
				return EvalType.FLOAT;
			} else if(lhs.type == EvalType.FLOAT && rhs.type == EvalType.FLOAT && op == "") {
				return EvalType.FLOAT;
			} else if (lhs.type == EvalType.BOOL && rhs.type == EvalType.BOOL) {
				return EvalType.BOOL;
			} else if (lhs.type == EvalType.INTEGER && rhs.type == EvalType.INTEGER && op == "<") {
				System.out.println("bool");
				return EvalType.BOOL;
			}

			// if we reach here, we have an invalid operand.
			return EvalType.VOID;
		}
	}

	/**
	 * Peform an addition operation.
	 */
	public class Add extends BinOp {
		ParseTree lop;
		ParseTree rop;

		/**
		 * Construct an addition between the left and right operands.
		 * @param lop Left operand
		 * @param rop Right operand
		 */
		public Add(ParseTree lop, ParseTree rop) {
			this.lop = lop;
			this.rop = rop;
		}


		/**
		 * Evaluate the addition, returning the result.
		 * @return The result of the addition operation.
		 */
		@Override
		public EvalResult eval() {
			EvalResult result = new EvalResult();

			//compute the lhs and rhs of the operation
			lhs = lop.eval();
			rhs = rop.eval();

			// compute the result type
			result.type = returnType();

			// carry out a valid operation
			if(result.type == EvalType.INTEGER) {
				//get the operand values
				int x = ((Number)lhs.value).intValue();
				int y = ((Number)rhs.value).intValue();

				result.value = x + y;
			} else if(result.type == EvalType.FLOAT) {
				//get the operand values
				double x = ((Number)lhs.value).doubleValue();
				double y = ((Number)rhs.value).doubleValue();

				result.value = x + y;
			}

			return result;
		}
	}

	/**
	 * Peform a subtraction operation.
	 */
	public class Subtract  extends BinOp {
		ParseTree lop;
		ParseTree rop;

		/**
		 * Construct a subtraction between the left and right operands.
		 * @param lop Left operand
		 * @param rop Right operand
		 */
		public Subtract (ParseTree lop, ParseTree rop) {
			this.lop = lop;
			this.rop = rop;
		}


		/**
		 * Evaluate the subtraction, returning the result.
		 * @return The result of the subtraction operation.
		 */
		@Override
		public EvalResult eval() {
			EvalResult result = new EvalResult();

			//compute the lhs and rhs of the operation
			lhs = lop.eval();
			rhs = rop.eval();

			// compute the result type
			result.type = returnType();

			// carry out a valid operation
			if(result.type == EvalType.INTEGER) {
				//get the operand values
				int x = ((Number)lhs.value).intValue();
				int y = ((Number)rhs.value).intValue();

				result.value = x - y;
			} else if(result.type == EvalType.FLOAT) {
				//get the operand values
				double x = ((Number)lhs.value).doubleValue();
				double y = ((Number)rhs.value).doubleValue();

				result.value = x - y;
			}

			return result;
		}
	}

	public class Equal extends BinOp {
		ParseTree lop;
		ParseTree rop;

		/**
		 * Construct a subtraction between the left and right operands.
		 * @param lop Left operand
		 * @param rop Right operand
		 */
		public Equal (ParseTree lop, ParseTree rop) {
			this.lop = lop;
			this.rop = rop;
		}


		/**
		 * Evaluate the subtraction, returning the result.
		 * @return The result of the subtraction operation.
		 */
		@Override
		public EvalResult eval() {
			EvalResult result = new EvalResult();

			//compute the lhs and rhs of the operation
			lhs = lop.eval();
			rhs = rop.eval();

			// compute the result type
			result.type = returnType();

			// carry out a valid operation
			if(result.type == EvalType.INTEGER) {
				//get the operand values
				int x = ((Number)lhs.value).intValue();
				int y = ((Number)rhs.value).intValue();

				result.value = x == y;
			} else if(result.type == EvalType.FLOAT) {
				//get the operand values
				double x = ((Number)lhs.value).doubleValue();
				double y = ((Number)rhs.value).doubleValue();

				result.value = x == y;
			} else if (result.type == EvalType.BOOL) {
				int x = ((Number)lhs.value).intValue();
				int y = ((Number)rhs.value).intValue();

				result.value = x == y;
			}

			return result;
		}
	}

	public class NotEqual extends BinOp {
		ParseTree lop;
		ParseTree rop;

		/**
		 * Construct a subtraction between the left and right operands.
		 * @param lop Left operand
		 * @param rop Right operand
		 */
		public NotEqual (ParseTree lop, ParseTree rop) {
			this.lop = lop;
			this.rop = rop;
		}


		/**
		 * Evaluate the subtraction, returning the result.
		 * @return The result of the subtraction operation.
		 */
		@Override
		public EvalResult eval() {
			EvalResult result = new EvalResult();

			//compute the lhs and rhs of the operation
			lhs = lop.eval();
			rhs = rop.eval();

			// compute the result type
			result.type = returnType();

			// carry out a valid operation
			if(result.type == EvalType.INTEGER) {
				//get the operand values
				int x = ((Number)lhs.value).intValue();
				int y = ((Number)rhs.value).intValue();

				result.value = x != y;
			} else if(result.type == EvalType.FLOAT) {
				//get the operand values
				double x = ((Number)lhs.value).doubleValue();
				double y = ((Number)rhs.value).doubleValue();

				result.value = x != y;
			} else if (result.type == EvalType.BOOL) {
				int x = ((Number)lhs.value).intValue();
				int y = ((Number)rhs.value).intValue();

				result.value = x != y;
			}

			return result;
		}
	}

	public class GreaterThan extends BinOp {
		ParseTree lop;
		ParseTree rop;

		/**
		 * Construct a subtraction between the left and right operands.
		 * @param lop Left operand
		 * @param rop Right operand
		 */
		public GreaterThan (ParseTree lop, ParseTree rop) {
			this.lop = lop;
			this.rop = rop;
		}


		/**
		 * Evaluate the subtraction, returning the result.
		 * @return The result of the subtraction operation.
		 */
		@Override
		public EvalResult eval() {
			EvalResult result = new EvalResult();

			//compute the lhs and rhs of the operation
			lhs = lop.eval();
			rhs = rop.eval();

			// compute the result type
			result.type = returnType();
			System.out.println(result.type);
			// carry out a valid operation
			if(result.type == EvalType.INTEGER) {
				//get the operand values
				int x = ((Number)lhs.value).intValue();
				int y = ((Number)rhs.value).intValue();

				System.out.println("X:" + x);
				System.out.println("y:" + y);
				result.value = x > y;
			} else if(result.type == EvalType.FLOAT) {
				//get the operand values
				double x = ((Number)lhs.value).doubleValue();
				double y = ((Number)rhs.value).doubleValue();

				result.value = x > y;
			} else if (result.type == EvalType.BOOL) {
				int x = ((Number)lhs.value).intValue();
				int y = ((Number)rhs.value).intValue();

				result.value = x > y;
			}

			return result;
		}
	}

	public class GreaterThanOrEqual extends BinOp {
		ParseTree lop;
		ParseTree rop;

		/**
		 * Construct a subtraction between the left and right operands.
		 * @param lop Left operand
		 * @param rop Right operand
		 */
		public GreaterThanOrEqual (ParseTree lop, ParseTree rop) {
			this.lop = lop;
			this.rop = rop;
		}


		/**
		 * Evaluate the subtraction, returning the result.
		 * @return The result of the subtraction operation.
		 */
		@Override
		public EvalResult eval() {
			EvalResult result = new EvalResult();

			//compute the lhs and rhs of the operation
			lhs = lop.eval();
			rhs = rop.eval();

			// compute the result type
			result.type = returnType();

			// carry out a valid operation
			if(result.type == EvalType.INTEGER) {
				//get the operand values
				int x = ((Number)lhs.value).intValue();
				int y = ((Number)rhs.value).intValue();

				result.value = x >= y;
			} else if(result.type == EvalType.FLOAT) {
				//get the operand values
				double x = ((Number)lhs.value).doubleValue();
				double y = ((Number)rhs.value).doubleValue();

				result.value = x >= y;
			} else if (result.type == EvalType.BOOL) {
				int x = ((Number)lhs.value).intValue();
				int y = ((Number)rhs.value).intValue();

				result.value = x >= y;
			}

			return result;
		}
	}

	public class LessThan extends BinOp {
		ParseTree lop;
		ParseTree rop;

		/**
		 * Construct a subtraction between the left and right operands.
		 * @param lop Left operand
		 * @param rop Right operand
		 */
		public LessThan (ParseTree lop, ParseTree rop) {
			this.lop = lop;
			this.rop = rop;
		}


		/**
		 * Evaluate the subtraction, returning the result.
		 * @return The result of the subtraction operation.
		 */
		@Override
		public EvalResult eval() {
			EvalResult result = new EvalResult();

			//compute the lhs and rhs of the operation
			lhs = lop.eval();
			rhs = rop.eval();
			op = "<";

			// compute the result type
			result.type = returnType();
			// System.out.println(result.type);
			// carry out a valid operation
			if(result.type == EvalType.INTEGER) {
				//get the operand values
				int x = ((Number)lhs.value).intValue();
				int y = ((Number)rhs.value).intValue();
				// System.out.println("X: " + x);
				// System.out.println("Y: " + y);
				result.value = x < y;
			} else if(result.type == EvalType.FLOAT) {
				//get the operand values
				double x = ((Number)lhs.value).doubleValue();
				double y = ((Number)rhs.value).doubleValue();

				result.value = x < y;
			} else if (result.type == EvalType.BOOL) {
				int x = ((Number)lhs.value).intValue();
				int y = ((Number)rhs.value).intValue();
				System.out.println("X: " + x);
				System.out.println("Y: " + y);
				result.value = x < y;
				// System.out.println("Value: " + result.value);
			}

			return result;
		}
	}

	public class LessThanOrEqual extends BinOp {
		ParseTree lop;
		ParseTree rop;

		/**
		 * Construct a subtraction between the left and right operands.
		 * @param lop Left operand
		 * @param rop Right operand
		 */
		public LessThanOrEqual (ParseTree lop, ParseTree rop) {
			this.lop = lop;
			this.rop = rop;
		}


		/**
		 * Evaluate the subtraction, returning the result.
		 * @return The result of the subtraction operation.
		 */
		@Override
		public EvalResult eval() {
			EvalResult result = new EvalResult();

			//compute the lhs and rhs of the operation
			lhs = lop.eval();
			rhs = rop.eval();

			// compute the result type
			result.type = returnType();

			// carry out a valid operation
			if(result.type == EvalType.INTEGER) {
				//get the operand values
				int x = ((Number)lhs.value).intValue();
				int y = ((Number)rhs.value).intValue();

				result.value = x <= y;
			} else if(result.type == EvalType.FLOAT) {
				//get the operand values
				double x = ((Number)lhs.value).doubleValue();
				double y = ((Number)rhs.value).doubleValue();

				result.value = x <= y;
			} else if (result.type == EvalType.BOOL) {
				int x = ((Number)lhs.value).intValue();
				int y = ((Number)rhs.value).intValue();

				result.value = x <= y;
			}

			return result;
		}
	}

	/**
	 * Peform a multiplication operation.
	 */
	public class Multiply extends BinOp {
		ParseTree lop;
		ParseTree rop;

		/**
		 * Construct a multiplication between the left and right operands.
		 * @param lop Left operand
		 * @param rop Right operand
		 */
		public Multiply(ParseTree lop, ParseTree rop) {
			this.lop = lop;
			this.rop = rop;
		}


		/**
		 * Evaluate the multiplication, returning the result.
		 * @return The result of the multiplication operation.
		 */
		@Override
		public EvalResult eval() {
			EvalResult result = new EvalResult();

			//compute the lhs and rhs of the operation
			lhs = lop.eval();
			rhs = rop.eval();

			// compute the result type
			result.type = returnType();

			// carry out a valid operation
			if(result.type == EvalType.INTEGER) {
				//get the operand values
				int x = ((Number)lhs.value).intValue();
				int y = ((Number)rhs.value).intValue();

				result.value = x * y;
			} else if(result.type == EvalType.FLOAT) {
				//get the operand values
				double x = ((Number)lhs.value).doubleValue();
				double y = ((Number)rhs.value).doubleValue();

				result.value = x * y;
			}

			return result;
		}
	}

	/**
	 * Peform a division operation.
	 */
	public class Divide extends BinOp {
		ParseTree lop;
		ParseTree rop;

		/**
		 * Construct a division between the left and right operands.
		 * @param lop Left operand
		 * @param rop Right operand
		 */
		public Divide(ParseTree lop, ParseTree rop) {
			this.lop = lop;
			this.rop = rop;
		}


		/**
		 * Evaluate the division, returning the result.
		 * @return The result of the division operation.
		 */
		@Override
		public EvalResult eval() {
			EvalResult result = new EvalResult();

			//compute the lhs and rhs of the operation
			lhs = lop.eval();
			rhs = rop.eval();

			// compute the result type
			result.type = returnType();

			// carry out a valid operation
			if(result.type == EvalType.INTEGER) {
				//get the operand values
				int x = ((Number)lhs.value).intValue();
				int y = ((Number)rhs.value).intValue();

				result.value = x / y;
			} else if(result.type == EvalType.FLOAT) {
				//get the operand values
				double x = ((Number)lhs.value).doubleValue();
				double y = ((Number)rhs.value).doubleValue();

				result.value = x / y;
			}

			return result;
		}
	}

	/**
	 * Peform an exponent operation.
	 */
	public class Exponent extends BinOp {
		ParseTree lop;
		ParseTree rop;

		/**
		 * Construct an exponent between the left and right operands.
		 * @param lop Left operand
		 * @param rop Right operand
		 */
		public Exponent(ParseTree lop, ParseTree rop) {
			this.lop = lop;
			this.rop = rop;
		}


		/**
		 * Evaluate the exponent, returning the result.
		 * @return The result of the exponent operation.
		 */
		@Override
		public EvalResult eval() {
			EvalResult result = new EvalResult();

			//compute the lhs and rhs of the operation
			lhs = lop.eval();
			rhs = rop.eval();

			// compute the result type
			result.type = returnType();

			// carry out a valid operation
			if(result.type == EvalType.INTEGER) {
				//get the operand values
				int x = ((Number)lhs.value).intValue();
				int y = ((Number)rhs.value).intValue();

				result.value = (int) Math.pow(x,y);
			} else if(result.type == EvalType.FLOAT) {
				//get the operand values
				double x = ((Number)lhs.value).doubleValue();
				double y = ((Number)rhs.value).doubleValue();

				result.value = Math.pow(x,y);
			}

			return result;
		}
	}

	/* public class WhileLoop extends BinOp {
		private EvalResult value;

		public WhileLoop(ParseTree lop, , ParseTree condition, ParseTree rop) {
			this.value = new EvalResult();
			this.value.type=type;
			this.value.value=value;
		}
	}*/

	/**
	 * Represent litetal values on the parse tree.
	 */
	public class Literal extends Expr
	{
		private EvalResult value;

		/**
		 * Construct the literal node.
		 */
		public Literal(EvalType type, Object value) {
			this.value = new EvalResult();
			this.value.type=type;
			this.value.value=value;
		}


		/**
		 * Return the literal value.
		 * @return The literal value.
		 */
		@Override
		public EvalResult eval() {
			return value;
		}
	}

	/**
	 * Execute a print statement.
	 */
	public class Print implements ParseTree
	{
		ArrayList<ParseTree> args;

		/**
		 * Construct the Print statement.
		 */
		public Print()
		{
			args = new ArrayList<ParseTree>();
			// System.out.println(args);
		}


		/**
		 * Add an argument to the print's arg list.
		 */
		public void add(ParseTree arg) {
			args.add(arg);
			// System.out.println(args);
		}


		/**
		 * Print the arglist.
		 * @return VOID result.
		 */
		@Override
		public EvalResult eval() {
			// print the argument list
			// System.out.println(args.size());
			for(int i=0; i<args.size(); i++) {
				// System.out.println("Get: " + args.size());
				System.out.print(args.get(i).eval().value);
			}

			//print a newline
			System.out.println();

			return voidResult;
		}
	}

	/**
	 * Execute a print statement.
	 */
	public class WhileLoop implements ParseTree
	{
		ArrayList<ParseTree> stmnts;

		/**
		 * Construct the Print statement.
		 */
		public WhileLoop()
		{
			stmnts = new ArrayList<ParseTree>();
			// System.out.println(args);
		}


		/**
		 * Add an argument to the print's arg list.
		 */
		public void add(ParseTree arg) {
			stmnts.add(arg);
			System.out.println(stmnts);
		}


		/**
		 * Print the arglist.
		 * @return VOID result.
		 */
		@Override
		public EvalResult eval() {
			// print the argument list
			System.out.println(stmnts.size());
			for(int i=0; i<stmnts.size(); i++) {
				System.out.println("Get: " + stmnts.get(i).eval().value);
				System.out.print(stmnts.get(i).eval().value);
			}

			//print a newline
			System.out.println();

			return voidResult;
		}
	}
}
