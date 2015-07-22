package mlt.instrument;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

public class Instrumenter implements Serializable {
	
	private static final long serialVersionUID = 6585501767998527830L;
	
	private ArrayList<Predicate> predicates;
	
	public Instrumenter() {
		predicates = new ArrayList<Predicate>();
	}

	public ArrayList<Predicate> getPredicates() {
		return predicates;
	}

	public void setPredicates(ArrayList<Predicate> predicates) {
		this.predicates = predicates;
	}

	/**
	 * loop a project directory to get and instrument files recursively
	 * @param root
	 * @throws IOException
	 * @throws MalformedTreeException
	 * @throws BadLocationException
	 */
	public void instrumentFilesInDir(File root) throws IOException, MalformedTreeException, BadLocationException {
		File[] files = root.listFiles();	 
		for (File f : files) {
			if (f.isFile()) {
				instrumentFile(f);
			}
			if (f.isDirectory()) {
				instrumentFilesInDir(f);
			}
		}
	}
		
	/*
	 *  instrument a file
	 */
	private void instrumentFile(File file) throws IOException, MalformedTreeException, BadLocationException {
		final String source = FileUtils.readFileToString(file);
		final Document document = new Document(source);
		
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(document.get().toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		final CompilationUnit cu = (CompilationUnit)parser.createAST(null);
		final AST ast = cu.getAST();
		final ASTRewrite rewriter = ASTRewrite.create(ast);
		
		final String className = cu.getPackage().getName() + "." + file.getName().substring(0, file.getName().length() - 5);
		
		cu.recordModifications();
		cu.accept(new ASTVisitor() {

			private String methodName = null;
			
			@Override
			public boolean visit(ConditionalExpression conditionalExpression) {			
				int lineNum = cu.getLineNumber(conditionalExpression.getStartPosition());
				/*Expression expression = conditionalExpression.getExpression();
						
				IfStatement ifStatement = ast.newIfStatement();
				ifStatement.setExpression((Expression)(rewriter.createCopyTarget(expression)));
				Block thenBlock = ast.newBlock();
				thenBlock.statements().add(createCounterStmt(className + " @ " + methodName + " @ " + lineNum + " @ " + expression));
				ifStatement.setThenStatement(thenBlock);
				Block elseBlock = ast.newBlock();
				elseBlock.statements().add(createCounterStmt(className + " @ " + methodName + " @ " + lineNum + " @ " + "!(" + expression + ")"));
				ifStatement.setElseStatement(elseBlock);
				
				ASTNode parent = conditionalExpression.getParent().getParent();
				ASTNode child = conditionalExpression.getParent();
				
				while (!(parent instanceof Block) && !(parent instanceof SwitchStatement)) {
					child = parent;
					parent = parent.getParent();
				}
				
				if (parent instanceof SwitchStatement) {
					ListRewrite listRewrite = rewriter.getListRewrite(parent, SwitchStatement.STATEMENTS_PROPERTY);
					listRewrite.insertAfter(ifStatement, child, null);
				} else if (parent instanceof Block) {
					ListRewrite listRewrite = rewriter.getListRewrite(parent, Block.STATEMENTS_PROPERTY);
					listRewrite.insertAfter(ifStatement, child, null);
				} else {
					System.err.println(""[ml-testing] " + switch statement nested in unknown statements");
				}*/
				System.err.println("[ml-testing] conditional expression is not supported (" + className + " @ " + lineNum + ")");
				return super.visit(conditionalExpression);
			}
			
			@Override
			public boolean visit(SwitchStatement switchStatement) {
				int lineNum = cu.getLineNumber(switchStatement.getStartPosition());
				/*Expression expression = switchStatement.getExpression();
				
				ListRewrite listRewrite = rewriter.getListRewrite(switchStatement, SwitchStatement.STATEMENTS_PROPERTY);
				List<?> stmts = switchStatement.statements();
				String partCondition = null;
				String fullCondition = null;
				boolean hasDefault = false;
				for (int i = 0; i < stmts.size(); i++) {
					Statement stmt = (Statement)stmts.get(i);
					if (stmt instanceof SwitchCase) {
						SwitchCase switchCase = (SwitchCase)stmt;
						if (switchCase.isDefault()) {
							hasDefault = true;
							if (fullCondition != null) {
								listRewrite.insertAfter(createCounterStmt(className + " @ " + methodName + " @ " + lineNum + " @ " + fullCondition), switchCase, null);
							}
						} else {
							partCondition = partCondition == null ? expression + " == " + switchCase.getExpression() : partCondition + " || " + expression + " == " + switchCase.getExpression();
							fullCondition = fullCondition == null ? expression + " != " + switchCase.getExpression() : fullCondition + " && " + expression + " != " + switchCase.getExpression();
						}
					} else if (partCondition != null) {
						listRewrite.insertBefore(createCounterStmt(className + " @ " + methodName + " @ " + lineNum + " @ " + partCondition), stmt, null);
						partCondition = null;
					}
				}
				
				if (!hasDefault && fullCondition != null) {
					SwitchCase newDefault = ast.newSwitchCase();
					newDefault.setExpression(null);
					listRewrite.insertLast(newDefault, null);
					listRewrite.insertLast(createCounterStmt(className + " @ " + methodName + " @ " + lineNum + " @ " + fullCondition), null);
					listRewrite.insertLast(ast.newBreakStatement(), null);
				}*/
				System.err.println("[ml-testing] switch statement is not supported (" + className + " @ " + lineNum + ")");
				return super.visit(switchStatement);
			}
			
			@Override
			public boolean visit(ForStatement forStatement) {
				visit(forStatement, forStatement.getExpression(), forStatement.getBody(), forStatement.getParent(), "for");
				return super.visit(forStatement);
			}
			
			@Override
			public boolean visit(WhileStatement whileStatement) {
				visit(whileStatement, whileStatement.getExpression(), whileStatement.getBody(), whileStatement.getParent(), "while");
				return super.visit(whileStatement);
			}
			
			@Override
			public boolean visit(DoStatement doStatement) {
				visit(doStatement, doStatement.getExpression(), doStatement.getBody(), doStatement.getParent(), "do");
				return super.visit(doStatement);
			}
			
			@Override
			public boolean visit(IfStatement ifStatement) {
				int lineNum = cu.getLineNumber(ifStatement.getStartPosition());
				Expression expression = ifStatement.getExpression();
				
				ListRewrite listRewrite = rewriter.getListRewrite(ifStatement.getThenStatement(), Block.STATEMENTS_PROPERTY);
				listRewrite.insertFirst(createCounterStmt(className, methodName, lineNum, expression.toString(), "if", true), null);

				listRewrite = rewriter.getListRewrite(ifStatement.getElseStatement(), Block.STATEMENTS_PROPERTY);
				listRewrite.insertFirst(createCounterStmt(className, methodName, lineNum, expression.toString(), "if", false), null);
				
				return super.visit(ifStatement);
			}
			
			@SuppressWarnings("unchecked")
			private Statement createCounterStmt(String className, String methodName, int lineNumber, String expression, String type, boolean branch) {
				// add the branch predicate
				if (branch) {
					Predicate predicate = new Predicate(className, methodName, lineNumber, expression, type);
					predicates.add(predicate);
				}
				int index = predicates.size() - 1;
				// create the counter statement
				MethodInvocation newInvocation = ast.newMethodInvocation();
				QualifiedName qualifiedName = ast.newQualifiedName(ast.newName(new String[] {"mlt", "learn"}), ast.newSimpleName("Profile"));
				newInvocation.setExpression(qualifiedName);
				newInvocation.setName(ast.newSimpleName("add"));
				NumberLiteral literal1 = ast.newNumberLiteral(String.valueOf(index));
				newInvocation.arguments().add(literal1);
				BooleanLiteral literal2 = ast.newBooleanLiteral(branch);
				newInvocation.arguments().add(literal2);
				Statement newStatement = ast.newExpressionStatement(newInvocation);
				return newStatement;
			}
			
			private void visit(Statement statement, Expression expression, Statement body, ASTNode parent, String type) {	
				int lineNum = cu.getLineNumber(statement.getStartPosition());
				
				ListRewrite listRewrite = rewriter.getListRewrite(body, Block.STATEMENTS_PROPERTY);
				listRewrite.insertFirst(createCounterStmt(className, methodName, lineNum, expression.toString(), type, true), null);
				
				if (parent instanceof SwitchStatement) {
					listRewrite = rewriter.getListRewrite(parent, SwitchStatement.STATEMENTS_PROPERTY);
				} else if (parent instanceof Block) {
					listRewrite = rewriter.getListRewrite(parent, Block.STATEMENTS_PROPERTY);
				} else {
					System.err.println("[ml-testing] loop nested in unknown statements");
				}
				listRewrite.insertAfter(createCounterStmt(className, methodName, lineNum, expression.toString(), type, false), statement, null);
			}
			
			@Override
			public boolean visit(MethodDeclaration methodDeclaration) {
				methodName = methodDeclaration.getName().getFullyQualifiedName();
				return true;
			}

			@Override
			public void endVisit(MethodDeclaration node) {
				methodName = null;
			}

		});
		
		TextEdit edit = rewriter.rewriteAST(document, null);
		edit.apply(document);
		FileUtils.write(file, document.get());
	}
	
	
	/**
	 * loop a project directory to get and format files recursively
	 * @param root
	 * @throws IOException
	 * @throws MalformedTreeException
	 * @throws BadLocationException
	 */
	public void formatFilesInDir(File root) throws IOException, MalformedTreeException, BadLocationException {
		File[] files = root.listFiles();	 
		for (File f : files) {
			if (f.isFile()) {
				formatFile(f);
			}
			if (f.isDirectory()) {
				formatFilesInDir(f);
			}
		}
	}
	
	private void formatFile(final File file) throws IOException, MalformedTreeException, BadLocationException {
		final String source = FileUtils.readFileToString(file);
		final Document document = new Document(source);

		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(document.get().toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		final CompilationUnit cu = (CompilationUnit)parser.createAST(null);
		final AST ast = cu.getAST();
		final ASTRewrite rewriter = ASTRewrite.create(ast);
		
		cu.recordModifications();
		cu.accept(new ASTVisitor() {
			
			public boolean visit(ForStatement forStatement) {
				insertBraces(forStatement.getBody());				
				return super.visit(forStatement);
			}
			
			public boolean visit(WhileStatement whileStatement) {
				insertBraces(whileStatement.getBody());
				return super.visit(whileStatement);
			}
			
			public boolean visit(DoStatement doStatement) {
				insertBraces(doStatement.getBody());
				return super.visit(doStatement);
			}
			
			public boolean visit(IfStatement ifStatement) {
				insertBraces(ifStatement.getThenStatement());
				if (ifStatement.getElseStatement() == null) {
					Block block = ast.newBlock();
					rewriter.set(ifStatement, IfStatement.ELSE_STATEMENT_PROPERTY, block, null);
				} else {
					insertBraces(ifStatement.getElseStatement());
				}
				return super.visit(ifStatement);
			}
			
			@SuppressWarnings("unchecked")
			private void insertBraces(Statement statement) {
				if (!(statement instanceof Block)) {
					Block block = ast.newBlock();
					block.statements().add(rewriter.createCopyTarget(statement));
					rewriter.replace(statement, block, null);
				}
			}

		});
		
		TextEdit edit = rewriter.rewriteAST(document, null);
		edit.apply(document);
		FileUtils.write(file, document.get());
	}
	
	public static void main(String[] args) {
		try {
			File project = new File("src/tests/mlt/instrument/test2/");
			Instrumenter instrumenter = new Instrumenter();
			instrumenter.formatFilesInDir(project);
			instrumenter.instrumentFilesInDir(project);
			
			ArrayList<Predicate> pList = instrumenter.getPredicates();
			int size = pList.size();
			for (int i = 0; i < size; i++) {
				System.out.println("[ml-testing] " + pList.get(i));
			}
		} catch (IOException | MalformedTreeException | BadLocationException e) {
			e.printStackTrace();
		}
	}

}
