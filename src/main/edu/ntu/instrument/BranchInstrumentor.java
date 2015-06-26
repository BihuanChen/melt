package edu.ntu.instrument;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
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
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

public class BranchInstrumentor {
	
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
		
		final String fileName = cu.getPackage().getName() + "." + file.getName();
		
		cu.recordModifications();
		cu.accept(new ASTVisitor() {

			private String methodName = null;
			
			@SuppressWarnings("unchecked")
			@Override
			public boolean visit(ConditionalExpression conditionalExpression) {			
				int lineNum = cu.getLineNumber(conditionalExpression.getStartPosition());
				Expression expression = conditionalExpression.getExpression();
						
				IfStatement ifStatement = ast.newIfStatement();
				ifStatement.setExpression((Expression)(rewriter.createCopyTarget(expression)));
				Block thenBlock = ast.newBlock();
				thenBlock.statements().add(createCounterStmt(fileName + " @ " + methodName + " @ " + lineNum + " @ " + expression));
				ifStatement.setThenStatement(thenBlock);
				Block elseBlock = ast.newBlock();
				elseBlock.statements().add(createCounterStmt(fileName + " @ " + methodName + " @ " + lineNum + " @ " + "!(" + expression + ")"));
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
					System.err.println("error");
				}
				return super.visit(conditionalExpression);
			}
			
			@Override
			public boolean visit(SwitchStatement switchStatement) {
				int lineNum = cu.getLineNumber(switchStatement.getStartPosition());
				Expression expression = switchStatement.getExpression();
				
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
								listRewrite.insertAfter(createCounterStmt(fileName + " @ " + methodName + " @ " + lineNum + " @ " + fullCondition), switchCase, null);
							}
						} else {
							partCondition = partCondition == null ? expression + " == " + switchCase.getExpression() : partCondition + " || " + expression + " == " + switchCase.getExpression();
							fullCondition = fullCondition == null ? expression + " != " + switchCase.getExpression() : fullCondition + " && " + expression + " != " + switchCase.getExpression();
						}
					} else if (partCondition != null) {
						listRewrite.insertBefore(createCounterStmt(fileName + " @ " + methodName + " @ " + lineNum + " @ " + partCondition), stmt, null);
						partCondition = null;
					}
				}
				
				if (!hasDefault && fullCondition != null) {
					SwitchCase newDefault = ast.newSwitchCase();
					newDefault.setExpression(null);
					listRewrite.insertLast(newDefault, null);
					listRewrite.insertLast(createCounterStmt(fileName + " @ " + methodName + " @ " + lineNum + " @ " + fullCondition), null);
					listRewrite.insertLast(ast.newBreakStatement(), null);
				}
				
				return super.visit(switchStatement);
			}
			
			@Override
			public boolean visit(ForStatement forStatement) {
				visit(forStatement, forStatement.getExpression(), forStatement.getBody(), forStatement.getParent());
				return super.visit(forStatement);
			}
			
			@Override
			public boolean visit(WhileStatement whileStatement) {
				visit(whileStatement, whileStatement.getExpression(), whileStatement.getBody(), whileStatement.getParent());
				return super.visit(whileStatement);
			}
			
			@Override
			public boolean visit(DoStatement doStatement) {
				visit(doStatement, doStatement.getExpression(), doStatement.getBody(), doStatement.getParent());
				return super.visit(doStatement);
			}
			
			@Override
			public boolean visit(IfStatement ifStatement) {
				int lineNum = cu.getLineNumber(ifStatement.getStartPosition());
				Expression expression = ifStatement.getExpression();
				
				ListRewrite listRewrite = rewriter.getListRewrite(ifStatement.getThenStatement(), Block.STATEMENTS_PROPERTY);
				listRewrite.insertFirst(createCounterStmt(fileName + " @ " + methodName + " @ " + lineNum + " @ " + expression), null);

				listRewrite = rewriter.getListRewrite(ifStatement.getElseStatement(), Block.STATEMENTS_PROPERTY);
				listRewrite.insertFirst(createCounterStmt(fileName + " @ " + methodName + " @ " + lineNum + " @ " + "!(" + expression + ")"), null);
				
				return super.visit(ifStatement);
			}
			
			@SuppressWarnings("unchecked")
			private Statement createCounterStmt(String id) {
				// add the branch predicate
				PredicateCounter.branchPredicates.add(id);
				int index = PredicateCounter.branchPredicates.size() - 1;
				// create the counter statement
				MethodInvocation newInvocation = ast.newMethodInvocation();
				QualifiedName qualifiedName = ast.newQualifiedName(ast.newName(new String[] {"edu", "ntu", "instrument"}), ast.newSimpleName("PredicateCounter"));
				newInvocation.setExpression(qualifiedName);
				newInvocation.setName(ast.newSimpleName("incBranchPredicateCounters"));
				NumberLiteral literal = ast.newNumberLiteral(String.valueOf(index));
				newInvocation.arguments().add(literal);
				Statement newStatement = ast.newExpressionStatement(newInvocation);
				return newStatement;
			}
			
			private void visit(Statement statement, Expression expression, Statement body, ASTNode parent) {	
				int lineNum = cu.getLineNumber(statement.getStartPosition());
				
				ListRewrite listRewrite = rewriter.getListRewrite(body, Block.STATEMENTS_PROPERTY);
				listRewrite.insertFirst(createCounterStmt(fileName + " @ " + methodName + " @ " + lineNum + " @ " + expression), null);
				
				if (parent instanceof SwitchStatement) {
					listRewrite = rewriter.getListRewrite(parent, SwitchStatement.STATEMENTS_PROPERTY);
				} else if (parent instanceof Block) {
					listRewrite = rewriter.getListRewrite(parent, Block.STATEMENTS_PROPERTY);
				} else {
					System.err.println("error");
				}
				listRewrite.insertAfter(createCounterStmt(fileName + " @ " + methodName + " @ " + lineNum + " @ " + "!(" + expression + ")"), statement, null);
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
				instrumentFilesInDir(f);
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
			BranchInstrumentor bi = new BranchInstrumentor();
			File project = new File("/home/bhchen/workspace/jpf-v6/ml-testing/src/tests/edu/ntu/instrument/branch/test2");
			
			bi.formatFilesInDir(project);
			bi.instrumentFilesInDir(project);
			
			PredicateCounter.printBranchPredicates();
		} catch (IOException | MalformedTreeException | BadLocationException e) {
			e.printStackTrace();
		}
	}

}
