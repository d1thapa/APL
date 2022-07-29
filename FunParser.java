//import javax.naming.ldap.HasControls;

/**
 * Recursive descent parser for the Fun language.
 */
class FunParser implements FunTokens
{
	private FunLexer lexer;
	private FunProgram fun;

	public FunParser(FunLexer lexer) {
		this.lexer = lexer;
	}

	/**
	 * Parse, return only if the program succesfully parses.
	 */
	public ParseTree parse() {
		fun = new FunProgram();
		// call the start symbol
		program();
		return fun;
	}

	/**
	 * Parse a program.
	 */
	private ParseTree program() {
		// parse this list iteratively
		lexer.next();
		while(lexer.token() != EOF) {
			fun.add(stmnt());
		}

		//TODO: Exclude null programs.
		// if(fun.size() == 0) {
        //     // System.out.println("Error! Null programs are not allowed.");
        //     System.exit(-2);
        // }

		return fun;
	}

	/**
	 * Parse a stmnt.
	 */
	private ParseTree stmnt()
	{
		// System.out.println("In stmnt() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
		// check for refOrExpr
		if( has(REF) || has(LPAREN) || has(RPAREN) || has(LBRACKET) || has(RBRACKET) ||
			has(INTEGER) || has(FLOAT) || has(NUMBER) || has(CHARACTER) || has(STRING) ||
			has(OVERRIDE) || has(PRIVATE) || has(INHERITS) || has(BOOLEAN) || has(NEW) || has(IF) || has(ELSE) ) {
			// System.out.println("stmnt(): " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			return refOrExpr();
		} else if(has(PRINT)){
			// System.out.println("stmnt(): PRINT " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			mustBe(PRINT);
			return print();
		} else if (has(COMMA)) {
			// System.out.println("stmnt(): COMMA " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			mustBe(COMMA);
			return argList3();
		} else if( has(CLASS) || has(PROC) ){
			 // System.out.println("stmnt(): CLASS " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			return refOrExpr4();
		} else if ( has(BEGIN) || has(END) ) {
			// System.out.println("stmnt(): BEGIN " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			return funcBlock();
		} 
		else if (has(WHILE)) {
			System.out.println("stmnt(): WHILE " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			return loopRefOrExpr();
		}
		else {
			mustBe(READ);
			return read();
		}
	}

	private ParseTree loopRefOrExpr(){
		FunProgram.WhileLoop wl = fun.new WhileLoop();
		mustBe(WHILE);
		lexer.next();

		loopConditions(wl);

		return wl;
	}

	private ParseTree loopConditions(FunProgram.WhileLoop wl) {
		if(has(REF)) {
			ParseTree ref = fun.new VarRef(lexer.string());
			lexer.next();
			ParseTree expr = loopExpr(ref);
			wl.add(expr);
		}

		loopBlock(wl);

		return wl;
	}

	private void loopBlock(FunProgram.WhileLoop wl) {
		System.out.println(lexer.token());
		if (has(BEGIN)) {
			mustBe(BEGIN);
			lexer.next();
			
		}
	}

	private ParseTree loopExpr(ParseTree ref) {
		if(has(LT)) {
			//LESS THAN
			lexer.next();
			System.out.println(lexer.token());
			// term();
			// expr2();
			ParseTree test = fun.new LessThan(ref, expr());

			// System.out.println(test.eval().value);

			return test;
		}
		return ref;
	}

	private ParseTree funcBlock(){
		// System.out.println("In funcBlock() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
		if(has(BEGIN)){
			// System.out.println("funcBlock(): BEGIN " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			mustBe(BEGIN);
			lexer.next();
			return fun.new Proc();
		} else {
			// System.out.println("funcBlock(): END " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			mustBe(END);
			lexer.next();
			return fun.new Proc();
		}
	}

	/**
	 * Parse ref-or-expr
	 */
	private ParseTree refOrExpr()
	{
		// System.out.println("In refOrExpr() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
		if(has(REF)) {
			// System.out.println("refOrExpr(): REF " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			//consume the reference
			ParseTree ref = fun.new VarRef(lexer.string());
			lexer.next();
			return refOrExpr2(ref);
		} else if ( has(CHARACTER) || has(STRING) || has(OVERRIDE) || has(PRIVATE) || has(INHERITS)
			|| has(BOOLEAN) || has(NEW)  || has(NUMBER) || has(IF) || has(ELSE) ) {
			// System.out.println("refOrExpr(): CHARACTER " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			return refOrExpr5();
		} else if (has(BEGIN) || has(END)) {
			// System.out.println("refOrExpr(): BEGIN " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			return funcBlock();
		} 
		else if (has(WHILE)) {
			System.out.println("refOrExpr(): WHILE " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			return loopRefOrExpr();
		}		
		else {
			// System.out.println("refOrExpr(): else " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			return expr();
		}
	}

	/**
	 * Parse ref-or-expr'
	 */
	private ParseTree refOrExpr2(ParseTree left)
	{
		// System.out.println("In refOrExpr2() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
		if(has(ASSIGN)) {
			// System.out.println("refOrExpr2(): ASSIGN " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			//consume the assign
			lexer.next();
			return fun.new Assign((FunProgram.VarRef)left, (FunProgram.Expr) expr());
		} else if (has(SWAP)) {
			return refOrExpr3(left);
		} else {
			// System.out.println("refOrExpr2(): else " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			return expr2(left);
		}
	}

	/**
	 * Parse ref-or-expr'
	 */
	private ParseTree refOrExpr3(ParseTree left)
	{
		// System.out.println("In refOrExpr3() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
		if(has(SWAP)) {
			// System.out.println("refOrExpr3(): SWAP " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			//consume the SWAP
			lexer.next();
			return fun.new Swap((FunProgram.VarRef)left, (FunProgram.Expr) expr());
		} else {
			// System.out.println("refOrExpr3(): else " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			return expr2(left);
		}
	}

	private ParseTree refOrExpr4()
	{
		// System.out.println("In refOrExpr4() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
		if(has(CLASS)) {
			// System.out.println("refOrExpr4(): CLASS " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			//consume the SWAP
			mustBe(CLASS);
			lexer.next();
			return refOrExpr();
		} else if (has(PROC)) {
			// System.out.println("refOrExpr4(): PROC " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			mustBe(PROC);
			lexer.next();
			return refOrExpr();
		} else {
			// System.out.println("refOrExpr4(): else " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			return expr();
		}
	}

	private ParseTree refOrExpr5()
	{
		if ( has(CHARACTER) ) {
			// System.out.println("refOrExpr(): CHARACTER " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			mustBe(CHARACTER);
			lexer.next();
			return refOrExpr();
		}
		else if (has(STRING)) {
			// System.out.println("refOrExpr(): STRING " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			mustBe(STRING);
			lexer.next();
			return refOrExpr();
		}
		else if (has(OVERRIDE)) {
			// System.out.println("refOrExpr(): OVERRIDE " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			mustBe(OVERRIDE);
			lexer.next();
			return refOrExpr4();
		}
		else if (has(PRIVATE)) {
			// System.out.println("refOrExpr(): PRIVATE " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			mustBe(PRIVATE);
			lexer.next();
			return refOrExpr();
		}
		else if (has(INHERITS)) {
			// System.out.println("refOrExpr(): INHERITS " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			mustBe(INHERITS);
			lexer.next();
			return refOrExpr();
		}
		else if (has(BOOLEAN)) {
			// System.out.println("refOrExpr(): BOOLEAN " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			mustBe(BOOLEAN);
			lexer.next();
			return refOrExpr();
		}
		else if (has(NEW)) {
			// System.out.println("refOrExpr(): NEW " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			mustBe(NEW);
			lexer.next();
			return refOrExpr();
		}
		else if (has(NUMBER)) {
			// System.out.println("refOrExpr(): NUMBER " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			mustBe(NUMBER);
			lexer.next();
			return refOrExpr();
		}
		else if (has(IF)) {
			// System.out.println("refOrExpr(): IF " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			mustBe(IF);
			lexer.next();
			return refOrExpr();
		}
		else if (has(ELSE)) {
			// System.out.println("refOrExpr(): ELSE " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			mustBe(ELSE);
			lexer.next();
			return refOrExpr();
		}
		else if (has(WHILE)) {
			// System.out.println("refOrExpr(): WHILE " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			mustBe(WHILE);
			lexer.next();
			return refOrExpr();
		}
		else {
			return expr();
		}
	}

	/**
	 * Parse expr
	 */
	private ParseTree expr()
	{
		// System.out.println("In expr() for term() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
		ParseTree left = term();
		// System.out.println("In expr() for expr2() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
		return expr2(left);
	}

	/**
	 * Parse expr'
	 */
	private ParseTree expr2(ParseTree left)
	{
		// System.out.println("In expr2() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
		if(has(PLUS)) {
			//addition
			lexer.next();
			// term();
			// expr2();
			return fun.new Add(left, expr());
		} else if(has(MINUS)) {
			//subtraction
			lexer.next();
			// term();
			// expr2();
			return fun.new Subtract(left, expr());
		} else if(has(EQUAL)) {
			//EQUAL
			lexer.next();
			// term();
			// expr2();
			return fun.new Equal(left, expr());
		} else if(has(NOTEQUAL)) {
			//NOTEQUAL
			lexer.next();
			// term();
			// expr2();
			return fun.new NotEqual(left, expr());
		} else if(has(GT)) {
			//GREATER THAN
			lexer.next();
			// term();
			// expr2();
			return fun.new GreaterThan(left, expr());
		} else if(has(GTE)) {
			//GREATER THAN EQUAL
			lexer.next();
			// term();
			// expr2();
			return fun.new GreaterThanOrEqual(left, expr());
		} else if(has(LT)) {
			//LESS THAN
			lexer.next();
			// term();
			// expr2();
			ParseTree test = fun.new LessThan(left, expr());

			System.out.println(test.eval().value);

			return test;
		} else if(has(LTE)) {
			//LESS THAN EQUAL
			lexer.next();
			// term();
			// expr2();
			return fun.new LessThanOrEqual(left, expr());
		}
		//empty (no mustBe())
		return left;
	}

	/**
	 * Parse term
	 */
	private ParseTree term()
	{
		// // System.out.println("In term() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
		// // System.out.println("In term() for factor2() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
		// factor();

		// System.out.println("In term() for term2() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
		return term2(factor());
	}

	/**
	 * Parse term'
	 */
	private ParseTree term2(ParseTree left)
	{
		// System.out.println("In term2() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
		if(has(TIMES)) {
			// System.out.println("refOrExpr4(): TIMES " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			//multiplication
			lexer.next();
			// factor();
			// term2();
			return fun.new Multiply(left, term());
		} else if(has(DIVIDE)){
			// System.out.println("refOrExpr4(): TIMES " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			//division
			lexer.next();
			factor();
			return fun.new Divide(left, term());
		}
		return left;
	}

	/**
	 * Parse factor
	 */
	private ParseTree factor()
	{
		// // System.out.println("In factor() for argList2() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
		// exponent();

		// System.out.println("In factor() for argList2() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
		return factor2(exponent());
	}

	/**
	 * Parse factor2
	 */
	private ParseTree factor2(ParseTree left)
	{
		// System.out.println("In factor2() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
		if(has(EXPONENT)) {
			// System.out.println("refOrExpr4(): EXPONENT " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			//exponent
			lexer.next();
			// factor();
			return fun.new Exponent(left, factor());
		}
		return left;
	}

	/**
	 * Parse Exponent
	 */
	private ParseTree exponent()
	{
		// System.out.println("In exponent() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
		if(has(LBRACKET)) {
			//consume (
			lexer.next();
			//consume expr
			// expr();
			// // System.out.println("In term() for argList2() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			// ParseTree result =  argList3(expr());
			//consume )
			// mustBe(RBRACKET);
			// lexer.next();

			return expr();
		}
		else if (has(RBRACKET))
		{
			mustBe(RBRACKET);
			lexer.next();
			return refOrExpr();
		}
		else if(has(LPAREN)) {
			// System.out.println("exponent(): LPAREN " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			//consume (
			lexer.next();
			// // System.out.println(lexer.token());
			// //consume expr
			// ParseTree result = expr();
			// //consume )
			// // System.out.println("exponent(): RPAREN " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			// // System.out.println(result);
			// mustBe(RPAREN);
			// lexer.next();

			return expr();

		} else if (has(RPAREN)) {
			mustBe(RPAREN);
			lexer.next();
			return refOrExpr();

		} else if(has(REF)) {
			// System.out.println("exponent(): RES " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			//reference
			ParseTree result = fun.new VarRef(lexer.string());
			lexer.next();
			return result;
		} else if(has(INTEGER)) {
			// System.out.println("exponent(): INTEGER " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			// integer literal
			ParseTree result = fun.new Literal(EvalType.INTEGER, lexer.value());
			lexer.next();
			return result;
		} else if(has(FLOAT)) {
			// System.out.println("exponent(): FLOAT " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			// float literal is all that is left
			ParseTree result = fun.new Literal(EvalType.FLOAT, lexer.value());
			mustBe(FLOAT);
			lexer.next();
			return result;
		} else if(has(TRUE)) {
			// System.out.println("exponent(): TRUE " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			// float literal is all that is left
			ParseTree result = fun.new Literal(EvalType.BOOL, lexer.value());
			mustBe(TRUE);
			lexer.next();
			return result;
		} else if(has(FALSE)) {
			// System.out.println("exponent(): FALSE " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			// float literal is all that is left
			ParseTree result = fun.new Literal(EvalType.BOOL, lexer.value());
			mustBe(FALSE);
			lexer.next();
			return result;
		} else if(has(STRING)) {
			// System.out.println("exponent(): STRING " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			ParseTree result = fun.new Literal(EvalType.STRING, lexer.value());
			mustBe(STRING);
			lexer.next();
			return result;
		} else if(has(CHARACTER)) {
			// System.out.println("exponent(): CHARACTER " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			ParseTree result = fun.new Literal(EvalType.CHARACTER, lexer.value());
			mustBe(CHARACTER);
			lexer.next();
			return result;
		} else if ( has(NEW) ) {
			// System.out.println("exponent(): NEW " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			// ParseTree result = fun.new Literal(EvalType.CHARACTER, lexer.value());
			mustBe(NEW);
			lexer.next();

			mustBe(REF);
			lexer.next();

			return stmnt();
		} else {
			// System.out.println("exponent(): COMMA " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			mustBe(COMMA);
			lexer.next();
			return expr();
		}
		// else {
		// 	// System.out.println("exponent(): ELSE " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
		// 	// System.out.println(lexer.token());
		// 	// // lexer.next();
		// 	// // return refOrExpr();
		// 	mustBe(RPAREN);
		// 	lexer.next();
		// 	// // System.out.println(lexer.token());
		// 	return refOrExpr();
		// }
	}

	/**
	 * Parse print.
	 */
	private ParseTree print() {
		// System.out.println("In print() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
		//consume print
		FunProgram.Print p = fun.new Print();
		// System.out.println(p.toString());
		mustBe(PRINT);
		lexer.next();

		// System.out.println("In print() for argList() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
		argList(p);

		return p;
	}

	/**
	 * Parse read.
	 */
	private ParseTree read() {
		// System.out.println("In read() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
		//consume print
		FunProgram.Print p = fun.new Print();
		mustBe(READ);
		lexer.next();

		// System.out.println("In print() for argList() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
		argList(p);

		return p;
	}

	/**
	 * Parse arg-list.
	 */
	private ParseTree argList(FunProgram.Print p) {
		// System.out.println("In argList() for arg() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
		p.add(arg());

		// // System.out.println("In argList() for arg2() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
		// arg2();

		// // System.out.println("In argList() for arg3() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
		// arg3();

		// System.out.println("In argList() for argList2() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
		argList2(p);
		return p;
	}

	/**
	 * Parse arg-list'
	 */
	private void argList2(FunProgram.Print p) {
		// System.out.println("In argList2() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
		if(has(COMMA)) {
			// System.out.println("argList2(): COMMA " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			lexer.next();

			// System.out.println("In argList2() for arg() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			p.add(arg());
            argList2(p);

			// System.out.println("In argList2() for argList2() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			// argList2();
		}
	}

	private ParseTree argList3() {
		if(has(COMMA)) {
			// System.out.println("argList2(): COMMA " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			lexer.next();

			// System.out.println("In argList2() for arg() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			return arg();

			// // System.out.println("In argList2() for argList2() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			// argList2();
		} else {
			return expr();
		}
	}

	/**
	 * Parse arg
	 */
	private ParseTree arg() {
		// System.out.println("In arg() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
		if(has(STRING)) {
			// System.out.println("arg(): STRING " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			//consume the string
			ParseTree lit = fun.new Literal(EvalType.STRING, lexer.value());
			lexer.next();
			return lit;
		} else if (has(CHARACTER)) {
			// System.out.println("arg2(): CHARACTER " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			//consume the CAHARACTER
			ParseTree lit = fun.new Literal(EvalType.CHARACTER, lexer.value());
			lexer.next();
			return lit;
		} else if(has(BOOLEAN)) {
			// System.out.println("arg3(): BOOLEAN " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			//consume the BOOLEAN
			ParseTree lit = fun.new Literal(EvalType.BOOL, lexer.value());
			lexer.next();
			return lit;
		}  else {
			// System.out.println("arg(): else for expr() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
			return expr();
		}
	}

	/**
	 * Parse arg2
	 */
	// private ParseTree arg2() {
	// 	// System.out.println("In arg2() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
	// 	if(has(CHARACTER)) {
	// 		// System.out.println("arg2(): CHARACTER " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
	// 		//consume the CAHARACTER
	// 		ParseTree lit = fun.new Literal(EvalType.CHARACTER, lexer.value());
	// 		lexer.next();
	// 		return lit;
	// 	} else {
	// 		// System.out.println("arg(): else for expr() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
	// 		return expr();
	// 	}
	// }

	/**
	 * Parse arg3
	 */
	// private ParseTree arg3() {
	// 	// System.out.println("In arg3() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
	// 	if(has(BOOLEAN)) {
	// 		// System.out.println("arg3(): BOOLEAN " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
	// 		//consume the BOOLEAN
	// 		ParseTree lit = fun.new Literal(EvalType.BOOL, lexer.value());
	// 		lexer.next();
	// 		return lit;
	// 	} else {
	// 		// System.out.println("arg(): else for expr() " + lexer.token() + " line:" + lexer.lineNumber() +" column:"+ lexer.columnNumber());
	// 		return expr();
	// 	}
	// }

	/**
	 * Only returns if the current token is the specified token.
	 * Terminates the program otherwise.
	 */
	private void mustBe(int token) {
		if(token != lexer.token()) {
			// // System.out.println("Parser: " + token);
			// // System.out.println("Lexer: " + lexer.token());
			System.out.printf("Parser error at line %d, column %d. Parser: %d and Token: %d",
			lexer.lineNumber(), lexer.columnNumber(), token, lexer.token());
			System.exit(-1); //terminate the program
		}
		// else {
			// System.out.println("Parser: " + token);
			// System.out.println("Lexer: " + lexer.token());
		// }
		return;
	}

	/**
	 * Return true if the current token is the specified token.
	 */
	private boolean has(int token) {
		return lexer.token() == token;
	}

	public static void main(String [] args) {
		FunParser parser = new FunParser(new FunLexer(System.in));
		parser.parse();
		System.out.println("Success!");
	}
}
