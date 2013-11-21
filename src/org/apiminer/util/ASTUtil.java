package org.apiminer.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

//TODO review all AST manipulation
public class ASTUtil {
	
	private static final Logger LOGGER  = Logger.getLogger(ASTUtil.class);

	public static enum VariableType {
		READ, WRITE;
	}

	public static final ASTNode getParent(ASTNode node, Class<?> nodeType) {
		do {
			node = node.getParent();
		} while (node != null && !nodeType.isInstance(node));
		return node;
	}
	
	public static final ASTNode getParent(ASTNode node, Class<?> nodeType, ASTNode fromNode) {
		do {
			node = node.getParent();
			if (fromNode == node && !nodeType.isInstance(node)) {
				return null;
			}
		} while (node != null && !nodeType.isInstance(node));
		return node;
	}
	
	public static final ASTNode getParent(ASTNode node, Class<?> nodeType, int maxLevel) {
		int count = 1;
		do {
			node = node.getParent();
			if (count++ > maxLevel && !nodeType.isInstance(node)) {
				return null;
			}
		} while (node != null && !nodeType.isInstance(node));
		return node;
	}

	public static final List<ASTNode> getAncestry(ASTNode node,
			Class<?> nodeType) {
		List<ASTNode> nodes = new LinkedList<ASTNode>();
		do {
			node = node.getParent();
			if (nodeType.isInstance(node)) {
				nodes.add(node);
			}
		} while (node != null);
		return nodes;
	}

	@Deprecated
	private static final HashMap<IBinding, LinkedHashSet<ASTNode>> joinResultMaps(
			Map<IBinding, LinkedHashSet<ASTNode>> m1,
			Map<IBinding, LinkedHashSet<ASTNode>> m2) {

		HashMap<IBinding, LinkedHashSet<ASTNode>> finalMap = new HashMap<IBinding, LinkedHashSet<ASTNode>>();
		finalMap.putAll(m1);

		if (m2 == null) {
			return finalMap;
		}

		for (IBinding o : m2.keySet()) {
			if (finalMap.containsKey(o)) {
				finalMap.get(o).addAll(m2.get(o));
			} else {
				finalMap.put(o, m2.get(o));
			}
		}

		return finalMap;

	}



	@Deprecated
	public static Map<IBinding, LinkedHashSet<ASTNode>> collectRelatedVariables(
			ASTNode node) {

		if (node == null) {
			return null;
		}

		HashMap<IBinding, LinkedHashSet<ASTNode>> mapResults = new HashMap<IBinding, LinkedHashSet<ASTNode>>();
		Map<IBinding, LinkedHashSet<ASTNode>> result = null;

		switch (node.getNodeType()) {

		/*
		 * Type org.eclipse.jdt.core.dom.Annotation is unecessary perform 
		 */

		case ASTNode.ARRAY_ACCESS: {
			ArrayAccess arrayAccess = (ArrayAccess) node;

			result = collectRelatedVariables(arrayAccess.getArray());
			mapResults = joinResultMaps(mapResults, result);

			result = collectRelatedVariables(arrayAccess.getIndex());
			mapResults = joinResultMaps(mapResults, result);

			break;
		}

		case ASTNode.ARRAY_CREATION: {
			ArrayCreation arrayCreation = (ArrayCreation) node;
			for (Object o : arrayCreation.dimensions()) {
				result = collectRelatedVariables((ASTNode) o);
				mapResults = joinResultMaps(mapResults, result);
			}

			result = collectRelatedVariables(arrayCreation.getInitializer());
			mapResults = joinResultMaps(mapResults, result);

			result = collectRelatedVariables(arrayCreation.getType());
			mapResults = joinResultMaps(mapResults, result);

			break;
		}

		case ASTNode.ARRAY_INITIALIZER: {
			ArrayInitializer arrayInitializer = (ArrayInitializer) node;
			for (Object o : arrayInitializer.expressions()) {
				result = collectRelatedVariables((ASTNode) o);
				mapResults = joinResultMaps(mapResults, result);
			}

			break;
		}

		case ASTNode.ASSIGNMENT: {
			Assignment assignment = (Assignment) node;
			result = collectRelatedVariables(assignment.getLeftHandSide());
			mapResults = joinResultMaps(mapResults, result);

			result = collectRelatedVariables(assignment.getRightHandSide());
			mapResults = joinResultMaps(mapResults, result);

			break;
		}

		case ASTNode.BOOLEAN_LITERAL: {
			// This expression do not have variables
			break; 
		}

		case ASTNode.CAST_EXPRESSION: {
			CastExpression castExpression = (CastExpression) node;
			result = collectRelatedVariables(castExpression.getExpression());
			mapResults = joinResultMaps(mapResults, result);

			break;
		}

		case ASTNode.CHARACTER_LITERAL: {
			// This expression do not have variables
			break; 
		}

		case ASTNode.CLASS_INSTANCE_CREATION: {
			ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) node;
			for (Object o : classInstanceCreation.arguments()) {
				result = collectRelatedVariables((ASTNode) o);
				mapResults = joinResultMaps(mapResults, result);
			}

			result = collectRelatedVariables(classInstanceCreation
					.getExpression());
			mapResults = joinResultMaps(mapResults, result);

			break;
		}

		case ASTNode.CONDITIONAL_EXPRESSION: {
			ConditionalExpression conditionalExpression = (ConditionalExpression) node;
			result = collectRelatedVariables(conditionalExpression
					.getElseExpression());
			mapResults = joinResultMaps(mapResults, result);

			result = collectRelatedVariables(conditionalExpression
					.getThenExpression());
			mapResults = joinResultMaps(mapResults, result);

			result = collectRelatedVariables(conditionalExpression
					.getExpression());
			mapResults = joinResultMaps(mapResults, result);

			break;
		}

		//		case ASTNode.EXPRESSION_STATEMENT: {
		//			ExpressionStatement expressionStatement = (ExpressionStatement) node;
		//			result = collectRelatedVariables(expressionStatement.getExpression());
		//			mapResults = joinResultMaps(mapResults, result);
		//
		//			break;
		//		}

		case ASTNode.FIELD_ACCESS: {
			FieldAccess fieldAccess = (FieldAccess) node;
			result = collectRelatedVariables(fieldAccess.getName());
			mapResults = joinResultMaps(mapResults, result);

			result = collectRelatedVariables(fieldAccess.getExpression());
			mapResults = joinResultMaps(mapResults, result);

			break;
		}

		case ASTNode.INFIX_EXPRESSION: {
			InfixExpression infixExpression = (InfixExpression) node;
			result = collectRelatedVariables(infixExpression.getLeftOperand());
			mapResults = joinResultMaps(mapResults, result);

			if (!infixExpression.hasExtendedOperands()) {
				result = collectRelatedVariables(infixExpression
						.getRightOperand());
				mapResults = joinResultMaps(mapResults, result);
			} else {
				for (Object o : infixExpression.extendedOperands()) {
					result = collectRelatedVariables((ASTNode) o);
					mapResults = joinResultMaps(mapResults, result);
				}
			}

			break;
		}

		case ASTNode.INSTANCEOF_EXPRESSION: {
			InstanceofExpression instanceofExpression = (InstanceofExpression) node;
			result = collectRelatedVariables(instanceofExpression
					.getLeftOperand());
			mapResults = joinResultMaps(mapResults, result);

			result = collectRelatedVariables(instanceofExpression
					.getRightOperand());
			mapResults = joinResultMaps(mapResults, result);

			break;
		}

		case ASTNode.METHOD_INVOCATION: {
			MethodInvocation methodInvocation = (MethodInvocation) node;
			for (Object o : methodInvocation.arguments()) {
				result = collectRelatedVariables((ASTNode) o);
				mapResults = joinResultMaps(mapResults, result);
			}
			result = collectRelatedVariables(methodInvocation.getExpression());
			mapResults = joinResultMaps(mapResults, result);

			break;
		}

		/*
		 *  Handler of subtypes of org.eclipse.jdt.core.dom.Name
		 */

		case ASTNode.SIMPLE_NAME: {
			SimpleName name = (SimpleName) node;
			IBinding bi = name.resolveBinding();
			if (bi != null) {
				if (bi.getKind() != IBinding.VARIABLE) {
					break;
				}
				if (mapResults.containsKey(bi)) {
					mapResults.get(bi).add(node);
				} else {
					LinkedHashSet<ASTNode> list = new LinkedHashSet<ASTNode>();
					list.add(node);
					mapResults.put(bi, list);
				}
			}

			break;
		}

		case ASTNode.QUALIFIED_NAME: {
			//TODO Implement
		}

		/*
		 *  End of handler of subtypes of org.eclipse.jdt.core.dom.Name
		 */

		case ASTNode.NULL_LITERAL: {
			// This expression do not have variables
			break; 
		}

		case ASTNode.NUMBER_LITERAL: {
			// This expression do not have variables
			break; 
		}

		case ASTNode.PARENTHESIZED_EXPRESSION: {
			ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) node;
			result = collectRelatedVariables(parenthesizedExpression
					.getExpression());
			mapResults = joinResultMaps(mapResults, result);

			break;
		}

		case ASTNode.POSTFIX_EXPRESSION: {
			PostfixExpression postfixExpression = (PostfixExpression) node;
			result = collectRelatedVariables(postfixExpression.getOperand());
			mapResults = joinResultMaps(mapResults, result);

			break;
		}

		case ASTNode.PREFIX_EXPRESSION: {
			PrefixExpression prefixExpression = (PrefixExpression) node;
			result = collectRelatedVariables(prefixExpression.getOperand());
			mapResults = joinResultMaps(mapResults, result);

			break;
		}

		case ASTNode.STRING_LITERAL: {
			// This expression do not have variables
			break; 
		}

		case ASTNode.SUPER_FIELD_ACCESS: {
			//TODO Implement
			break; 
		}

		case ASTNode.SUPER_CONSTRUCTOR_INVOCATION: {
			//TODO Evaluate the needs of handle this node type
			SuperConstructorInvocation superConstructorInvocation = (SuperConstructorInvocation) node;
			for (Object o : superConstructorInvocation.arguments()) {
				result = collectRelatedVariables((ASTNode) o);
				mapResults = joinResultMaps(mapResults, result);
			}

			result = collectRelatedVariables(superConstructorInvocation
					.getExpression());
			mapResults = joinResultMaps(mapResults, result);

			break;
		}

		case ASTNode.SUPER_METHOD_INVOCATION: {
			SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation) node;
			for (Object o : superMethodInvocation.arguments()) {
				result = collectRelatedVariables((ASTNode) o);
				mapResults = joinResultMaps(mapResults, result);
			}

			break;
		}

		case ASTNode.THIS_EXPRESSION: {
			ThisExpression thisExpression = (ThisExpression) node;
			result = collectRelatedVariables(thisExpression.getQualifier());
			mapResults = joinResultMaps(mapResults, result);

			break;
		}

		case ASTNode.TYPE_LITERAL: {
			// This expression do not have variables
			break; 
		}

		case ASTNode.VARIABLE_DECLARATION_EXPRESSION: {
			VariableDeclarationExpression variableDeclarationExpression = (VariableDeclarationExpression) node;
			for (Object o : variableDeclarationExpression.fragments()) {
				result = collectRelatedVariables((ASTNode) o);
				mapResults = joinResultMaps(mapResults, result);
			}

			break;
		}

		// This node is part of case ASTNode.VARIABLE_DECLARATION_EXPRESSION
		case ASTNode.VARIABLE_DECLARATION_FRAGMENT: {
			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) node;
			result = collectRelatedVariables(variableDeclarationFragment
					.getInitializer());
			mapResults = joinResultMaps(mapResults, result);

			result = collectRelatedVariables(variableDeclarationFragment
					.getName());
			mapResults = joinResultMaps(mapResults, result);

			break;
		}

		}

		return mapResults;

	}

	/**
	 *  Code obtained from the address 
	 *  	http://help.eclipse.org/juno/index.jsp?topic=/org.eclipse.jdt.doc.isv/guide/jdt_api_manip.htm
	 * 
	 * @param code
	 * @return formatted code
	 * @throws MalformedTreeException
	 * @throws BadLocationException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String codeFormatter(String code) throws MalformedTreeException, BadLocationException{

		// take default Eclipse formatting options
		Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();

		// initialize the compiler settings to be able to format 1.5 code
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_6);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_6);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_6);

		// change the option to wrap each enum constant on a new line
		options.put(
				DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ENUM_CONSTANTS,
				DefaultCodeFormatterConstants.createAlignmentValue(false,
						DefaultCodeFormatterConstants.WRAP_ONE_PER_LINE,
						DefaultCodeFormatterConstants.INDENT_ON_COLUMN));

		// instantiate the default code formatter with the given options
		final CodeFormatter codeFormatter = ToolFactory.createCodeFormatter(options);

		final TextEdit edit = codeFormatter.format(
				CodeFormatter.K_STATEMENTS, // format statements
				code, // source to format
				0, // starting position
				code.length(), // length
				0, // initial indentation
				System.getProperty("line.separator") // line separator
				);

		IDocument document = new Document(code);

		edit.apply(document);

		return document.get();

	}

	@Deprecated
	public static final ASTNode copyToNewNode(final ASTNode root, final Collection<Statement> statements, final AST ast){

		if (root == null) {
			return null;
		}else if (statements == null) {
			throw new NullPointerException("List of statements to include must be not null");
		}else if (ast == null){
			throw new NullPointerException("AST must be not null");
		}else if (root instanceof Statement) {
			// Faço o tratamento dos statements aqui, para evitar copia de codigo.
			if (!statements.contains(root)) {
				return null;
			}
		}

		switch(root.getNodeType()){

		case ASTNode.METHOD_DECLARATION: {
			MethodDeclaration methodDeclaration = (MethodDeclaration) root;

			MethodDeclaration md = ast.newMethodDeclaration();
			Object body = copyToNewNode(methodDeclaration.getBody(), statements, ast);
			md.setBody(body == null ? ast.newBlock() : (Block) body);
			md.setConstructor(methodDeclaration.isConstructor());
			md.setExtraDimensions(methodDeclaration.getExtraDimensions());
			md.setName((SimpleName) copyToNewNode(methodDeclaration.getName(), statements, ast));
			md.setReturnType2((Type) copyToNewNode(methodDeclaration.getReturnType2(), statements, ast));

			for (int i = 0; i < methodDeclaration.parameters().size(); i++) { 
				md.parameters().add(i, copyToNewNode((ASTNode) methodDeclaration.parameters().get(i), statements, ast));
			}

			for (int i = 0; i < methodDeclaration.thrownExceptions().size(); i++) { 
				md.thrownExceptions().add(i, copyToNewNode((ASTNode) methodDeclaration.thrownExceptions().get(i), statements, ast));
			}


			return md;
		}


		/**
		 *  Tratamento dos tipos de org.eclipse.jdt.core.dom.Statement
		 */

		case ASTNode.ASSERT_STATEMENT: {
			AssertStatement assertStatement = (AssertStatement) root;

			AssertStatement newStatement = ast.newAssertStatement();
			newStatement.setExpression((Expression) copyToNewNode(assertStatement.getExpression(), statements, ast));
			newStatement.setMessage((Expression) copyToNewNode(assertStatement.getMessage(), statements, ast));

			return newStatement;
		}

		case ASTNode.BLOCK: {

			Block block = (Block) root;
			Block newBlock = ast.newBlock();
			for (Object oSt : block.statements()) {
				Statement statement = (Statement) oSt;
				if (statements.contains(statement)) {
					newBlock.statements().add(copyToNewNode(statement, statements, ast));
				}
			}

			return newBlock;
		}

		case ASTNode.BREAK_STATEMENT: {
			BreakStatement breakStatement = (BreakStatement) root;

			BreakStatement newStatement = ast.newBreakStatement();
			newStatement.setLabel((SimpleName) copyToNewNode(breakStatement.getLabel(), statements, ast));

			return newStatement;
		}

		case ASTNode.CONSTRUCTOR_INVOCATION: {
			ConstructorInvocation constructorInvocation = (ConstructorInvocation) root;

			ConstructorInvocation newConstructorInvocation = ast.newConstructorInvocation();

			for (Object o : constructorInvocation.arguments()) {
				newConstructorInvocation.arguments().add(copyToNewNode((Expression) o, statements, ast));
			}

			for (Object o : constructorInvocation.typeArguments()) {
				newConstructorInvocation.typeArguments().add(copyToNewNode((Type) o, statements, ast));
			}

			return newConstructorInvocation;
		}

		case ASTNode.CONTINUE_STATEMENT: {
			ContinueStatement continueStatement = (ContinueStatement) root;

			ContinueStatement newStatement = ast.newContinueStatement();
			newStatement.setLabel((SimpleName) copyToNewNode(continueStatement.getLabel(), statements, ast));
			return newStatement;
		}

		case ASTNode.DO_STATEMENT: {
			DoStatement doStatement = (DoStatement) root;

			DoStatement newStatement = ast.newDoStatement();
			Object body = copyToNewNode(doStatement.getBody(), statements, ast);
			newStatement.setBody( body == null ? ast.newBlock() : (Statement) body );
			newStatement.setExpression((Expression) copyToNewNode(doStatement.getExpression(), statements, ast));

			return newStatement;
		}

		case ASTNode.EMPTY_STATEMENT: {
			return ast.newEmptyStatement();
		}

		case ASTNode.ENHANCED_FOR_STATEMENT: {
			EnhancedForStatement enhancedForStatement = (EnhancedForStatement) root;

			EnhancedForStatement newStatement = ast.newEnhancedForStatement();
			Object body = copyToNewNode(enhancedForStatement.getBody(), statements, ast);
			newStatement.setBody( body == null ? ast.newBlock() : (Statement) body );
			newStatement.setExpression((Expression) copyToNewNode(enhancedForStatement.getExpression(), statements, ast));
			newStatement.setParameter((SingleVariableDeclaration) copyToNewNode(enhancedForStatement.getParameter(), statements, ast));

			return newStatement;
		}

		case ASTNode.EXPRESSION_STATEMENT: {
			ExpressionStatement expressionStatement = (ExpressionStatement) root;
			return ast.newExpressionStatement((Expression) copyToNewNode(expressionStatement.getExpression(), statements, ast));
		}

		case ASTNode.FOR_STATEMENT: {
			ForStatement forStatement = (ForStatement) root;

			ForStatement newStatement = ast.newForStatement();
			Object body = copyToNewNode(forStatement.getBody(), statements, ast);
			newStatement.setBody( body == null ? ast.newBlock() : (Statement) body );
			newStatement.setExpression((Expression) copyToNewNode(forStatement.getExpression(), statements, ast));
			for (Object o : forStatement.initializers()) {
				newStatement.initializers().add(copyToNewNode((Expression) o, statements, ast));
			}
			for (Object o : forStatement.updaters()) {
				newStatement.updaters().add(copyToNewNode((Expression) o, statements, ast));
			}

			return newStatement;
		}

		case ASTNode.IF_STATEMENT: {
			IfStatement ifStatement = (IfStatement) root;

			IfStatement newStatement = ast.newIfStatement();
			Object elseStatement = copyToNewNode(ifStatement.getElseStatement(), statements, ast);
			newStatement.setElseStatement(elseStatement == null ? ast.newEmptyStatement() : (Statement) elseStatement);
			newStatement.setExpression((Expression) copyToNewNode(ifStatement.getExpression(), statements, ast));
			Object thenStatement = copyToNewNode(ifStatement.getThenStatement(), statements, ast);
			newStatement.setThenStatement(thenStatement == null ? ast.newEmptyStatement() : (Statement) thenStatement);

			return newStatement;
		}

		case ASTNode.LABELED_STATEMENT: {
			LabeledStatement labeledStatement = (LabeledStatement) root;

			LabeledStatement newStatement = ast.newLabeledStatement();
			Object body = copyToNewNode(labeledStatement.getBody(), statements, ast);
			newStatement.setBody( body == null ? ast.newBlock() : (Statement) body );
			newStatement.setLabel((SimpleName) copyToNewNode(labeledStatement.getLabel(), statements, ast));

			return newStatement;
		}

		case ASTNode.RETURN_STATEMENT: {
			ReturnStatement returnStatement = (ReturnStatement) root;

			ReturnStatement newStatement = ast.newReturnStatement();
			newStatement.setExpression((Expression) copyToNewNode(returnStatement.getExpression(), statements, ast));

			return newStatement;
		}

		case ASTNode.SUPER_CONSTRUCTOR_INVOCATION: {
			SuperConstructorInvocation superConstructorInvocation = (SuperConstructorInvocation) root;

			SuperConstructorInvocation newStatement = ast.newSuperConstructorInvocation();
			newStatement.setExpression((Expression) copyToNewNode(newStatement.getExpression(), statements, ast));
			for (Object o : superConstructorInvocation.arguments()) {
				newStatement.arguments().add(copyToNewNode((Expression) o, statements, ast));
			}
			for (Object o : superConstructorInvocation.typeArguments()) {
				newStatement.arguments().add(copyToNewNode((Type) o, statements, ast));
			}

			return newStatement;
		}

		case ASTNode.SWITCH_CASE: {
			SwitchCase switchCase = (SwitchCase) root;

			SwitchCase newStatement = ast.newSwitchCase();
			newStatement.setExpression((Expression) copyToNewNode(switchCase.getExpression(), statements, ast));

			return newStatement;
		}

		case ASTNode.SWITCH_STATEMENT: {
			SwitchStatement switchStatement = (SwitchStatement) root;

			SwitchStatement newStatement = ast.newSwitchStatement();
			newStatement.setExpression((Expression) copyToNewNode(switchStatement.getExpression(), statements, ast));
			for (Object o : switchStatement.statements()) {
				Statement statement = (Statement) o;
				if (statements.contains(statement)) {
					newStatement.statements().add(copyToNewNode(statement, statements, ast));
				}
			}

			return newStatement;
		}

		case ASTNode.SYNCHRONIZED_STATEMENT: {
			SynchronizedStatement synchronizedStatement = (SynchronizedStatement) root;

			SynchronizedStatement newStatement = ast.newSynchronizedStatement();
			Object body = copyToNewNode(synchronizedStatement.getBody(), statements, ast);
			newStatement.setBody( body == null ? ast.newBlock() : (Block) body );
			newStatement.setExpression((Expression) copyToNewNode(synchronizedStatement.getExpression(), statements, ast));

			return newStatement;
		}

		case ASTNode.THROW_STATEMENT: {
			ThrowStatement throwStatement = (ThrowStatement) root;

			ThrowStatement newStatement = ast.newThrowStatement();
			newStatement.setExpression((Expression) copyToNewNode(throwStatement.getExpression(), statements, ast));

			return newStatement;
		}

		case ASTNode.TRY_STATEMENT: {
			TryStatement tryStatement = (TryStatement) root;

			TryStatement newStatement = ast.newTryStatement();
			Object body = copyToNewNode(tryStatement.getBody(), statements, ast);
			newStatement.setBody(body == null ? ast.newBlock() : (Block) body);
			Object finallyBlock = copyToNewNode(tryStatement.getFinally(), statements, ast); 
			newStatement.setFinally(finallyBlock == null ? ast.newBlock() : (Block) finallyBlock);
			for (Object o : tryStatement.catchClauses()) {
				newStatement.catchClauses().add(copyToNewNode( (CatchClause) o, statements, ast));
			}

			return newStatement;
		}

		case ASTNode.TYPE_DECLARATION_STATEMENT: {
			// TypeDeclarationStatement typeDeclarationStatement = (TypeDeclarationStatement) root;
			// return ast.newTypeDeclarationStatement((TypeDeclaration) copyToNewNode(typeDeclarationStatement.getDeclaration(), statements, ast));
			//TODO Nao necessario ainda
			break;
		}

		case ASTNode.VARIABLE_DECLARATION_STATEMENT: {
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) root;

			LinkedList<VariableDeclarationFragment> newFragments = new LinkedList<VariableDeclarationFragment>();
			for (Object o : variableDeclarationStatement.fragments()) {
				newFragments.add((VariableDeclarationFragment) copyToNewNode((ASTNode) o, statements, ast));
			}

			VariableDeclarationStatement newStatement = ast.newVariableDeclarationStatement(newFragments.removeFirst());
			for (Object o : variableDeclarationStatement.modifiers()) {
				try {
					newStatement.modifiers().add(copyToNewNode((ASTNode) o, statements, ast));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			newStatement.setType((Type) copyToNewNode(variableDeclarationStatement.getType(), statements, ast));
			newStatement.fragments().addAll(newFragments);

			return newStatement;
		}

		case ASTNode.WHILE_STATEMENT: {
			WhileStatement whileStatement = (WhileStatement) root;

			WhileStatement newStatement = ast.newWhileStatement();
			Object body = copyToNewNode(whileStatement.getBody(), statements, ast);
			newStatement.setBody( body == null ? ast.newBlock() : (Statement) body );
			newStatement.setExpression((Expression) copyToNewNode(whileStatement.getExpression(), statements, ast));

			return newStatement;
		}

		/**
		 *  Fim org.eclipse.jdt.core.dom.Statement
		 */

		case ASTNode.MEMBER_VALUE_PAIR: {
			MemberValuePair memberValuePair = (MemberValuePair) root;

			MemberValuePair newMember = ast.newMemberValuePair();
			newMember.setName((SimpleName) copyToNewNode(memberValuePair.getName(), statements, ast));
			newMember.setValue((Expression) copyToNewNode(memberValuePair.getValue(), statements, ast));

			return newMember;
		}

		/**
		 *  Tratando tipo org.eclipse.jdt.core.dom.IExtendedModifier
		 */

		case ASTNode.MODIFIER: {
			Modifier modifier = (Modifier) root;
			return ast.newModifier(modifier.getKeyword());
		}

		case ASTNode.MARKER_ANNOTATION: {
			MarkerAnnotation markerAnnotation = (MarkerAnnotation) root;

			MarkerAnnotation newAnnotation = ast.newMarkerAnnotation();
			newAnnotation.setTypeName((Name) copyToNewNode(markerAnnotation.getTypeName(), statements, ast));

			return newAnnotation;
		}

		case ASTNode.NORMAL_ANNOTATION: {
			NormalAnnotation normalAnnotation = (NormalAnnotation) root;

			NormalAnnotation newAnnotation = ast.newNormalAnnotation();
			newAnnotation.setTypeName((Name) copyToNewNode(normalAnnotation.getTypeName(), statements, ast));
			for (Object o : normalAnnotation.values()) {
				newAnnotation.values().add(copyToNewNode((ASTNode) o, statements, ast));
			}

			return newAnnotation;
		}

		case ASTNode.SINGLE_MEMBER_ANNOTATION: {
			SingleMemberAnnotation singleMemberAnnotation = (SingleMemberAnnotation) root;

			SingleMemberAnnotation newAnnotation = ast.newSingleMemberAnnotation();
			newAnnotation.setTypeName((Name) copyToNewNode(singleMemberAnnotation.getTypeName(), statements, ast));
			newAnnotation.setValue((Expression) copyToNewNode(singleMemberAnnotation.getValue(), statements, ast));

			return newAnnotation;
		}

		/**
		 *  Fim org.eclipse.jdt.core.dom.IExtendedModifier
		 */

		case ASTNode.CATCH_CLAUSE: {
			CatchClause catchClause = (CatchClause) root;

			CatchClause newClause = ast.newCatchClause();
			Block newBlock = (Block) copyToNewNode(catchClause.getBody(), statements, ast);
			newClause.setBody(newBlock == null ? ast.newBlock() : newBlock);
			newClause.setException((SingleVariableDeclaration) copyToNewNode(catchClause.getException(), statements, ast));

			return newClause;
		}

		/**
		 * Tratamento dos tipos de org.eclipse.jdt.core.dom.VariableDeclaration
		 */

		case ASTNode.SINGLE_VARIABLE_DECLARATION: {
			SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) root;

			SingleVariableDeclaration newVD = ast.newSingleVariableDeclaration();
			newVD.setExtraDimensions(singleVariableDeclaration.getExtraDimensions());
			newVD.setInitializer((Expression) copyToNewNode(singleVariableDeclaration.getInitializer(), statements, ast));
			for (Object o : singleVariableDeclaration.modifiers()) {
				newVD.modifiers().add(copyToNewNode((ASTNode) o, statements, ast));
			}

			newVD.setName((SimpleName) copyToNewNode(singleVariableDeclaration.getName(), statements, ast));
			newVD.setType((Type) copyToNewNode(singleVariableDeclaration.getType(), statements, ast));
			newVD.setVarargs(singleVariableDeclaration.isVarargs());

			return newVD;
		}

		case ASTNode.VARIABLE_DECLARATION_FRAGMENT: {
			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) root;

			VariableDeclarationFragment newExpression = ast.newVariableDeclarationFragment();
			newExpression.setExtraDimensions(variableDeclarationFragment.getExtraDimensions());
			newExpression.setInitializer((Expression) copyToNewNode(variableDeclarationFragment.getInitializer(), statements, ast));
			newExpression.setName((SimpleName) copyToNewNode(variableDeclarationFragment.getName(), statements, ast));

			return newExpression;
		}

		/**
		 * Fim org.eclipse.jdt.core.dom.VariableDeclaration
		 */

		/**
		 * Tratamento dos tipos de org.eclipse.jdt.core.dom.Expression
		 */

		case ASTNode.ARRAY_ACCESS: {
			ArrayAccess arrayAccess = (ArrayAccess) root;

			ArrayAccess newExpression = ast.newArrayAccess();
			newExpression.setArray((Expression) copyToNewNode(arrayAccess.getArray(), statements, ast));
			newExpression.setIndex((Expression) copyToNewNode(arrayAccess.getIndex(), statements, ast));

			return newExpression;
		}

		case ASTNode.ARRAY_CREATION: {
			ArrayCreation arrayCreation = (ArrayCreation) root;

			ArrayCreation newExpression = ast.newArrayCreation();
			newExpression.setInitializer((ArrayInitializer) copyToNewNode(arrayCreation.getInitializer(), statements, ast));
			newExpression.setType((ArrayType) copyToNewNode(arrayCreation.getType(), statements, ast));
			for (Object o : arrayCreation.dimensions()) {
				newExpression.dimensions().add(copyToNewNode( (Expression) o, statements, ast));
			}

			return newExpression;
		}

		case ASTNode.ARRAY_INITIALIZER: {
			ArrayInitializer arrayInitializer = (ArrayInitializer) root;

			ArrayInitializer newExpression = ast.newArrayInitializer();
			for (Object o : arrayInitializer.expressions()) {
				newExpression.expressions().add(copyToNewNode( (Expression) o, statements, ast));
			}

			return newExpression;
		}

		case ASTNode.ASSIGNMENT: {
			Assignment assignment = (Assignment) root;

			Assignment newExpression = ast.newAssignment();
			newExpression.setLeftHandSide((Expression) copyToNewNode(assignment.getLeftHandSide(), statements, ast));
			newExpression.setOperator(assignment.getOperator());
			newExpression.setRightHandSide((Expression) copyToNewNode(assignment.getRightHandSide(), statements, ast));

			return newExpression;
		}

		case ASTNode.BOOLEAN_LITERAL: {
			BooleanLiteral booleanLiteral = (BooleanLiteral) root;
			return ast.newBooleanLiteral(booleanLiteral.booleanValue());
		}

		case ASTNode.CAST_EXPRESSION: {
			CastExpression castExpression = (CastExpression) root;

			CastExpression newExpession = ast.newCastExpression();
			newExpession.setExpression((Expression) copyToNewNode(castExpression.getExpression(), statements, ast));
			newExpession.setType((Type) copyToNewNode(castExpression.getType(), statements, ast));

			return newExpession;
		}

		case ASTNode.CHARACTER_LITERAL : {
			CharacterLiteral characterLiteral = (CharacterLiteral) root;

			CharacterLiteral newExpression = ast.newCharacterLiteral();
			newExpression.setCharValue(characterLiteral.charValue());
			newExpression.setEscapedValue(characterLiteral.getEscapedValue());

			return newExpression;
		}

		case ASTNode.CLASS_INSTANCE_CREATION: {
			ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) root;

			ClassInstanceCreation newExpression = ast.newClassInstanceCreation();
			newExpression.setAnonymousClassDeclaration((AnonymousClassDeclaration) copyToNewNode(classInstanceCreation.getAnonymousClassDeclaration(), statements, ast));
			newExpression.setExpression((Expression) copyToNewNode(classInstanceCreation.getExpression(), statements, ast));
			newExpression.setType((Type) copyToNewNode(classInstanceCreation.getType(), statements, ast));
			for (Object o : classInstanceCreation.arguments()) {
				newExpression.arguments().add(copyToNewNode( (Expression) o, statements, ast));
			}
			for (Object o : classInstanceCreation.typeArguments()) {
				newExpression.typeArguments().add(copyToNewNode( (Expression) o, statements, ast));
			}

			return newExpression;
		}

		case ASTNode.CONDITIONAL_EXPRESSION: {
			ConditionalExpression conditionalExpression = (ConditionalExpression) root;

			ConditionalExpression newExpression = ast.newConditionalExpression();
			newExpression.setElseExpression((Expression) copyToNewNode(conditionalExpression.getElseExpression(), statements, ast));
			newExpression.setExpression((Expression) copyToNewNode(conditionalExpression.getExpression(), statements, ast));
			newExpression.setThenExpression((Expression) copyToNewNode(conditionalExpression.getThenExpression(), statements, ast));

			return newExpression;
		}

		case ASTNode.FIELD_ACCESS: {
			FieldAccess fieldAccess = (FieldAccess) root;

			FieldAccess newExpression = ast.newFieldAccess();
			newExpression.setExpression((Expression) copyToNewNode(fieldAccess.getExpression(), statements, ast));
			newExpression.setName((SimpleName) copyToNewNode(fieldAccess.getName(), statements, ast));

			return newExpression;
		}

		case ASTNode.INFIX_EXPRESSION: {
			InfixExpression infixExpression = (InfixExpression) root;

			InfixExpression newExpression = ast.newInfixExpression();
			newExpression.setLeftOperand((Expression) copyToNewNode(infixExpression.getLeftOperand(), statements, ast));
			newExpression.setOperator(infixExpression.getOperator());
			newExpression.setRightOperand((Expression) copyToNewNode(infixExpression.getRightOperand(), statements, ast));
			for (Object o : infixExpression.extendedOperands()) {
				newExpression.extendedOperands().add(copyToNewNode((Expression) o, statements, ast));
			}

			return newExpression;
		}

		case ASTNode.INSTANCEOF_EXPRESSION: {
			InstanceofExpression instanceofExpression = (InstanceofExpression) root;

			InstanceofExpression newExpression = ast.newInstanceofExpression();
			newExpression.setLeftOperand((Expression) copyToNewNode(instanceofExpression.getLeftOperand(), statements, ast));
			newExpression.setRightOperand((Type) copyToNewNode(instanceofExpression.getRightOperand(), statements, ast));

			return newExpression;
		}

		case ASTNode.METHOD_INVOCATION: {
			MethodInvocation methodInvocation = (MethodInvocation) root;

			MethodInvocation newExpression = ast.newMethodInvocation();
			newExpression.setExpression((Expression) copyToNewNode(methodInvocation.getExpression(), statements, ast));
			newExpression.setName((SimpleName) copyToNewNode(methodInvocation.getName(), statements, ast));
			for (Object o : methodInvocation.arguments()) {
				newExpression.arguments().add(copyToNewNode( (Expression) o, statements, ast));
			}
			for (Object o : methodInvocation.typeArguments()) {
				newExpression.typeArguments().add(copyToNewNode( (Expression) o, statements, ast));
			}

			return newExpression;
		}

		case ASTNode.NULL_LITERAL: {
			return ast.newNullLiteral();
		}

		case ASTNode.NUMBER_LITERAL: {
			NumberLiteral numberLiteral = (NumberLiteral) root;
			return ast.newNumberLiteral(numberLiteral.getToken());
		}

		case ASTNode.PARENTHESIZED_EXPRESSION: {
			ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) root;

			ParenthesizedExpression newExpression = ast.newParenthesizedExpression();
			newExpression.setExpression((Expression) copyToNewNode(parenthesizedExpression.getExpression(), statements, ast));		

			return newExpression;
		}

		case ASTNode.POSTFIX_EXPRESSION: {
			PostfixExpression postfixExpression = (PostfixExpression) root;

			PostfixExpression newExpression = ast.newPostfixExpression();
			newExpression.setOperator(postfixExpression.getOperator());
			newExpression.setOperand((Expression) copyToNewNode(postfixExpression.getOperand(), statements, ast));

			return newExpression;
		}

		case ASTNode.PREFIX_EXPRESSION: {
			PrefixExpression prefixExpression = (PrefixExpression) root;

			PrefixExpression newExpression = ast.newPrefixExpression();
			newExpression.setOperator(prefixExpression.getOperator());
			newExpression.setOperand((Expression) copyToNewNode(prefixExpression.getOperand(), statements, ast));

			return newExpression;
		}

		case ASTNode.STRING_LITERAL: {
			StringLiteral stringLiteral = (StringLiteral) root;

			StringLiteral newExpression = ast.newStringLiteral();
			newExpression.setEscapedValue(stringLiteral.getEscapedValue());
			newExpression.setLiteralValue(stringLiteral.getLiteralValue());

			return newExpression;
		}

		case ASTNode.SUPER_FIELD_ACCESS: {
			SuperFieldAccess superFieldAccess = (SuperFieldAccess) root;

			SuperFieldAccess newExpression = ast.newSuperFieldAccess();
			newExpression.setName((SimpleName) copyToNewNode(superFieldAccess.getName(), statements, ast));
			newExpression.setQualifier((Name) copyToNewNode(superFieldAccess.getQualifier(), statements, ast));

			return newExpression;
		}

		case ASTNode.SUPER_METHOD_INVOCATION: {
			SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation) root;

			SuperMethodInvocation newExpression = ast.newSuperMethodInvocation();
			newExpression.setName((SimpleName) copyToNewNode(superMethodInvocation.getName(), statements, ast));
			newExpression.setQualifier((Name) copyToNewNode(superMethodInvocation.getQualifier(), statements, ast));
			for (Object o : superMethodInvocation.arguments()) {
				newExpression.arguments().add(copyToNewNode((Expression)o, statements, ast));
			}
			for (Object o : superMethodInvocation.typeArguments()) {
				newExpression.typeArguments().add(copyToNewNode((Type)o, statements, ast));
			}

			return newExpression;
		}

		case ASTNode.THIS_EXPRESSION: {
			ThisExpression thisExpression = (ThisExpression) root;

			ThisExpression newExpression = ast.newThisExpression();
			newExpression.setQualifier((Name) copyToNewNode(thisExpression.getQualifier(), statements, ast));

			return newExpression;
		}

		case ASTNode.TYPE_LITERAL: {
			TypeLiteral typeLiteral = (TypeLiteral) root;

			TypeLiteral newExpression = ast.newTypeLiteral();
			newExpression.setType((Type) copyToNewNode(typeLiteral.getType(), statements, ast));

			return newExpression;
		}

		case ASTNode.VARIABLE_DECLARATION_EXPRESSION: {
			VariableDeclarationExpression variableDeclarationExpression = (VariableDeclarationExpression) root;

			LinkedList<VariableDeclarationFragment> fragments = new LinkedList<VariableDeclarationFragment>();
			for (Object o : variableDeclarationExpression.fragments()) {
				fragments.add( (VariableDeclarationFragment) copyToNewNode( (VariableDeclarationFragment) o, statements, ast));
			}

			VariableDeclarationExpression newExpression = ast.newVariableDeclarationExpression(fragments.removeFirst());
			newExpression.fragments().addAll(fragments);
			for (Object o : variableDeclarationExpression.modifiers()) {
				newExpression.modifiers().add(copyToNewNode((ASTNode) o, statements, ast));
			}

			newExpression.setType((Type) copyToNewNode(variableDeclarationExpression.getType(), statements, ast));

			return newExpression;
		}

		/**
		 *  Fim org.eclipse.jdt.core.dom.Expression
		 */

		case ASTNode.ANONYMOUS_CLASS_DECLARATION: {
			//TODO Criar
			break;
		}


		/**
		 *  Tratamento dos tipos de org.eclipse.jdt.core.dom.Type
		 */

		case ASTNode.ARRAY_TYPE:{
			ArrayType arrayType = (ArrayType) root;
			return ast.newArrayType((Type) copyToNewNode(arrayType.getElementType(), statements, ast));
		}

		case ASTNode.PARAMETERIZED_TYPE: {
			ParameterizedType parameterizedType = (ParameterizedType) root;
			ParameterizedType newParameterizedType = ast.newParameterizedType((Type) copyToNewNode(parameterizedType.getType(), statements, ast));
			for (Object p : parameterizedType.typeArguments()) {
				newParameterizedType.typeArguments().add(copyToNewNode( (Type) p, statements, ast));
			}
			return newParameterizedType;
		}

		case ASTNode.PRIMITIVE_TYPE: {
			PrimitiveType primitiveType = (PrimitiveType) root;
			return ast.newPrimitiveType(primitiveType.getPrimitiveTypeCode());
		}

		case ASTNode.QUALIFIED_TYPE: {
			QualifiedType qualifiedType = (QualifiedType) root;
			return ast.newQualifiedType( (Type) copyToNewNode(qualifiedType.getQualifier(), statements, ast) , (SimpleName) copyToNewNode(qualifiedType.getName(), statements, ast));
		}

		case ASTNode.SIMPLE_TYPE: {
			SimpleType simpleType = (SimpleType) root;
			return ast.newSimpleType((Name) copyToNewNode(simpleType.getName(), statements, ast));
		}

		case ASTNode.WILDCARD_TYPE: {
			WildcardType wildcardType = (WildcardType) root;
			WildcardType newType = ast.newWildcardType();
			newType.setBound((Type) copyToNewNode(wildcardType.getBound(), statements, ast), wildcardType.isUpperBound());
			return newType;
		}

		/**
		 *  Tratamento dos tipos de org.eclipse.jdt.core.dom.Name
		 */

		case ASTNode.QUALIFIED_NAME: {
			QualifiedName qualifiedName = (QualifiedName) root;
			Name name = ast.newName(qualifiedName.getFullyQualifiedName());
			return ast.newQualifiedName(name, (SimpleName) copyToNewNode(qualifiedName.getName(), statements, ast));

		}

		case ASTNode.SIMPLE_NAME: {
			SimpleName simpleName = (SimpleName) root;
			return ast.newSimpleName(simpleName.getIdentifier());
		}

		/**
		 *  End org.eclipse.jdt.core.dom.Name
		 */


		default:
			return null;

		}

		return null;

	}

	public static final Statement getDeclarationStatement(final IBinding typeBinding, final ASTNode startFromNode){

		class InternalVisitor extends ASTVisitor {

			Statement statement = null;

			@Override
			public boolean preVisit2(ASTNode node) {
				return statement == null;
			}

			//			@Override
			//			public boolean visit(FieldDeclaration node) {
			//				for (Object o : node.fragments()) {
			//					VariableDeclarationFragment fragment = (VariableDeclarationFragment) o;
			//					IBinding vBinding = fragment.getName().resolveBinding();
			//					if (vBinding.equals(typeBinding)) {
			//						ASTNode parent = ASTUtil.getParent(node, Statement.class);
			//						this.statement = parent != null ? (Statement) parent : null;
			//					}
			//				}
			//				return statement == null;
			//			}

//			@Override
//			public boolean visit(SingleVariableDeclaration node) {
//				IBinding vBinding = node.getName().resolveBinding();
//				if (vBinding.equals(typeBinding)) {
//					ASTNode parent = ASTUtil.getParent(node, Statement.class);
//					this.statement = parent != null ? (Statement) parent : null;
//				}
//				return statement == null;
//			}

			@Override
			public boolean visit(VariableDeclarationStatement node) {
				for (Object o : node.fragments()) {
					VariableDeclarationFragment fragment = (VariableDeclarationFragment) o;
					IBinding vBinding = fragment.getName().resolveBinding();
					if (vBinding != null && vBinding.equals(typeBinding)) {
						this.statement = node;
					}
				}
				return statement == null;
			}

		}



		InternalVisitor internalVisitor = new InternalVisitor();
		startFromNode.accept(internalVisitor);

		return internalVisitor.statement;
	}

	public static Name getReturnVariable(MethodInvocation invocation) {

		if (invocation == null) {
			throw new IllegalArgumentException(new NullPointerException());
		}

		ASTNode parent = invocation.getParent();
		if (parent instanceof VariableDeclarationFragment) {
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) parent;
			IBinding v = fragment.getName().resolveBinding();
			if (v.getKind() == IBinding.VARIABLE) {
				return fragment.getName();
			}
		}else if (parent instanceof Assignment) {
			Assignment assignment = (Assignment) parent;
			Expression leftSide = assignment.getLeftHandSide();
			if (leftSide instanceof SimpleName) {
				IBinding nameBinding = ((SimpleName) leftSide).resolveBinding();
				if (nameBinding.getKind() == IBinding.VARIABLE) {
					return (SimpleName) leftSide;
				}
			}
		}

		return null;

	}

	public static final Statement copyStatements(final Statement root, final Collection<Statement> includeStatements, final AST ast){

		if (root == null) {
			return null;
		}else if (includeStatements == null) {
			throw new NullPointerException("List of statements to include must be not null");
		}else if (ast == null){
			throw new NullPointerException("AST must be not null");
		}

		boolean containsRoot = includeStatements.remove(root);

		switch(root.getNodeType()){

		/**
		 *  Tratamento dos tipos de org.eclipse.jdt.core.dom.Statement
		 */

		case ASTNode.ASSERT_STATEMENT: {

			if (!containsRoot) {
				return null;
			}

			AssertStatement assertStatement = (AssertStatement) root;

			AssertStatement newStatement = ast.newAssertStatement();
			newStatement.setExpression((Expression) copyAnothersNodes(assertStatement.getExpression(), includeStatements, ast));
			newStatement.setMessage((Expression) copyAnothersNodes(assertStatement.getMessage(), includeStatements, ast));

			return newStatement;
		}

		case ASTNode.BLOCK: {

			Block block = (Block) root;
			Block newBlock = ast.newBlock();
			for (Object oSt : block.statements()) { 
				Statement st = copyStatements((Statement) oSt, includeStatements, ast);
				if (st != null) {
					newBlock.statements().add(st);
				}
			}

			if (!newBlock.statements().isEmpty()) {
				return newBlock;
			}else{
				return null;
			}

		}

		case ASTNode.BREAK_STATEMENT: {

			if (!containsRoot) {
				return null;
			}

			BreakStatement breakStatement = (BreakStatement) root;

			BreakStatement newStatement = ast.newBreakStatement();
			newStatement.setLabel((SimpleName) copyAnothersNodes(breakStatement.getLabel(), includeStatements, ast));

			return newStatement;
		}

		case ASTNode.CONSTRUCTOR_INVOCATION: {

			if (!containsRoot) {
				return null;
			}

			ConstructorInvocation constructorInvocation = (ConstructorInvocation) root;

			ConstructorInvocation newConstructorInvocation = ast.newConstructorInvocation();

			for (Object o : constructorInvocation.arguments()) {
				newConstructorInvocation.arguments().add(copyAnothersNodes((Expression) o, includeStatements, ast));
			}

			for (Object o : constructorInvocation.typeArguments()) {
				newConstructorInvocation.typeArguments().add(copyAnothersNodes((Type) o, includeStatements, ast));
			}

			return newConstructorInvocation;
		}

		case ASTNode.CONTINUE_STATEMENT: {

			if (!containsRoot) {
				return null;
			}

			ContinueStatement continueStatement = (ContinueStatement) root;

			ContinueStatement newStatement = ast.newContinueStatement();
			newStatement.setLabel((SimpleName) copyAnothersNodes(continueStatement.getLabel(), includeStatements, ast));

			return newStatement;
		}

		case ASTNode.DO_STATEMENT: {
			DoStatement doStatement = (DoStatement) root;

			Statement body = copyStatements(doStatement.getBody(), includeStatements, ast);
			if (!containsRoot) {
				if (body != null) {
					return body;
				}else{
					return null;
				}
			}

			DoStatement newStatement = ast.newDoStatement();
			newStatement.setBody( body == null ? ast.newBlock() : body );
			newStatement.setExpression((Expression) copyAnothersNodes(doStatement.getExpression(), includeStatements, ast));

			return newStatement;
		}

		case ASTNode.EMPTY_STATEMENT: {
			return null;
		}

		case ASTNode.ENHANCED_FOR_STATEMENT: {
			EnhancedForStatement enhancedForStatement = (EnhancedForStatement) root;

			Statement body = copyStatements(enhancedForStatement.getBody(), includeStatements, ast);
			if (!containsRoot) {
				if (body != null) {
					return body;
				}else{
					return null;
				}
			}

			EnhancedForStatement newStatement = ast.newEnhancedForStatement();
			newStatement.setBody( body == null ? ast.newBlock() : (Statement) body );
			newStatement.setExpression((Expression) copyAnothersNodes(enhancedForStatement.getExpression(), includeStatements, ast));
			newStatement.setParameter((SingleVariableDeclaration) copyAnothersNodes(enhancedForStatement.getParameter(), includeStatements, ast));

			return newStatement;
		}

		case ASTNode.EXPRESSION_STATEMENT: {
			
			if (!containsRoot) {
				return null;
			}

			ExpressionStatement expressionStatement = (ExpressionStatement) root;
			return ast.newExpressionStatement((Expression) copyAnothersNodes(expressionStatement.getExpression(), includeStatements, ast));
		}

		case ASTNode.FOR_STATEMENT: {
			ForStatement forStatement = (ForStatement) root;


			Statement body = copyStatements(forStatement.getBody(), includeStatements, ast);
			if (!containsRoot) {
				if (body != null) {
					return body;
				}else{
					return null;
				}
			}

			ForStatement newStatement = ast.newForStatement();
			newStatement.setBody( body == null ? ast.newBlock() : (Statement) body );
			newStatement.setExpression((Expression) copyAnothersNodes(forStatement.getExpression(), includeStatements, ast));

			for (Object o : forStatement.initializers()) {
				newStatement.initializers().add(copyAnothersNodes((Expression) o, includeStatements, ast));
			}

			for (Object o : forStatement.updaters()) {
				newStatement.updaters().add(copyAnothersNodes((Expression) o, includeStatements, ast));
			}

			return newStatement;
		}

		case ASTNode.IF_STATEMENT: {
			IfStatement ifStatement = (IfStatement) root;

			IfStatement newStatement = ast.newIfStatement();

			Statement elseStatement = copyStatements(ifStatement.getElseStatement(), includeStatements, ast);
			Statement thenStatement = copyStatements(ifStatement.getThenStatement(), includeStatements, ast);
			if (!containsRoot) {
				if (elseStatement != null && thenStatement != null) {
					Block b = ast.newBlock();
					b.statements().add(thenStatement);
					b.statements().add(elseStatement);
					return b;
				}else if (elseStatement != null){
					return elseStatement;
				}else if (thenStatement != null){
					return thenStatement;
				}else{
					return null;
				}
			}

			newStatement.setExpression((Expression) copyAnothersNodes(ifStatement.getExpression(), includeStatements, ast));
			newStatement.setThenStatement(thenStatement == null ? ast.newBlock() : (Statement) thenStatement);
			newStatement.setElseStatement(elseStatement);

			return newStatement;
		}

		case ASTNode.LABELED_STATEMENT: {
			LabeledStatement labeledStatement = (LabeledStatement) root;

			Statement body = copyStatements(labeledStatement.getBody(), includeStatements, ast);
			if (!containsRoot) {
				if (body != null) {
					return body;
				}else{
					return null;
				}
			}

			LabeledStatement newStatement = ast.newLabeledStatement();
			newStatement.setBody( body == null ? ast.newBlock() : (Statement) body );
			newStatement.setLabel((SimpleName) copyAnothersNodes(labeledStatement.getLabel(), includeStatements, ast));

			return newStatement;
		}

		case ASTNode.RETURN_STATEMENT: {

			if (!containsRoot) {
				return null;
			}

			ReturnStatement returnStatement = (ReturnStatement) root;

			ReturnStatement newStatement = ast.newReturnStatement();
			newStatement.setExpression((Expression) copyAnothersNodes(returnStatement.getExpression(), includeStatements, ast));

			return newStatement;
		}

		case ASTNode.SUPER_CONSTRUCTOR_INVOCATION: {

			if (!containsRoot) {
				return null;
			}

			SuperConstructorInvocation superConstructorInvocation = (SuperConstructorInvocation) root;

			SuperConstructorInvocation newStatement = ast.newSuperConstructorInvocation();
			newStatement.setExpression((Expression) copyAnothersNodes(newStatement.getExpression(), includeStatements, ast));
			for (Object o : superConstructorInvocation.arguments()) {
				newStatement.arguments().add(copyAnothersNodes((Expression) o, includeStatements, ast));
			}
			for (Object o : superConstructorInvocation.typeArguments()) {
				newStatement.arguments().add(copyAnothersNodes((Type) o, includeStatements, ast));
			}

			return newStatement;
		}

		case ASTNode.SWITCH_CASE: {
			
			if (!containsRoot) {
				return null;
			}

			SwitchCase switchCase = (SwitchCase) root;

			SwitchCase newStatement = ast.newSwitchCase();
			newStatement.setExpression((Expression) copyAnothersNodes(switchCase.getExpression(), includeStatements, ast));

			return newStatement;
		}

		case ASTNode.SWITCH_STATEMENT: {
			SwitchStatement switchStatement = (SwitchStatement) root;

			SwitchStatement newStatement = ast.newSwitchStatement();
			newStatement.setExpression((Expression) copyAnothersNodes(switchStatement.getExpression(), includeStatements, ast));
			for (int i = 0; i < switchStatement.statements().size(); i++) {
				Object o = switchStatement.statements().get(i);  
				Statement st = copyStatements((Statement) o, includeStatements, ast);
				if (st != null) {
					for (int j = i -1 ; j >= 0; j--) {
						if (switchStatement.statements().get(j) instanceof SwitchCase) {
							SwitchCase switchCase = (SwitchCase) switchStatement.statements().get(j);
							SwitchCase newSwitchCase = ast.newSwitchCase();
							newSwitchCase.setExpression((Expression) copyAnothersNodes(switchCase.getExpression(), includeStatements, ast));
							newStatement.statements().add(newSwitchCase);
							break;
						}
					}
					newStatement.statements().add(st);
				}
			}

			return newStatement;
			
		}

		case ASTNode.SYNCHRONIZED_STATEMENT: {
			SynchronizedStatement synchronizedStatement = (SynchronizedStatement) root;

			Statement body = copyStatements(synchronizedStatement.getBody(), includeStatements, ast);
			if (!containsRoot) {
				if (body != null) {
					return body;
				}else{
					return null;
				}
			}

			SynchronizedStatement newStatement = ast.newSynchronizedStatement();
			newStatement.setBody( body == null ? ast.newBlock() : (Block) body );
			newStatement.setExpression((Expression) copyAnothersNodes(synchronizedStatement.getExpression(), includeStatements, ast));

			return newStatement;
		}

		case ASTNode.THROW_STATEMENT: {

			if (!containsRoot) {
				return null;
			}

			ThrowStatement throwStatement = (ThrowStatement) root;

			ThrowStatement newStatement = ast.newThrowStatement();
			newStatement.setExpression((Expression) copyAnothersNodes(throwStatement.getExpression(), includeStatements, ast));

			return newStatement;
		}

		case ASTNode.TRY_STATEMENT: {
			TryStatement tryStatement = (TryStatement) root;


			Statement body = copyStatements(tryStatement.getBody(), includeStatements, ast);

			boolean allAreEmpty = body == null;
			List<CatchClause> clauses = new ArrayList<CatchClause>();
			for (Object o : tryStatement.catchClauses()) {
				ASTNode clause = copyAnothersNodes( (CatchClause) o, includeStatements, ast);
				if (clause != null) {
					clauses.add((CatchClause) clause);
					if (!((CatchClause) clause).getBody().statements().isEmpty()) {
						allAreEmpty = false;
					}
				}
			}
			Statement finallyBlock = copyStatements(tryStatement.getFinally(), includeStatements, ast);
			if (finallyBlock != null) {
				allAreEmpty = false;
			}

			if (allAreEmpty) {
				return null;
			}

			TryStatement newStatement = ast.newTryStatement();
			newStatement.setBody(body == null ? ast.newBlock() : (Block) body);
			newStatement.catchClauses().addAll(clauses);

			if (newStatement.catchClauses().isEmpty()) {
				newStatement.setFinally(finallyBlock == null ? ast.newBlock() : (Block) finallyBlock);
			}else{
				newStatement.setFinally(finallyBlock == null ? null : (Block) finallyBlock);
			}

			return newStatement;
		}

		case ASTNode.TYPE_DECLARATION_STATEMENT: {
			TypeDeclarationStatement typeDeclarationStatement = (TypeDeclarationStatement) root;
			// return ast.newTypeDeclarationStatement((TypeDeclaration) copyAnothersNodes(typeDeclarationStatement.getDeclaration(), statements, ast));
			//TODO Nao necessario ainda
			break;
		}

		case ASTNode.VARIABLE_DECLARATION_STATEMENT: {

			if (!containsRoot) {
				return null;
			}

			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) root;

			LinkedList<VariableDeclarationFragment> newFragments = new LinkedList<VariableDeclarationFragment>();
			for (Object o : variableDeclarationStatement.fragments()) {
				newFragments.add((VariableDeclarationFragment) copyAnothersNodes((ASTNode) o, includeStatements, ast));
			}

			VariableDeclarationStatement newStatement = ast.newVariableDeclarationStatement(newFragments.removeFirst());
			for (Object o : variableDeclarationStatement.modifiers()) {
				try {
					newStatement.modifiers().add(copyAnothersNodes((ASTNode) o, includeStatements, ast));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			newStatement.setType((Type) copyAnothersNodes(variableDeclarationStatement.getType(), includeStatements, ast));
			newStatement.fragments().addAll(newFragments);

			return newStatement;
		}

		case ASTNode.WHILE_STATEMENT: {
			WhileStatement whileStatement = (WhileStatement) root;

			Statement body = copyStatements(whileStatement.getBody(), includeStatements, ast);
			if (!containsRoot) {
				if (body != null) {
					return body;
				}else{
					return null;
				}
			}

			WhileStatement newStatement = ast.newWhileStatement();
			newStatement.setBody( body == null ? ast.newBlock() : (Statement) body );
			newStatement.setExpression((Expression) copyAnothersNodes(whileStatement.getExpression(), includeStatements, ast));

			return newStatement;
		}

		/**
		 *  Fim org.eclipse.jdt.core.dom.Statement
		 */

		default: {
			System.err.println("Type not identified: "+ASTNode.nodeClassForType(root.getNodeType()));
			return null;
		}	

		}

		return null;

	}

	@SuppressWarnings("unchecked")
	public static final ASTNode copyAnothersNodes(final ASTNode root, final Collection<Statement> includeStatements, final AST ast){

		if (root == null) {
			return null;
		}else if (ast == null){
			throw new NullPointerException("AST must be not null");
		}

		switch(root.getNodeType()){
		
		case ASTNode.ENUM_DECLARATION: {
			EnumDeclaration enumDeclaration = (EnumDeclaration) root;
			
			EnumDeclaration newEnum = ast.newEnumDeclaration();
			for (Object o : enumDeclaration.bodyDeclarations()) {
				Object newBodyDeclaration = copyAnothersNodes((BodyDeclaration)o, includeStatements, ast);
				if (newBodyDeclaration != null) {
					newEnum.bodyDeclarations().add(newBodyDeclaration);
				}
			}
			
			return newEnum;
		}
		
		case ASTNode.TYPE_DECLARATION: {
			TypeDeclaration typeDeclaration = (TypeDeclaration) root;
			
			TypeDeclaration td = ast.newTypeDeclaration();
			td.setInterface(typeDeclaration.isInterface());
			td.setSuperclassType((Type) copyAnothersNodes(typeDeclaration.getSuperclassType(), includeStatements, ast));
			for(Object o : typeDeclaration.superInterfaceTypes()){
				td.superInterfaceTypes().add(copyAnothersNodes((Type)o, includeStatements, ast));
			}
			for (Object o : typeDeclaration.typeParameters()) {
				td.superInterfaceTypes().add(copyAnothersNodes((TypeParameter)o, includeStatements, ast));
			}
			for (Object o : typeDeclaration.bodyDeclarations()) {
				Object newBodyDeclaration = copyAnothersNodes((BodyDeclaration)o, includeStatements, ast);
				if (newBodyDeclaration != null) {
					td.bodyDeclarations().add(newBodyDeclaration);
				}
			}
			
			return td;
		}
		
		case ASTNode.FIELD_DECLARATION: {
			FieldDeclaration fieldDeclaration = (FieldDeclaration) root;
			
			LinkedList<VariableDeclarationFragment> list = new LinkedList<VariableDeclarationFragment>();
			for (Object o : fieldDeclaration.fragments()) {
				list.add((VariableDeclarationFragment) copyAnothersNodes((VariableDeclarationFragment)o, includeStatements, ast));
			}
			
			FieldDeclaration fd = ast.newFieldDeclaration(list.removeFirst());
			while(!list.isEmpty()){
				fd.fragments().add(list.removeFirst());
			}
			fd.setType((Type) copyAnothersNodes(fieldDeclaration.getType(), includeStatements, ast));
			
			return fd;
		}
		
		case ASTNode.INITIALIZER: {
			Initializer initializer = (Initializer) root;
			
			Initializer init = ast.newInitializer();
			Object newBlock = copyStatements(initializer.getBody(), includeStatements, ast);
			if (newBlock != null){
				init.setBody((Block) newBlock);
			}else{
				init.setBody(ast.newBlock());
			}
			
			return init;
			
		}
		
		case ASTNode.METHOD_DECLARATION: {
			MethodDeclaration methodDeclaration = (MethodDeclaration) root;

			MethodDeclaration md = ast.newMethodDeclaration();
			Statement body = copyStatements(methodDeclaration.getBody(), includeStatements, ast);
			md.setBody(body == null ? ast.newBlock() : (Block) body);
			md.setConstructor(methodDeclaration.isConstructor());
			md.setExtraDimensions(methodDeclaration.getExtraDimensions());
			md.setName((SimpleName) copyAnothersNodes(methodDeclaration.getName(), includeStatements, ast));
			md.setReturnType2((Type) copyAnothersNodes(methodDeclaration.getReturnType2(), includeStatements, ast));

			for (int i = 0; i < methodDeclaration.parameters().size(); i++) { 
				md.parameters().add(i, copyAnothersNodes((ASTNode) methodDeclaration.parameters().get(i), includeStatements, ast));
			}

			for (int i = 0; i < methodDeclaration.thrownExceptions().size(); i++) { 
				md.thrownExceptions().add(i, copyAnothersNodes((ASTNode) methodDeclaration.thrownExceptions().get(i), includeStatements, ast));
			}

			return md;
		}

		case ASTNode.MEMBER_VALUE_PAIR: {
			MemberValuePair memberValuePair = (MemberValuePair) root;

			MemberValuePair newMember = ast.newMemberValuePair();
			newMember.setName((SimpleName) copyAnothersNodes(memberValuePair.getName(), includeStatements, ast));
			newMember.setValue((Expression) copyAnothersNodes(memberValuePair.getValue(), includeStatements, ast));

			return newMember;
		}

		/**
		 *  Tratando tipo org.eclipse.jdt.core.dom.IExtendedModifier
		 */

		case ASTNode.MODIFIER: {
			Modifier modifier = (Modifier) root;
			return ast.newModifier(modifier.getKeyword());
		}

		case ASTNode.MARKER_ANNOTATION: {
			MarkerAnnotation markerAnnotation = (MarkerAnnotation) root;

			MarkerAnnotation newAnnotation = ast.newMarkerAnnotation();
			newAnnotation.setTypeName((Name) copyAnothersNodes(markerAnnotation.getTypeName(), includeStatements, ast));

			return newAnnotation;
		}

		case ASTNode.NORMAL_ANNOTATION: {
			NormalAnnotation normalAnnotation = (NormalAnnotation) root;

			NormalAnnotation newAnnotation = ast.newNormalAnnotation();
			newAnnotation.setTypeName((Name) copyAnothersNodes(normalAnnotation.getTypeName(), includeStatements, ast));
			for (Object o : normalAnnotation.values()) {
				newAnnotation.values().add(copyAnothersNodes((ASTNode) o, includeStatements, ast));
			}

			return newAnnotation;
		}

		case ASTNode.SINGLE_MEMBER_ANNOTATION: {
			SingleMemberAnnotation singleMemberAnnotation = (SingleMemberAnnotation) root;

			SingleMemberAnnotation newAnnotation = ast.newSingleMemberAnnotation();
			newAnnotation.setTypeName((Name) copyAnothersNodes(singleMemberAnnotation.getTypeName(), includeStatements, ast));
			newAnnotation.setValue((Expression) copyAnothersNodes(singleMemberAnnotation.getValue(), includeStatements, ast));

			return newAnnotation;
		}

		/**
		 *  Fim org.eclipse.jdt.core.dom.IExtendedModifier
		 */

		case ASTNode.CATCH_CLAUSE: {
			CatchClause catchClause = (CatchClause) root;

			CatchClause newClause = ast.newCatchClause();
			Statement newBlock = copyStatements(catchClause.getBody(), includeStatements, ast);
			newClause.setBody(newBlock == null ? ast.newBlock() : (Block) newBlock);
			newClause.setException((SingleVariableDeclaration) copyAnothersNodes(catchClause.getException(), includeStatements, ast));

			return newClause;
		}

		/**
		 * Tratamento dos tipos de org.eclipse.jdt.core.dom.VariableDeclaration
		 */

		case ASTNode.SINGLE_VARIABLE_DECLARATION: {
			SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) root;

			SingleVariableDeclaration newVD = ast.newSingleVariableDeclaration();
			newVD.setExtraDimensions(singleVariableDeclaration.getExtraDimensions());
			newVD.setInitializer((Expression) copyAnothersNodes(singleVariableDeclaration.getInitializer(), includeStatements, ast));
			for (Object o : singleVariableDeclaration.modifiers()) {
				newVD.modifiers().add(copyAnothersNodes((ASTNode) o, includeStatements, ast));
			}

			newVD.setName((SimpleName) copyAnothersNodes(singleVariableDeclaration.getName(), includeStatements, ast));
			newVD.setType((Type) copyAnothersNodes(singleVariableDeclaration.getType(), includeStatements, ast));
			newVD.setVarargs(singleVariableDeclaration.isVarargs());

			return newVD;
		}

		case ASTNode.VARIABLE_DECLARATION_FRAGMENT: {
			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) root;

			VariableDeclarationFragment newExpression = ast.newVariableDeclarationFragment();
			newExpression.setExtraDimensions(variableDeclarationFragment.getExtraDimensions());
			newExpression.setInitializer((Expression) copyAnothersNodes(variableDeclarationFragment.getInitializer(), includeStatements, ast));
			newExpression.setName((SimpleName) copyAnothersNodes(variableDeclarationFragment.getName(), includeStatements, ast));

			return newExpression;
		}

		/**
		 * Fim org.eclipse.jdt.core.dom.VariableDeclaration
		 */

		/**
		 * Tratamento dos tipos de org.eclipse.jdt.core.dom.Expression
		 */

		case ASTNode.ARRAY_ACCESS: {
			ArrayAccess arrayAccess = (ArrayAccess) root;

			ArrayAccess newExpression = ast.newArrayAccess();
			newExpression.setArray((Expression) copyAnothersNodes(arrayAccess.getArray(), includeStatements, ast));
			newExpression.setIndex((Expression) copyAnothersNodes(arrayAccess.getIndex(), includeStatements, ast));

			return newExpression;
		}

		case ASTNode.ARRAY_CREATION: {
			ArrayCreation arrayCreation = (ArrayCreation) root;

			ArrayCreation newExpression = ast.newArrayCreation();
			newExpression.setInitializer((ArrayInitializer) copyAnothersNodes(arrayCreation.getInitializer(), includeStatements, ast));
			newExpression.setType((ArrayType) copyAnothersNodes(arrayCreation.getType(), includeStatements, ast));
			for (Object o : arrayCreation.dimensions()) {
				newExpression.dimensions().add(copyAnothersNodes( (Expression) o, includeStatements, ast));
			}

			return newExpression;
		}

		case ASTNode.ARRAY_INITIALIZER: {
			ArrayInitializer arrayInitializer = (ArrayInitializer) root;

			ArrayInitializer newExpression = ast.newArrayInitializer();
			for (Object o : arrayInitializer.expressions()) {
				newExpression.expressions().add(copyAnothersNodes( (Expression) o, includeStatements, ast));
			}

			return newExpression;
		}

		case ASTNode.ASSIGNMENT: {
			Assignment assignment = (Assignment) root;

			Assignment newExpression = ast.newAssignment();
			newExpression.setLeftHandSide((Expression) copyAnothersNodes(assignment.getLeftHandSide(), includeStatements, ast));
			newExpression.setOperator(assignment.getOperator());
			newExpression.setRightHandSide((Expression) copyAnothersNodes(assignment.getRightHandSide(), includeStatements, ast));

			return newExpression;
		}

		case ASTNode.BOOLEAN_LITERAL: {
			BooleanLiteral booleanLiteral = (BooleanLiteral) root;
			return ast.newBooleanLiteral(booleanLiteral.booleanValue());
		}

		case ASTNode.CAST_EXPRESSION: {
			CastExpression castExpression = (CastExpression) root;

			CastExpression newExpession = ast.newCastExpression();
			newExpession.setExpression((Expression) copyAnothersNodes(castExpression.getExpression(), includeStatements, ast));
			newExpession.setType((Type) copyAnothersNodes(castExpression.getType(), includeStatements, ast));

			return newExpession;
		}

		case ASTNode.CHARACTER_LITERAL : {
			CharacterLiteral characterLiteral = (CharacterLiteral) root;

			CharacterLiteral newExpression = ast.newCharacterLiteral();
			newExpression.setCharValue(characterLiteral.charValue());
			newExpression.setEscapedValue(characterLiteral.getEscapedValue());

			return newExpression;
		}

		case ASTNode.CLASS_INSTANCE_CREATION: {
			ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) root;

			ClassInstanceCreation newExpression = ast.newClassInstanceCreation();
			
			AnonymousClassDeclaration acd = ast.newAnonymousClassDeclaration();
			if (classInstanceCreation.getAnonymousClassDeclaration() != null) {
				for (Object o : classInstanceCreation.getAnonymousClassDeclaration().bodyDeclarations()){
					Object bd = copyAnothersNodes((BodyDeclaration) o, includeStatements, ast);
					if (bd != null) {
//						if (bd instanceof AbstractTypeDeclaration) {
//							//TODO Evaluate this case
//						}else if (bd instanceof FieldDeclaration) {
//							//TODO Evaluate this case
//						}else if (bd instanceof Initializer) {
//							Initializer init = (Initializer) bd;
//							if (!init.getBody().statements().isEmpty()) {
//								acd.bodyDeclarations().add(init);
//							}
//						}else if (bd instanceof MethodDeclaration) {
//							MethodDeclaration md = (MethodDeclaration) bd;
//							if (!md.getBody().statements().isEmpty()) {
//								acd.bodyDeclarations().add(md);
//							}
//						}else{
//							//Another are useless
//						}
						acd.bodyDeclarations().add(bd);
					}
				}
				if (!acd.bodyDeclarations().isEmpty()) {
					newExpression.setAnonymousClassDeclaration(acd);
				}
			}
			
			newExpression.setExpression((Expression) copyAnothersNodes(classInstanceCreation.getExpression(), includeStatements, ast));
			newExpression.setType((Type) copyAnothersNodes(classInstanceCreation.getType(), includeStatements, ast));
			for (Object o : classInstanceCreation.arguments()) {
				newExpression.arguments().add(copyAnothersNodes( (Expression) o, includeStatements, ast));
			}
			for (Object o : classInstanceCreation.typeArguments()) {
				newExpression.typeArguments().add(copyAnothersNodes( (Expression) o, includeStatements, ast));
			}

			return newExpression;
		}

		case ASTNode.CONDITIONAL_EXPRESSION: {
			ConditionalExpression conditionalExpression = (ConditionalExpression) root;

			ConditionalExpression newExpression = ast.newConditionalExpression();
			newExpression.setElseExpression((Expression) copyAnothersNodes(conditionalExpression.getElseExpression(), includeStatements, ast));
			newExpression.setExpression((Expression) copyAnothersNodes(conditionalExpression.getExpression(), includeStatements, ast));
			newExpression.setThenExpression((Expression) copyAnothersNodes(conditionalExpression.getThenExpression(), includeStatements, ast));

			return newExpression;
		}

		case ASTNode.FIELD_ACCESS: {
			FieldAccess fieldAccess = (FieldAccess) root;

			FieldAccess newExpression = ast.newFieldAccess();
			newExpression.setExpression((Expression) copyAnothersNodes(fieldAccess.getExpression(), includeStatements, ast));
			newExpression.setName((SimpleName) copyAnothersNodes(fieldAccess.getName(), includeStatements, ast));

			return newExpression;
		}

		case ASTNode.INFIX_EXPRESSION: {
			InfixExpression infixExpression = (InfixExpression) root;

			InfixExpression newExpression = ast.newInfixExpression();
			newExpression.setLeftOperand((Expression) copyAnothersNodes(infixExpression.getLeftOperand(), includeStatements, ast));
			newExpression.setOperator(infixExpression.getOperator());
			newExpression.setRightOperand((Expression) copyAnothersNodes(infixExpression.getRightOperand(), includeStatements, ast));
			for (Object o : infixExpression.extendedOperands()) {
				newExpression.extendedOperands().add(copyAnothersNodes((Expression) o, includeStatements, ast));
			}

			return newExpression;
		}

		case ASTNode.INSTANCEOF_EXPRESSION: {
			InstanceofExpression instanceofExpression = (InstanceofExpression) root;

			InstanceofExpression newExpression = ast.newInstanceofExpression();
			newExpression.setLeftOperand((Expression) copyAnothersNodes(instanceofExpression.getLeftOperand(), includeStatements, ast));
			newExpression.setRightOperand((Type) copyAnothersNodes(instanceofExpression.getRightOperand(), includeStatements, ast));

			return newExpression;
		}

		case ASTNode.METHOD_INVOCATION: {
			MethodInvocation methodInvocation = (MethodInvocation) root;

			MethodInvocation newExpression = ast.newMethodInvocation();
			newExpression.setExpression((Expression) copyAnothersNodes(methodInvocation.getExpression(), includeStatements, ast));
			newExpression.setName((SimpleName) copyAnothersNodes(methodInvocation.getName(), includeStatements, ast));
			for (Object o : methodInvocation.arguments()) {
				newExpression.arguments().add(copyAnothersNodes( (Expression) o, includeStatements, ast));
			}
			for (Object o : methodInvocation.typeArguments()) {
				newExpression.typeArguments().add(copyAnothersNodes( (Type) o, includeStatements, ast));
			}

			return newExpression;
		}

		case ASTNode.NULL_LITERAL: {
			return ast.newNullLiteral();
		}

		case ASTNode.NUMBER_LITERAL: {
			NumberLiteral numberLiteral = (NumberLiteral) root;
			return ast.newNumberLiteral(numberLiteral.getToken());
		}

		case ASTNode.PARENTHESIZED_EXPRESSION: {
			ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) root;

			ParenthesizedExpression newExpression = ast.newParenthesizedExpression();
			newExpression.setExpression((Expression) copyAnothersNodes(parenthesizedExpression.getExpression(), includeStatements, ast));		

			return newExpression;
		}

		case ASTNode.POSTFIX_EXPRESSION: {
			PostfixExpression postfixExpression = (PostfixExpression) root;

			PostfixExpression newExpression = ast.newPostfixExpression();
			newExpression.setOperator(postfixExpression.getOperator());
			newExpression.setOperand((Expression) copyAnothersNodes(postfixExpression.getOperand(), includeStatements, ast));

			return newExpression;
		}

		case ASTNode.PREFIX_EXPRESSION: {
			PrefixExpression prefixExpression = (PrefixExpression) root;

			PrefixExpression newExpression = ast.newPrefixExpression();
			newExpression.setOperator(prefixExpression.getOperator());
			newExpression.setOperand((Expression) copyAnothersNodes(prefixExpression.getOperand(), includeStatements, ast));

			return newExpression;
		}

		case ASTNode.STRING_LITERAL: {
			StringLiteral stringLiteral = (StringLiteral) root;

			StringLiteral newExpression = ast.newStringLiteral();
			newExpression.setEscapedValue(stringLiteral.getEscapedValue());
			newExpression.setLiteralValue(stringLiteral.getLiteralValue());

			return newExpression;
		}

		case ASTNode.SUPER_FIELD_ACCESS: {
			SuperFieldAccess superFieldAccess = (SuperFieldAccess) root;

			SuperFieldAccess newExpression = ast.newSuperFieldAccess();
			newExpression.setName((SimpleName) copyAnothersNodes(superFieldAccess.getName(), includeStatements, ast));
			newExpression.setQualifier((Name) copyAnothersNodes(superFieldAccess.getQualifier(), includeStatements, ast));

			return newExpression;
		}

		case ASTNode.SUPER_METHOD_INVOCATION: {
			SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation) root;

			SuperMethodInvocation newExpression = ast.newSuperMethodInvocation();
			newExpression.setName((SimpleName) copyAnothersNodes(superMethodInvocation.getName(), includeStatements, ast));
			newExpression.setQualifier((Name) copyAnothersNodes(superMethodInvocation.getQualifier(), includeStatements, ast));
			for (Object o : superMethodInvocation.arguments()) {
				newExpression.arguments().add(copyAnothersNodes((Expression)o, includeStatements, ast));
			}
			for (Object o : superMethodInvocation.typeArguments()) {
				newExpression.typeArguments().add(copyAnothersNodes((Type)o, includeStatements, ast));
			}

			return newExpression;
		}

		case ASTNode.THIS_EXPRESSION: {
			ThisExpression thisExpression = (ThisExpression) root;

			ThisExpression newExpression = ast.newThisExpression();
			newExpression.setQualifier((Name) copyAnothersNodes(thisExpression.getQualifier(), includeStatements, ast));

			return newExpression;
		}

		case ASTNode.TYPE_LITERAL: {
			TypeLiteral typeLiteral = (TypeLiteral) root;

			TypeLiteral newExpression = ast.newTypeLiteral();
			newExpression.setType((Type) copyAnothersNodes(typeLiteral.getType(), includeStatements, ast));

			return newExpression;
		}

		case ASTNode.VARIABLE_DECLARATION_EXPRESSION: {
			VariableDeclarationExpression variableDeclarationExpression = (VariableDeclarationExpression) root;

			LinkedList<VariableDeclarationFragment> fragments = new LinkedList<VariableDeclarationFragment>();
			for (Object o : variableDeclarationExpression.fragments()) {
				fragments.add( (VariableDeclarationFragment) copyAnothersNodes( (VariableDeclarationFragment) o, includeStatements, ast));
			}

			VariableDeclarationExpression newExpression = ast.newVariableDeclarationExpression(fragments.removeFirst());
			newExpression.fragments().addAll(fragments);
			for (Object o : variableDeclarationExpression.modifiers()) {
				newExpression.modifiers().add(copyAnothersNodes((ASTNode) o, includeStatements, ast));
			}

			newExpression.setType((Type) copyAnothersNodes(variableDeclarationExpression.getType(), includeStatements, ast));

			return newExpression;
		}

		/**
		 *  Fim org.eclipse.jdt.core.dom.Expression
		 */

		case ASTNode.ANONYMOUS_CLASS_DECLARATION: {
			//TODO Criar
			break;
		}


		/**
		 *  Tratamento dos tipos de org.eclipse.jdt.core.dom.Type
		 */

		case ASTNode.ARRAY_TYPE:{
			ArrayType arrayType = (ArrayType) root;
			return ast.newArrayType((Type) copyAnothersNodes(arrayType.getElementType(), includeStatements, ast));
		}

		case ASTNode.PARAMETERIZED_TYPE: {
			ParameterizedType parameterizedType = (ParameterizedType) root;
			ParameterizedType newParameterizedType = ast.newParameterizedType((Type) copyAnothersNodes(parameterizedType.getType(), includeStatements, ast));
			for (Object p : parameterizedType.typeArguments()) {
				newParameterizedType.typeArguments().add(copyAnothersNodes( (Type) p, includeStatements, ast));
			}
			return newParameterizedType;
		}

		case ASTNode.PRIMITIVE_TYPE: {
			PrimitiveType primitiveType = (PrimitiveType) root;
			return ast.newPrimitiveType(primitiveType.getPrimitiveTypeCode());
		}

		case ASTNode.QUALIFIED_TYPE: {
			QualifiedType qualifiedType = (QualifiedType) root;
			return ast.newQualifiedType( (Type) copyAnothersNodes(qualifiedType.getQualifier(), includeStatements, ast) , (SimpleName) copyAnothersNodes(qualifiedType.getName(), includeStatements, ast));
		}

		case ASTNode.SIMPLE_TYPE: {
			SimpleType simpleType = (SimpleType) root;
			return ast.newSimpleType((Name) copyAnothersNodes(simpleType.getName(), includeStatements, ast));
		}

		case ASTNode.WILDCARD_TYPE: {
			WildcardType wildcardType = (WildcardType) root;
			WildcardType newType = ast.newWildcardType();
			newType.setBound((Type) copyAnothersNodes(wildcardType.getBound(), includeStatements, ast), wildcardType.isUpperBound());
			return newType;
		}

		/**
		 *  Tratamento dos tipos de org.eclipse.jdt.core.dom.Name
		 */

		case ASTNode.QUALIFIED_NAME: {
			QualifiedName qualifiedName = (QualifiedName) root;
			Name name = ast.newName(qualifiedName.getFullyQualifiedName());

//			return ast.newQualifiedName(name, (SimpleName) copyAnothersNodes(qualifiedName.getName(), statements, ast));
			return name;

		}

		case ASTNode.SIMPLE_NAME: {
			SimpleName simpleName = (SimpleName) root;
			return ast.newSimpleName(simpleName.getIdentifier());
		}

		/**
		 *  End org.eclipse.jdt.core.dom.Name
		 */


		default: {
			LOGGER.error(String.format("Type not identified: %s",ASTNode.nodeClassForType(root.getNodeType())));
			return null;
		}	

		}

		return null;

	}

	public static void removeEmptyBlocks(Block block){

		class InternalVisitor extends ASTVisitor {
			@Override
			public void endVisit(Block node) {
				if (node.statements().isEmpty()) {
					return;
				}

//				boolean areEmpty = true;
//				for (int i = 0; i < node.statements().size(); i++) {
//					Statement st = (Statement) node.statements().get(i);
//					if (!(st instanceof Block)) {
//						areEmpty = false;
//					}
//				}

//				if (areEmpty) {
//					List<Block> blocks = new ArrayList<Block>(node.statements());
					List<Block> blocks = new ArrayList<Block>();
					for (Object o : node.statements()) {
						if (o instanceof Block) {
							blocks.add((Block) o);
						}
					}
					node.statements().removeAll(blocks);

					for (int i = 0; i < blocks.size(); i++) {
						while (!blocks.get(i).statements().isEmpty()) {
							Statement statement = (Statement) blocks.get(i).statements().get(0);
							statement.delete();
							node.statements().add(statement);
						}
					}
//				}
				super.endVisit(node);
			}
		}

		block.accept(new InternalVisitor());

	}

	public static Set<IVariableBinding> collectVariables(Expression node) {

		if (node == null) {
			throw new IllegalArgumentException(new NullPointerException());
		}

		Set<IVariableBinding> variableBindings = new HashSet<IVariableBinding>();

		switch (node.getNodeType()) {

		/*
		 * Type org.eclipse.jdt.core.dom.Annotation is unecessary perform 
		 */

		case ASTNode.ARRAY_ACCESS: {
			ArrayAccess arrayAccess = (ArrayAccess) node;

			variableBindings.addAll(collectVariables(arrayAccess.getArray()));
			variableBindings.addAll(collectVariables(arrayAccess.getIndex()));

			return variableBindings;
		}

		case ASTNode.ARRAY_CREATION: {
			ArrayCreation arrayCreation = (ArrayCreation) node;
			for (Object o : arrayCreation.dimensions()) {
				variableBindings.addAll(collectVariables((Expression) o));
			}

			if (arrayCreation.getInitializer() != null) {
				variableBindings.addAll(collectVariables(arrayCreation.getInitializer()));
			}

			return variableBindings;
		}

		case ASTNode.ARRAY_INITIALIZER: {
			ArrayInitializer arrayInitializer = (ArrayInitializer) node;
			for (Object o : arrayInitializer.expressions()) {
				variableBindings.addAll(collectVariables((Expression) o));
			}

			return variableBindings;
		}

		case ASTNode.ASSIGNMENT: {
			Assignment assignment = (Assignment) node;

			variableBindings.addAll(collectVariables(assignment.getLeftHandSide()));
			variableBindings.addAll(collectVariables(assignment.getRightHandSide()));

			return variableBindings;
		}

		case ASTNode.BOOLEAN_LITERAL: {
			// This expression do not have variables
			return variableBindings;
		}

		case ASTNode.CAST_EXPRESSION: {
			CastExpression castExpression = (CastExpression) node;

			variableBindings.addAll(collectVariables(castExpression.getExpression()));

			return variableBindings;
		}

		case ASTNode.CHARACTER_LITERAL: {
			// This expression do not have variables
			return variableBindings;
		}

		case ASTNode.CLASS_INSTANCE_CREATION: {
			ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) node;
			for (Object o : classInstanceCreation.arguments()) {
				variableBindings.addAll(collectVariables((Expression) o));
			}

			if (classInstanceCreation.getExpression() != null) {
				variableBindings.addAll(collectVariables(classInstanceCreation.getExpression()));
			}

			return variableBindings;
		}

		case ASTNode.CONDITIONAL_EXPRESSION: {
			ConditionalExpression conditionalExpression = (ConditionalExpression) node;

			variableBindings.addAll(collectVariables(conditionalExpression.getElseExpression()));
			variableBindings.addAll(collectVariables(conditionalExpression.getThenExpression()));
			variableBindings.addAll(collectVariables(conditionalExpression.getExpression()));

			return variableBindings;
		}

		case ASTNode.FIELD_ACCESS: {
			FieldAccess fieldAccess = (FieldAccess) node;

			variableBindings.addAll(collectVariables(fieldAccess.getName()));
			variableBindings.addAll(collectVariables(fieldAccess.getExpression()));

			return variableBindings;
		}

		case ASTNode.INFIX_EXPRESSION: {
			InfixExpression infixExpression = (InfixExpression) node;

			variableBindings.addAll(collectVariables(infixExpression.getLeftOperand()));

			if (!infixExpression.hasExtendedOperands()) {
				variableBindings.addAll(collectVariables(infixExpression.getRightOperand()));
			} else {
				for (Object o : infixExpression.extendedOperands()) {
					variableBindings.addAll(collectVariables((Expression) o));
				}
			}

			return variableBindings;
		}

		case ASTNode.INSTANCEOF_EXPRESSION: {
			InstanceofExpression instanceofExpression = (InstanceofExpression) node;

			variableBindings.addAll(collectVariables(instanceofExpression.getLeftOperand()));

			return variableBindings;
		}

		case ASTNode.METHOD_INVOCATION: {
			MethodInvocation methodInvocation = (MethodInvocation) node;
			for (Object o : methodInvocation.arguments()) {
				variableBindings.addAll(collectVariables((Expression) o));
			}

			if (methodInvocation.getExpression() != null) {
				variableBindings.addAll(collectVariables(methodInvocation.getExpression()));
			}

			return variableBindings;
		}

		/*
		 *  Handler of subtypes of org.eclipse.jdt.core.dom.Name
		 */

		case ASTNode.SIMPLE_NAME: {
			SimpleName name = (SimpleName) node;
			IBinding bi = name.resolveBinding();
			if (bi != null) {
				if (bi instanceof IVariableBinding) {
					variableBindings.add((IVariableBinding) bi);
				}
			}

			return variableBindings;
		}

		case ASTNode.QUALIFIED_NAME: {
			QualifiedName qualifiedName = (QualifiedName) node;

			variableBindings.addAll(collectVariables(qualifiedName.getName()));
			variableBindings.addAll(collectVariables(qualifiedName.getQualifier()));

			return variableBindings;
		}

		/*
		 *  End of handler of subtypes of org.eclipse.jdt.core.dom.Name
		 */

		case ASTNode.NULL_LITERAL: {
			// This expression do not have variables
			return variableBindings;
		}

		case ASTNode.NUMBER_LITERAL: {
			// This expression do not have variables
			return variableBindings;
		}

		case ASTNode.PARENTHESIZED_EXPRESSION: {
			ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) node;

			variableBindings.addAll(collectVariables(parenthesizedExpression.getExpression()));

			return variableBindings;
		}

		case ASTNode.POSTFIX_EXPRESSION: {
			PostfixExpression postfixExpression = (PostfixExpression) node;

			variableBindings.addAll(collectVariables(postfixExpression.getOperand()));

			return variableBindings;
		}

		case ASTNode.PREFIX_EXPRESSION: {
			PrefixExpression prefixExpression = (PrefixExpression) node;

			variableBindings.addAll(collectVariables(prefixExpression.getOperand()));

			return variableBindings;
		}

		case ASTNode.STRING_LITERAL: {
			// This expression do not have variables
			return variableBindings;
		}

		case ASTNode.SUPER_FIELD_ACCESS: {
			SuperFieldAccess superFieldAccess = (SuperFieldAccess) node;

			variableBindings.addAll(collectVariables(superFieldAccess.getName()));
			if (superFieldAccess.getQualifier() != null) {
				variableBindings.addAll(collectVariables(superFieldAccess.getQualifier()));
			}

			return variableBindings;
		}

		case ASTNode.SUPER_METHOD_INVOCATION: {
			SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation) node;
			for (Object o : superMethodInvocation.arguments()) {
				variableBindings.addAll(collectVariables((Expression) o));
			}

			if (superMethodInvocation.getQualifier() != null) {
				variableBindings.addAll(collectVariables(superMethodInvocation.getQualifier()));
			}

			return variableBindings;
		}

		case ASTNode.THIS_EXPRESSION: {
			ThisExpression thisExpression = (ThisExpression) node;

			if (thisExpression.getQualifier() != null) {
				variableBindings.addAll(collectVariables(thisExpression.getQualifier()));
			}

			return variableBindings;
		}

		case ASTNode.TYPE_LITERAL: {
			// This expression do not have variables
			return variableBindings;
		}

		case ASTNode.VARIABLE_DECLARATION_EXPRESSION: {
			VariableDeclarationExpression variableDeclarationExpression = (VariableDeclarationExpression) node;
			for (Object o : variableDeclarationExpression.fragments()) {
				VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) o;
				variableBindings.addAll(collectVariables(variableDeclarationFragment.getName()));
				if (variableDeclarationFragment.getInitializer() != null) {
					variableBindings.addAll(collectVariables(variableDeclarationFragment.getInitializer()));
				}
			}

			return variableBindings;
		}

		default:
			throw new IllegalArgumentException("Node type cannot be handled (Not yet implemented)");

		}

	}
	
	public static Set<IVariableBinding> collectVariables2 (ASTNode node) {

		if (node == null) {
			throw new IllegalArgumentException(new NullPointerException());
		}

		Set<IVariableBinding> variableBindings = new HashSet<IVariableBinding>();

		switch (node.getNodeType()) {

		/*
		 * Type org.eclipse.jdt.core.dom.Annotation is unnecessary perform 
		 */

		case ASTNode.ARRAY_ACCESS: {
			ArrayAccess arrayAccess = (ArrayAccess) node;

			variableBindings.addAll(collectVariables2(arrayAccess.getArray()));
			variableBindings.addAll(collectVariables2(arrayAccess.getIndex()));

			return variableBindings;
		}

		case ASTNode.ARRAY_CREATION: {
			ArrayCreation arrayCreation = (ArrayCreation) node;
			for (Object o : arrayCreation.dimensions()) {
				variableBindings.addAll(collectVariables2((Expression) o));
			}

			if (arrayCreation.getInitializer() != null) {
				variableBindings.addAll(collectVariables2(arrayCreation.getInitializer()));
			}

			return variableBindings;
		}

		case ASTNode.ARRAY_INITIALIZER: {
			ArrayInitializer arrayInitializer = (ArrayInitializer) node;
			for (Object o : arrayInitializer.expressions()) {
				variableBindings.addAll(collectVariables2((Expression) o));
			}

			return variableBindings;
		}

		case ASTNode.ASSIGNMENT: {
			Assignment assignment = (Assignment) node;

			variableBindings.addAll(collectVariables2(assignment.getLeftHandSide()));
			variableBindings.addAll(collectVariables2(assignment.getRightHandSide()));

			return variableBindings;
		}

		case ASTNode.BOOLEAN_LITERAL: {
			// This expression do not have variables
			return variableBindings;
		}

		case ASTNode.CAST_EXPRESSION: {
			CastExpression castExpression = (CastExpression) node;

			variableBindings.addAll(collectVariables2(castExpression.getExpression()));

			return variableBindings;
		}

		case ASTNode.CHARACTER_LITERAL: {
			// This expression do not have variables
			return variableBindings;
		}

		case ASTNode.CLASS_INSTANCE_CREATION: {
			ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) node;
			for (Object o : classInstanceCreation.arguments()) {
				variableBindings.addAll(collectVariables2((Expression) o));
			}

			if (classInstanceCreation.getExpression() != null) {
				variableBindings.addAll(collectVariables2(classInstanceCreation.getExpression()));
			}

			return variableBindings;
		}

		case ASTNode.CONDITIONAL_EXPRESSION: {
			ConditionalExpression conditionalExpression = (ConditionalExpression) node;

			variableBindings.addAll(collectVariables2(conditionalExpression.getElseExpression()));
			variableBindings.addAll(collectVariables2(conditionalExpression.getThenExpression()));
			variableBindings.addAll(collectVariables2(conditionalExpression.getExpression()));

			return variableBindings;
		}

		case ASTNode.FIELD_ACCESS: {
			FieldAccess fieldAccess = (FieldAccess) node;

			variableBindings.addAll(collectVariables2(fieldAccess.getName()));
			variableBindings.addAll(collectVariables2(fieldAccess.getExpression()));

			return variableBindings;
		}

		case ASTNode.INFIX_EXPRESSION: {
			InfixExpression infixExpression = (InfixExpression) node;

			variableBindings.addAll(collectVariables2(infixExpression.getLeftOperand()));

			if (!infixExpression.hasExtendedOperands()) {
				variableBindings.addAll(collectVariables2(infixExpression.getRightOperand()));
			} else {
				for (Object o : infixExpression.extendedOperands()) {
					variableBindings.addAll(collectVariables2((Expression) o));
				}
			}

			return variableBindings;
		}

		case ASTNode.INSTANCEOF_EXPRESSION: {
			InstanceofExpression instanceofExpression = (InstanceofExpression) node;

			variableBindings.addAll(collectVariables2(instanceofExpression.getLeftOperand()));

			return variableBindings;
		}

		case ASTNode.METHOD_INVOCATION: {
			MethodInvocation methodInvocation = (MethodInvocation) node;
			for (Object o : methodInvocation.arguments()) {
				variableBindings.addAll(collectVariables2((Expression) o));
			}

			if (methodInvocation.getExpression() != null) {
				variableBindings.addAll(collectVariables2(methodInvocation.getExpression()));
			}

			return variableBindings;
		}

		/*
		 *  Handler of subtypes of org.eclipse.jdt.core.dom.Name
		 */

		case ASTNode.SIMPLE_NAME: {
			SimpleName name = (SimpleName) node;
			IBinding bi = name.resolveBinding();
			if (bi != null) {
				if (bi instanceof IVariableBinding) {
					variableBindings.add((IVariableBinding) bi);
				}
			}

			return variableBindings;
		}

		case ASTNode.QUALIFIED_NAME: {
			QualifiedName qualifiedName = (QualifiedName) node;

			//TODO Em qualificadores o interessante não é o nome, mas o qualificador. Verificar tal afirmação
//			variableBindings.addAll(collectVariables2(qualifiedName.getName()));
			variableBindings.addAll(collectVariables2(qualifiedName.getQualifier()));

			return variableBindings;
		}

		/*
		 *  End of handler of subtypes of org.eclipse.jdt.core.dom.Name
		 */

		case ASTNode.NULL_LITERAL: {
			// This expression do not have variables
			return variableBindings;
		}

		case ASTNode.NUMBER_LITERAL: {
			// This expression do not have variables
			return variableBindings;
		}

		case ASTNode.PARENTHESIZED_EXPRESSION: {
			ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) node;

			variableBindings.addAll(collectVariables2(parenthesizedExpression.getExpression()));

			return variableBindings;
		}

		case ASTNode.POSTFIX_EXPRESSION: {
			PostfixExpression postfixExpression = (PostfixExpression) node;

			variableBindings.addAll(collectVariables2(postfixExpression.getOperand()));

			return variableBindings;
		}

		case ASTNode.PREFIX_EXPRESSION: {
			PrefixExpression prefixExpression = (PrefixExpression) node;

			variableBindings.addAll(collectVariables2(prefixExpression.getOperand()));

			return variableBindings;
		}

		case ASTNode.STRING_LITERAL: {
			// This expression do not have variables
			return variableBindings;
		}

		case ASTNode.SUPER_FIELD_ACCESS: {
			SuperFieldAccess superFieldAccess = (SuperFieldAccess) node;

			variableBindings.addAll(collectVariables2(superFieldAccess.getName()));
			if (superFieldAccess.getQualifier() != null) {
				variableBindings.addAll(collectVariables2(superFieldAccess.getQualifier()));
			}

			return variableBindings;
		}

		case ASTNode.SUPER_METHOD_INVOCATION: {
			SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation) node;
			for (Object o : superMethodInvocation.arguments()) {
				variableBindings.addAll(collectVariables2((Expression) o));
			}

			if (superMethodInvocation.getQualifier() != null) {
				variableBindings.addAll(collectVariables2(superMethodInvocation.getQualifier()));
			}

			return variableBindings;
		}

		case ASTNode.THIS_EXPRESSION: {
			ThisExpression thisExpression = (ThisExpression) node;

			if (thisExpression.getQualifier() != null) {
				variableBindings.addAll(collectVariables2(thisExpression.getQualifier()));
			}

			return variableBindings;
		}

		case ASTNode.TYPE_LITERAL: {
			// This expression do not have variables
			return variableBindings;
		}

		case ASTNode.VARIABLE_DECLARATION_EXPRESSION: {
			VariableDeclarationExpression variableDeclarationExpression = (VariableDeclarationExpression) node;
			for (Object o : variableDeclarationExpression.fragments()) {
				VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) o;
				variableBindings.addAll(collectVariables2(variableDeclarationFragment.getName()));
				if (variableDeclarationFragment.getInitializer() != null) {
					variableBindings.addAll(collectVariables2(variableDeclarationFragment.getInitializer()));
				}
			}

			return variableBindings;
		}
		
		/*
		 *  org.eclipse.jdt.core.dom.Statement 
		 */
		
		case ASTNode.ASSERT_STATEMENT: {
			AssertStatement assertStatement = (AssertStatement) node;
			variableBindings.addAll(collectVariables2(assertStatement.getExpression()));
			variableBindings.addAll(collectVariables2(assertStatement.getMessage()));
			return variableBindings;
		}
		
		case ASTNode.BLOCK: {
//			Block block = (Block) node;
//			for (Object o : block.statements()) {
//				variableBindings.addAll(collectVariables2((Statement)o));
//			}
			return variableBindings;
		}
		
		case ASTNode.BREAK_STATEMENT: {
			BreakStatement breakStatement = (BreakStatement) node;
			variableBindings.addAll(collectVariables2(breakStatement.getLabel()));
			return variableBindings;
		}
		
		case ASTNode.CONSTRUCTOR_INVOCATION: {
			ConstructorInvocation constructorInvocation = (ConstructorInvocation) node;
			for (Object o : constructorInvocation.arguments()) {
				variableBindings.addAll(collectVariables2((Expression)o));
			}
			return variableBindings;
		}
		
		case ASTNode.CONTINUE_STATEMENT: {
			ContinueStatement continueStatement = (ContinueStatement) node;
			variableBindings.addAll(collectVariables2(continueStatement.getLabel()));
			return variableBindings;
		}
		
		case ASTNode.DO_STATEMENT: {
			DoStatement doStatement = (DoStatement) node;
//			variableBindings.addAll(collectVariables2(doStatement.getBody()));
			variableBindings.addAll(collectVariables2(doStatement.getExpression()));
			return variableBindings;
		}
		
		case ASTNode.EMPTY_STATEMENT: {
			return variableBindings;
		}
		
		case ASTNode.EXPRESSION_STATEMENT: {
			ExpressionStatement expressionStatement = (ExpressionStatement) node;
			variableBindings.addAll(collectVariables2(expressionStatement.getExpression()));
			return variableBindings;
		}

		case ASTNode.FOR_STATEMENT: {
			ForStatement forStatement = (ForStatement) node;
//			variableBindings.addAll(collectVariables2(forStatement.getBody()));
			variableBindings.addAll(collectVariables2(forStatement.getExpression()));
			for (Object o : forStatement.initializers()) {
				variableBindings.addAll(collectVariables2((Expression)o));
			}
			return variableBindings;
		}
		
		case ASTNode.IF_STATEMENT: {
			IfStatement ifStatement = (IfStatement) node;
//			variableBindings.addAll(collectVariables2(ifStatement.getElseStatement()));
			variableBindings.addAll(collectVariables2(ifStatement.getExpression()));
//			variableBindings.addAll(collectVariables2(ifStatement.getThenStatement()));
			return variableBindings;
		}
		
		case ASTNode.LABELED_STATEMENT: {
			LabeledStatement labeledStatement = (LabeledStatement) node;
//			variableBindings.addAll(collectVariables2(labeledStatement.getBody()));
			variableBindings.addAll(collectVariables2(labeledStatement.getLabel()));
			return variableBindings;
		}
		
		case ASTNode.RETURN_STATEMENT: {
			ReturnStatement returnStatement = (ReturnStatement) node;
			variableBindings.addAll(collectVariables2(returnStatement.getExpression()));
			return variableBindings;
		}
		
		case ASTNode.SUPER_CONSTRUCTOR_INVOCATION: {
			SuperConstructorInvocation superConstructorInvocation = (SuperConstructorInvocation) node;
			for (Object o : superConstructorInvocation.arguments()) {
				variableBindings.addAll(collectVariables2((Expression)o));
			}
			variableBindings.addAll(collectVariables2(superConstructorInvocation.getExpression()));
			return variableBindings;
		}
		
		case ASTNode.SWITCH_CASE: {
			SwitchCase switchCase = (SwitchCase) node;
			variableBindings.addAll(collectVariables2(switchCase.getExpression()));
			return variableBindings;
		}
		
		case ASTNode.SWITCH_STATEMENT: {
			SwitchStatement switchStatement = (SwitchStatement) node;
			variableBindings.addAll(collectVariables2(switchStatement.getExpression()));
			return variableBindings;
		}
		
		case ASTNode.SYNCHRONIZED_STATEMENT: {
			SynchronizedStatement synchronizedStatement = (SynchronizedStatement) node;
			variableBindings.addAll(collectVariables2(synchronizedStatement.getExpression()));
			return variableBindings;
		}
		
		case ASTNode.THROW_STATEMENT: {
			ThrowStatement throwStatement = (ThrowStatement) node;
			variableBindings.addAll(collectVariables2(throwStatement.getExpression()));
			return variableBindings;
		}
		
		case ASTNode.TRY_STATEMENT: {
			return variableBindings;
		}
		
		case ASTNode.TYPE_DECLARATION_STATEMENT: {
			return variableBindings;
		}
		
		case ASTNode.VARIABLE_DECLARATION_STATEMENT: {
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;
			for (Object o : variableDeclarationStatement.fragments()) {
				variableBindings.addAll(collectVariables2((VariableDeclarationFragment)o));
			}	
			return variableBindings;
		}
		
		case ASTNode.VARIABLE_DECLARATION_FRAGMENT: {
			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) node;
			variableBindings.addAll(collectVariables2(variableDeclarationFragment.getInitializer()));
			variableBindings.addAll(collectVariables2(variableDeclarationFragment.getName()));
			return variableBindings;
		}
		
		case ASTNode.SINGLE_VARIABLE_DECLARATION: {
			SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) node;
			variableBindings.addAll(collectVariables2(singleVariableDeclaration.getInitializer()));
			variableBindings.addAll(collectVariables2(singleVariableDeclaration.getName()));
			return variableBindings;
		}
		
		case ASTNode.WHILE_STATEMENT: {
			WhileStatement whileStatement = (WhileStatement) node;
			variableBindings.addAll(collectVariables2(whileStatement.getExpression()));
			return variableBindings;
		}
		
		case ASTNode.ENHANCED_FOR_STATEMENT: {
			EnhancedForStatement enhancedForStatement = (EnhancedForStatement) node;
			variableBindings.addAll(collectVariables2(enhancedForStatement.getExpression()));
			variableBindings.addAll(collectVariables2(enhancedForStatement.getParameter()));
			return variableBindings;
		}
		
		default: {
			LOGGER.error(String.format("Node type (%s) cannot be handled (Not yet implemented)", ASTNode.nodeClassForType(node.getNodeType()).getName()));
			return variableBindings;
//			throw new IllegalArgumentException("Node type cannot be handled (Not yet implemented)");
		}

		}

	}
	

	public static Set<IVariableBinding> collectWritableVariables(Expression expression) {
		
		class InternalVisitor extends ASTVisitor {
			
			private Set<IVariableBinding> variables = new HashSet<IVariableBinding>();

			@Override
			public boolean visit(Assignment node) {
				variables.addAll(collectVariables(node.getLeftHandSide()));
				return super.visit(node);
			}
			
		}
		
		InternalVisitor internalVisitor = new InternalVisitor();
		expression.accept(internalVisitor);
		
		return internalVisitor.variables;
	}
	
	public static Set<IVariableBinding> collectReadableVariables(Expression expression) {
		
//		class InternalVisitor extends ASTVisitor {
//			
//			private Set<IVariableBinding> variables = new HashSet<IVariableBinding>();
//
//			@Override
//			public boolean visit(Assignment node) {
//				variables.addAll(collectVariables(node.getLeftHandSide()));
//				return super.visit(node);
//			}
//			
//		}
//		
//		InternalVisitor internalVisitor = new InternalVisitor();
//		expression.accept(internalVisitor);
		
		Set<IVariableBinding> result = collectVariables(expression);
		result.removeAll(collectWritableVariables(expression));
//		result.removeAll(internalVisitor.variables);
		
		return result;
	}
	

}
