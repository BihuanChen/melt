package melt.instrument;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import melt.Config;
import melt.core.Predicate;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
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
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
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
	private String srcPath;
	
	// used for update line number
	private int id = 0;
	
	public Instrumenter(String srcPath) {
		this.srcPath = srcPath;
		predicates = new ArrayList<Predicate>();
	}

	public ArrayList<Predicate> getPredicates() {
		return predicates;
	}

	public void setPredicates(ArrayList<Predicate> predicates) {
		this.predicates = predicates;
	}

	public void instrument() throws MalformedTreeException, IOException, BadLocationException {
		File file = new File(srcPath);
		this.format(file);
		this.instrument(file);
		this.updateLineNumbers(file);
	}
		
	/**
	 * loop a project directory to update line numbers recursively
	 * @param root
	 * @throws IOException
	 * @throws MalformedTreeException
	 * @throws BadLocationException
	 */
	private void updateLineNumbers(File root) throws IOException, MalformedTreeException, BadLocationException {
		if (root.isFile()) {
			if (root.getName().endsWith(".java")) {
				updateLineNumbersFile(root);
			}
		} else {
			File[] files = root.listFiles();
			java.util.Arrays.sort(files);
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
		Map<?, ?> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parser.setCompilerOptions(options);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		final CompilationUnit cu = (CompilationUnit)parser.createAST(null);
				
		cu.recordModifications();
		cu.accept(new ASTVisitor() {
			
			@Override
			public boolean visit(ForStatement forStatement) {
				updateLineNumber(cu.getLineNumber(forStatement.getStartPosition()));
				return super.visit(forStatement);
			}
			
			@Override
			public boolean visit(EnhancedForStatement foreachsStatement) {
				updateLineNumber(cu.getLineNumber(foreachsStatement.getStartPosition()));
				return super.visit(foreachsStatement);
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
	private void instrument(File root) throws IOException, MalformedTreeException, BadLocationException {
		if (root.isFile()) {
			if (root.getName().endsWith(".java")) {
				instrumentFile(root);
			}
		} else {
			File[] files = root.listFiles();
			java.util.Arrays.sort(files);
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
		parser.setResolveBindings(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		Map<?, ?> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parser.setCompilerOptions(options);
		parser.setUnitName(file.getName());
		parser.setEnvironment(new String[]{"/usr/lib/jvm/java-8-oracle/jre/lib/rt.jar"}, new String[]{srcPath.substring(0, srcPath.lastIndexOf("src") + 3)}, new String[]{"UTF-8"}, true);
		parser.setSource(document.get().toCharArray());

		final CompilationUnit cu = (CompilationUnit)parser.createAST(null);
		final AST ast = cu.getAST();
		final ASTRewrite rewriter = ASTRewrite.create(ast);
		
		final String className = cu.getPackage().getName() + "." + file.getName().substring(0, file.getName().length() - 5);
		
		cu.recordModifications();
		cu.accept(new ASTVisitor() {

			private String methodName = null;
			private String signature = null;
			private int nestedLoops = 0;
			private boolean hasRecursion = false;
			
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
					System.err.println(""[melt] " + switch statement nested in unknown statements");
				}*/
				System.err.println("[melt] conditional expression is not supported (" + className + " @ " + lineNum + ")");
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
				System.err.println("[melt] switch statement is not supported (" + className + " @ " + lineNum + ")");
				return super.visit(switchStatement);
			}
			
			@Override
			public boolean visit(ForStatement forStatement) {
				nestedLoops++;
				visit(forStatement, forStatement.getExpression(), forStatement.getBody(), forStatement.getParent(), Predicate.TYPE.FOR);
				return super.visit(forStatement);
			}
			
			@Override
			public boolean visit(EnhancedForStatement foreachStatement) {
				nestedLoops++;
				visit(foreachStatement, foreachStatement.getExpression(), foreachStatement.getBody(), foreachStatement.getParent(), Predicate.TYPE.FOREACH);
				return super.visit(foreachStatement);
			}

			@Override
			public boolean visit(WhileStatement whileStatement) {
				nestedLoops++;
				visit(whileStatement, whileStatement.getExpression(), whileStatement.getBody(), whileStatement.getParent(), Predicate.TYPE.WHILE);
				return super.visit(whileStatement);
			}
			
			@Override
			public boolean visit(DoStatement doStatement) {
				nestedLoops++;
				visit(doStatement, doStatement.getExpression(), doStatement.getBody(), doStatement.getParent(), Predicate.TYPE.DO);
				return super.visit(doStatement);
			}
			
			private void visit(Statement statement, Expression expression, Statement body, ASTNode parent, Predicate.TYPE type) {
				ListRewrite listRewrite = rewriter.getListRewrite(body, Block.STATEMENTS_PROPERTY);
				listRewrite.insertFirst(createBranchRecordStmt(expression, type, true), null);
				
				if (parent instanceof SwitchStatement) {
					listRewrite = rewriter.getListRewrite(parent, SwitchStatement.STATEMENTS_PROPERTY);
				} else if (parent instanceof Block) {
					listRewrite = rewriter.getListRewrite(parent, Block.STATEMENTS_PROPERTY);
				} else {
					System.err.println("[melt] loop nested in unknown statements");
					System.exit(0);
				}
				listRewrite.insertAfter(createBranchRecordStmt(expression, type, false), statement, null);
			}
			
			@Override
			public boolean visit(IfStatement ifStatement) {
				Expression expression = ifStatement.getExpression();
				
				ListRewrite listRewrite = rewriter.getListRewrite(ifStatement.getThenStatement(), Block.STATEMENTS_PROPERTY);
				listRewrite.insertFirst(createBranchRecordStmt(expression, Predicate.TYPE.IF, true), null);

				listRewrite = rewriter.getListRewrite(ifStatement.getElseStatement(), Block.STATEMENTS_PROPERTY);
				listRewrite.insertFirst(createBranchRecordStmt(expression, Predicate.TYPE.IF, false), null);
				
				return super.visit(ifStatement);
			}
			
			@SuppressWarnings("unchecked")
			private Statement createBranchRecordStmt(Expression expression, Predicate.TYPE type, boolean branch) {
				// add the branch predicate
				if (branch) {
					Predicate predicate = new Predicate(className, methodName, signature, 0, expression.toString(), type);
					predicates.add(predicate);
				}
				int index = predicates.size() - 1;
				// create the branch record statement
				MethodInvocation newInvocation = ast.newMethodInvocation();
				QualifiedName qualifiedName = ast.newQualifiedName(ast.newName(new String[] {"melt", "core"}), ast.newSimpleName("Profile"));
				newInvocation.setExpression(qualifiedName);
				newInvocation.setName(ast.newSimpleName("add"));
				NumberLiteral literal1 = ast.newNumberLiteral(String.valueOf(index));
				newInvocation.arguments().add(literal1);
				BooleanLiteral literal2 = ast.newBooleanLiteral(branch);
				newInvocation.arguments().add(literal2);
				newInvocation.arguments().add(ast.newQualifiedName(ast.newQualifiedName(ast.newName(new String[] {"melt", "core", "Predicate"}), ast.newSimpleName("TYPE")), ast.newSimpleName(type.toString())));
				Statement newStatement = ast.newExpressionStatement(newInvocation);
				return newStatement;
			}
				
			@Override
			public boolean visit(MethodDeclaration methodDeclaration) {
				methodName = methodDeclaration.getName().getFullyQualifiedName();
				// to match with the constraints from concolic execution
				if (methodName.equals(className.substring(className.lastIndexOf(".") + 1))) {
					methodName = "<init>";
				}
				
				int size = 0;
				List<?> parameters = methodDeclaration.parameters();
				signature = "(";
				if (parameters != null) {
					size = parameters.size();
					for (int i = 0; i < size; i++) {
						SingleVariableDeclaration svd = (SingleVariableDeclaration)parameters.get(i);
						if (i < size - 1) {
							signature += svd.getType() + ",";
						} else {
							signature += svd.getType();
						}
					}
				}
				signature += ")";
				
				if (className.equals(Config.MAINCLASS) && methodName.equals(Config.METHOD)) {
					// construct the entry method
					String id = methodDeclaration.getReturnType2().toString() + " " + methodName + signature;
					// instrument Tainter.taint
					if (id.equals(Config.ENTRYMETHOD)) {
						Block block = methodDeclaration.getBody();
						ListRewrite listRewrite = rewriter.getListRewrite(block, Block.STATEMENTS_PROPERTY);
						for (int i = size - 1; i >= 0; i--) {
							listRewrite.insertFirst(createAssignmentStatement(Config.PARAMETERS[i], Config.CLS[i], (int)Math.pow(2, i)), null);
						}
					}
				}
				return super.visit(methodDeclaration);
			}
			
			@SuppressWarnings("unchecked")
			private Statement createAssignmentStatement(String parameter, Class<?> cls, int tag) {
			    MethodInvocation invocation = ast.newMethodInvocation();
			    //TODO data-flow-enabled or control-flow-enabled taint analysis (using Tainter or MultiTainter respectively)
			    QualifiedName qualifiedName = ast.newQualifiedName(ast.newName(new String[] {"edu", "columbia", "cs", "psl", "phosphor", "runtime"}), ast.newSimpleName("Tainter"));
			    invocation.setExpression(qualifiedName);
			    if (cls == byte.class) {
				    invocation.setName(ast.newSimpleName("taintedByte"));
				} else if (cls == short.class) {
					invocation.setName(ast.newSimpleName("taintedShort"));
				} else if (cls == char.class) {
					invocation.setName(ast.newSimpleName("taintedChar"));
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
				} else {
					System.err.println("[melt] unsupported input type " + cls);
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
			public void endVisit(MethodDeclaration methodDeclaration) {
				if (hasRecursion) {
					hasRecursion = false;
					methodDeclaration.accept(new ASTVisitor() {

						@SuppressWarnings("unchecked")
						@Override
						public boolean visit(MethodDeclaration node) {
							MethodInvocation newInvocation = ast.newMethodInvocation();
							QualifiedName qualifiedName = ast.newQualifiedName(ast.newName(new String[] {"melt", "core"}), ast.newSimpleName("Profile"));
							newInvocation.setExpression(qualifiedName);
							newInvocation.setName(ast.newSimpleName("add"));
							NumberLiteral literal1 = ast.newNumberLiteral("-2");
							newInvocation.arguments().add(literal1);
							BooleanLiteral literal2 = ast.newBooleanLiteral(false);
							newInvocation.arguments().add(literal2);
							newInvocation.arguments().add(ast.newNullLiteral());
							Statement newStatement = ast.newExpressionStatement(newInvocation);
							
							ListRewrite listRewrite = rewriter.getListRewrite(node.getBody(), Block.STATEMENTS_PROPERTY);
							listRewrite.insertFirst(newStatement, null);
							
							List<?> ss = node.getBody().statements();
							if (!(ss.get(ss.size() - 1) instanceof ReturnStatement)) {
								System.out.println("[melt] finish recursion normally at " + className + "." + methodName + signature);
								MethodInvocation newInvocation1 = ast.newMethodInvocation();
								QualifiedName qualifiedName1 = ast.newQualifiedName(ast.newName(new String[] {"melt", "core"}), ast.newSimpleName("Profile"));
								newInvocation1.setExpression(qualifiedName1);
								newInvocation1.setName(ast.newSimpleName("add"));
								NumberLiteral literal11 = ast.newNumberLiteral("-3");
								newInvocation1.arguments().add(literal11);
								BooleanLiteral literal21 = ast.newBooleanLiteral(false);
								newInvocation1.arguments().add(literal21);
								newInvocation1.arguments().add(ast.newNullLiteral());
								Statement newStatement1 = ast.newExpressionStatement(newInvocation1);
								
								ListRewrite listRewrite1 = rewriter.getListRewrite(node.getBody(), Block.STATEMENTS_PROPERTY);
								listRewrite1.insertLast(newStatement1, null);
							}
							return super.visit(node);
						}

						@SuppressWarnings("unchecked")
						@Override
						public boolean visit(ReturnStatement node) {
							System.out.println("[melt] finish recursion by return at " + className + "." + methodName + signature);
							MethodInvocation newInvocation = ast.newMethodInvocation();
							QualifiedName qualifiedName = ast.newQualifiedName(ast.newName(new String[] {"melt", "core"}), ast.newSimpleName("Profile"));
							newInvocation.setExpression(qualifiedName);
							newInvocation.setName(ast.newSimpleName("add"));
							NumberLiteral literal1 = ast.newNumberLiteral("-3");
							newInvocation.arguments().add(literal1);
							BooleanLiteral literal2 = ast.newBooleanLiteral(false);
							newInvocation.arguments().add(literal2);
							newInvocation.arguments().add(ast.newNullLiteral());
							Statement newStatement = ast.newExpressionStatement(newInvocation);
							
							ListRewrite listRewrite = rewriter.getListRewrite(node.getParent(), Block.STATEMENTS_PROPERTY);
							listRewrite.insertBefore(newStatement, node, null);
							
							return super.visit(node);
						}
						
					});
				}
				methodName = null;
				signature = null;
				super.endVisit(methodDeclaration);
			}
			
			@Override
			public void endVisit(DoStatement node) {
				nestedLoops--;
				super.endVisit(node);
			}

			@Override
			public void endVisit(ForStatement node) {
				nestedLoops--;
				super.endVisit(node);
			}

			@Override
			public void endVisit(EnhancedForStatement node) {
				nestedLoops--;
				super.endVisit(node);
			}

			@Override
			public void endVisit(WhileStatement node) {
				nestedLoops--;
				super.endVisit(node);
			}

			@SuppressWarnings("unchecked")
			@Override
			public boolean visit(ReturnStatement node) {
				if (nestedLoops > 0) {
					System.out.println("[melt] return statements in loops at " + className + "." + methodName + signature);
					
					MethodInvocation newInvocation = ast.newMethodInvocation();
					QualifiedName qualifiedName = ast.newQualifiedName(ast.newName(new String[] {"melt", "core"}), ast.newSimpleName("Profile"));
					newInvocation.setExpression(qualifiedName);
					newInvocation.setName(ast.newSimpleName("add"));
					NumberLiteral literal1 = ast.newNumberLiteral("-1");
					newInvocation.arguments().add(literal1);
					BooleanLiteral literal2 = ast.newBooleanLiteral(false);
					newInvocation.arguments().add(literal2);
					newInvocation.arguments().add(ast.newNullLiteral());
					Statement newStatement = ast.newExpressionStatement(newInvocation);
					
					ListRewrite listRewrite = rewriter.getListRewrite(node.getParent(), Block.STATEMENTS_PROPERTY);
					listRewrite.insertBefore(newStatement, node, null);				
				}
				return super.visit(node);
			}
			
			@Override
			public boolean visit(MethodInvocation methodInvocation) {
				Expression receiver = methodInvocation.getExpression();
				IMethodBinding imb = methodInvocation.resolveMethodBinding();
				if ((receiver != null && receiver.toString().equals("this")) || 
						(receiver == null && ((imb.getModifiers() & Modifier.STATIC) > 0))) {
					String mn = methodInvocation.getName().getFullyQualifiedName();
					// to match with the constraints from concolic execution
					if (mn.equals(className.substring(className.lastIndexOf(".") + 1))) {
						mn = "<init>";
					}
					ITypeBinding[] tp = methodInvocation.resolveMethodBinding().getParameterTypes();
					String sn = "(";
					if (tp != null) {
						int size = tp.length;
						for (int i = 0; i < size; i++) {
							if (i < size - 1) {
								sn += tp[i].getName() + ",";
							} else {
								sn += tp[i].getName();
							}
						}						
					}
					sn += ")";
					
					if (mn.equals(methodName) && sn.equals(signature)) {
						System.out.println("[melt] method recursion at " + className + "." + methodName + signature);
						hasRecursion = true;
					}
				}
				return super.visit(methodInvocation);
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
	private void format(File root) throws IOException, MalformedTreeException, BadLocationException {
		if (root.isFile()) {
			if (root.getName().endsWith(".java")) {
				formatFile(root);
			}
		} else {
			File[] files = root.listFiles();
			java.util.Arrays.sort(files);
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
		Map<?, ?> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parser.setCompilerOptions(options);
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
			
			public boolean visit(EnhancedForStatement foreachStatement) {
				insertBraces(foreachStatement.getBody());
				return super.visit(foreachStatement);
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
			Config.loadProperties("/home/bhchen/workspace/testing/benchmark1-art/src/dt/original/Remainder.melt");
			
			Instrumenter instrumenter = new Instrumenter("/home/bhchen/workspace/testing/benchmark1-art/src/dt/original/Remainder.java");
			instrumenter.instrument();
			
			ArrayList<Predicate> pList = instrumenter.getPredicates();
			int size = pList.size();
			for (int i = 0; i < size; i++) {
				System.out.println("[melt] " + pList.get(i));
			}
		} catch (IOException | MalformedTreeException | BadLocationException e) {
			e.printStackTrace();
		}
	}

}
