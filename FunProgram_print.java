/**
 * A parse-tree representation of a fun program.
 */
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

class FunProgram implements ParseTree {
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
}
