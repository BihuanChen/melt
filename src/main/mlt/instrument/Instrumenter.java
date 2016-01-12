package mlt.instrument;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import mlt.Config;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Assignment.Operator;
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
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
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
	 * loop a project directory to update line numbers recursively
	 * @param root
	 * @throws IOException
	 * @throws MalformedTreeException
	 * @throws BadLocationException
	 */
	public void updateLineNumbers(File root) throws IOException, MalformedTreeException, BadLocationException {
		if (root.isFile()) {
			if (root.getName().endsWith(".java")) {
				updateLineNumbersFile(root);
			}
		} else {
			File[] files = root.listFiles();	 
			for (File f : files) {
				updateLineNumbers(f);
			}
		}
	}
		
	/*
	 *  update line numbers for a file
	 */
	private void updateLineNumbersFile(File file) throws IOException, MalformedTreeException, BadLocationException {
		final String source = FileUtils.readFileToString(file);
		final Document document = new Document(source);
		
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(document.get().toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		final CompilationUnit cu = (CompilationUnit)parser.createAST(null);
				
		cu.recordModifications();
		cu.accept(new ASTVisitor() {

			private int id = 0;
			
			@Override
			public boolean visit(ForStatement forStatement) {
				updateLineNumber(cu.getLineNumber(forStatement.getStartPosition()));
				return super.visit(forStatement);
			}
			
			@Override
			public boolean visit(WhileStatement whileStatement) {
				updateLineNumber(cu.getLineNumber(whileStatement.getStartPosition()));
				return super.visit(whileStatement);
			}
			
			@Override
			public boolean visit(DoStatement doStatement) {
				updateLineNumber(cu.getLineNumber(doStatement.getExpression().getStartPosition()));
				return super.visit(doStatement);
			}
			
			@Override
			public boolean visit(IfStatement ifStatement) {
				updateLineNumber(cu.getLineNumber(ifStatement.getStartPosition()));
				return super.visit(ifStatement);
			}
			
			private void updateLineNumber(int lineNumber) {
				predicates.get(id++).setLineNumber(lineNumber);
			}

		});
		
	}
	
	/**
	 * loop a project directory to get and instrument files recursively
	 * @param root
	 * @throws IOException
	 * @throws MalformedTreeException
	 * @throws BadLocationException
	 */
	public void instrument(File root) throws IOException, MalformedTreeException, BadLocationException {
		if (root.isFile()) {
			if (root.getName().endsWith(".java")) {
				instrumentFile(root);
			}
		} else {
			File[] files = root.listFiles();	 
			for (File f : files) {
				instrument(f);
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
				visit(forStatement, forStatement.getExpression(), forStatement.getBody(), forStatement.getParent(), Predicate.TYPE.FOR);
				return super.visit(forStatement);
			}
			
			@Override
			public boolean visit(WhileStatement whileStatement) {
				visit(whileStatement, whileStatement.getExpression(), whileStatement.getBody(), whileStatement.getParent(), Predicate.TYPE.WHILE);
				return super.visit(whileStatement);
			}
			
			@Override
			public boolean visit(DoStatement doStatement) {
				visit(doStatement, doStatement.getExpression(), doStatement.getBody(), doStatement.getParent(), Predicate.TYPE.DO);
				return super.visit(doStatement);
			}
			
			@Override
			public boolean visit(IfStatement ifStatement) {
				//int lineNum = cu.getLineNumber(ifStatement.getStartPosition());
				Expression expression = ifStatement.getExpression();
				
				ListRewrite listRewrite = rewriter.getListRewrite(ifStatement.getThenStatement(), Block.STATEMENTS_PROPERTY);
				listRewrite.insertFirst(createCounterStmt(className, methodName, /*lineNum,*/ expression, Predicate.TYPE.IF, true), null);

				listRewrite = rewriter.getListRewrite(ifStatement.getElseStatement(), Block.STATEMENTS_PROPERTY);
				listRewrite.insertFirst(createCounterStmt(className, methodName, /*lineNum,*/ expression, Predicate.TYPE.IF, false), null);
				
				return super.visit(ifStatement);
			}
			
			@SuppressWarnings("unchecked")
			private Statement createCounterStmt(String className, String methodName, /*int lineNumber,*/ Expression expression, Predicate.TYPE type, boolean branch) {
				// add the branch predicate
				if (branch) {
					Predicate predicate = new Predicate(className, methodName, 0, expression.toString(), type);
					predicates.add(predicate);
				}
				int index = predicates.size() - 1;
				// create the counter statement
				MethodInvocation newInvocation = ast.newMethodInvocation();
				QualifiedName qualifiedName = ast.newQualifiedName(ast.newName(new String[] {"mlt", "test"}), ast.newSimpleName("Profiles"));
				newInvocation.setExpression(qualifiedName);
				newInvocation.setName(ast.newSimpleName("add"));
				NumberLiteral literal1 = ast.newNumberLiteral(String.valueOf(index));
				newInvocation.arguments().add(literal1);
				BooleanLiteral literal2 = ast.newBooleanLiteral(branch);
				newInvocation.arguments().add(literal2);
				Statement newStatement = ast.newExpressionStatement(newInvocation);
				return newStatement;
			}
			
			private void visit(Statement statement, Expression expression, Statement body, ASTNode parent, Predicate.TYPE type) {	
				/*int lineNum;
				if (statement instanceof DoStatement) {
					lineNum = cu.getLineNumber(((DoStatement)statement).getExpression().getStartPosition());
				} else {
					lineNum = cu.getLineNumber(statement.getStartPosition());
				}*/
				
				ListRewrite listRewrite = rewriter.getListRewrite(body, Block.STATEMENTS_PROPERTY);
				listRewrite.insertFirst(createCounterStmt(className, methodName, /*lineNum,*/ expression, type, true), null);
				
				if (parent instanceof SwitchStatement) {
					listRewrite = rewriter.getListRewrite(parent, SwitchStatement.STATEMENTS_PROPERTY);
				} else if (parent instanceof Block) {
					listRewrite = rewriter.getListRewrite(parent, Block.STATEMENTS_PROPERTY);
				} else {
					System.err.println("[ml-testing] loop nested in unknown statements");
				}
				listRewrite.insertAfter(createCounterStmt(className, methodName, /*lineNum,*/ expression, type, false), statement, null);
			}
			
			@Override
			public boolean visit(MethodDeclaration methodDeclaration) {
				methodName = methodDeclaration.getName().getFullyQualifiedName();
				if (className.equals(Config.MAINCLASS) && methodName.equals(Config.METHOD)) {
					int size = 0;
					// construct the entry method
					String id = methodDeclaration.getReturnType2().toString() + " " + methodName + "(";
					List<?> parameters = methodDeclaration.parameters();
					if (parameters != null) {
						size = parameters.size();
						for (int i = 0; i < size; i++) {
							SingleVariableDeclaration svd = (SingleVariableDeclaration)parameters.get(i);
							if (i < size - 1) {
								id += svd.getType() + ",";
							} else {
								id += svd.getType();
							}
						}
					}
					id += ")";
					// instrument Tainter.taint
					if (id.equals(Config.ENTRYMETHOD)) {
						Block block = methodDeclaration.getBody();
						ListRewrite listRewrite = rewriter.getListRewrite(block, Block.STATEMENTS_PROPERTY);
						for (int i = size - 1; i >= 0; i--) {
							listRewrite.insertFirst(createAssignmentStatement(Config.PARAMETERS[i], Config.CLS[i], (int)Math.pow(2, i)), null);
						}
					}
				}
				return true;
			}
			
			@SuppressWarnings("unchecked")
			private Statement createAssignmentStatement(String parameter, Class<?> cls, int tag) {
			    MethodInvocation invocation = ast.newMethodInvocation();
			    QualifiedName qualifiedName = ast.newQualifiedName(ast.newName(new String[] {"edu", "columbia", "cs", "psl", "phosphor", "runtime"}), ast.newSimpleName("Tainter"));
			    invocation.setExpression(qualifiedName);
			    if (cls == byte.class) {
				    invocation.setName(ast.newSimpleName("taintedByte"));
				} else if (cls == short.class) {
					invocation.setName(ast.newSimpleName("taintedShort"));
				} else if (cls == int.class) {
					invocation.setName(ast.newSimpleName("taintedInt"));
				} else if (cls == long.class) {
					invocation.setName(ast.newSimpleName("taintedLong"));
				} else if (cls == float.class) {
					invocation.setName(ast.newSimpleName("taintedFloat"));
				} else if (cls == double.class) {
					invocation.setName(ast.newSimpleName("taintedDouble"));
				} else if (cls == boolean.class) {
					invocation.setName(ast.newSimpleName("taintedBoolean"));
				}
			    invocation.arguments().add(ast.newSimpleName(parameter));
			    invocation.arguments().add(ast.newNumberLiteral(String.valueOf(tag)));
				
			    Assignment assignment = ast.newAssignment();
			    assignment.setLeftHandSide(ast.newSimpleName(parameter));
			    assignment.setOperator(Operator.ASSIGN);
			    assignment.setRightHandSide(invocation);
			    return ast.newExpressionStatement(assignment);
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
	public void format(File root) throws IOException, MalformedTreeException, BadLocationException {
		if (root.isFile()) {
			if (root.getName().endsWith(".java")) {
				formatFile(root);
			}
		} else {
			File[] files = root.listFiles();	 
			for (File f : files) {
				format(f);
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
			Config.loadProperties("/home/bhchen/workspace/testing/phosphor-test/src/phosphor/test/Test1.mlt");
			
			File project = new File("/home/bhchen/workspace/testing/phosphor-test/src/phosphor/test/Test1.java");
			Instrumenter instrumenter = new Instrumenter();
			instrumenter.format(project);
			instrumenter.instrument(project);
			instrumenter.updateLineNumbers(project);
			
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
