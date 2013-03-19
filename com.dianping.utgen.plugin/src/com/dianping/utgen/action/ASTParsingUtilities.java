package com.dianping.utgen.action;

import org.eclipse.jdt.core.dom.CompilationUnit;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jdt.core.dom.ASTParser;

import org.eclipse.jdt.core.dom.AST;

import java.util.Map;

public class ASTParsingUtilities {

	public static String SourceLevel = null;

	public static CompilationUnit parse(ICompilationUnit compilationUnit) {
		return parse(compilationUnit, true, null);
	}

	public static CompilationUnit parse(ICompilationUnit compilationUnit,
			boolean resolveBindings, IProgressMonitor monitor) {
		if ((compilationUnit == null) || (!compilationUnit.exists())) {
			Logger.log("Compilation unit does not exist: "
					+ (compilationUnit == null ? "null" : compilationUnit
							.getElementName()));
			return null;
		}
		if (!compilationUnit.isOpen())
			try {
				compilationUnit.open(null);
			} catch (JavaModelException exception) {
				Logger.log("Java model exception while opening :"
						+ compilationUnit.getElementName());
				Logger.log(exception);
			}
		
		try {
			Map options = decideOptions(compilationUnit);
			ASTParser parser = ASTParser.newParser(3);
			parser.setSource(compilationUnit);
			parser.setResolveBindings(resolveBindings);
			
			if (options != null) {
				parser.setCompilerOptions(options);
			}
			return (CompilationUnit) parser.createAST(monitor);
		} catch (Throwable localThrowable1) {
			try {
				return AST.parseCompilationUnit(compilationUnit,
						resolveBindings);
			} catch (Throwable innerException) {
				Logger.log("Inner Exception while parsing compilation unit:"
						+ compilationUnit.getElementName());
				Logger.log(innerException);
			}
		}
		return null;
	}

	private static Map<String, String> decideOptions(ICompilationUnit compilationUnit) {
		Map<String, String> options;
		if (SourceLevel == null) {
			options = null;
		} else {
			options = compilationUnit.getJavaProject().getOptions(true);

			options.put("org.eclipse.jdt.core.compiler.compliance", SourceLevel);
			options.put("org.eclipse.jdt.core.compiler.source", SourceLevel);
		}
		return options;
	}

	public static CompilationUnit parse(IClassFile classFile) {
		return parse(classFile, true, null);
	}

	public static CompilationUnit parse(IClassFile classFile,
			boolean resolveBindings, IProgressMonitor monitor) {
		if ((classFile == null) || (!classFile.exists())) {
			return null;
		}
		if (!classFile.isOpen()) {
			try {
				classFile.open(null);
			} catch (JavaModelException localJavaModelException) {
			}
		}
		try {
			ASTParser parser = ASTParser.newParser(3);
			parser.setSource(classFile);
			parser.setResolveBindings(resolveBindings);
			return (CompilationUnit) parser.createAST(monitor);
		} catch (Throwable localThrowable1) {
			try {
				return AST.parseCompilationUnit(classFile, resolveBindings);
			} catch (Throwable localThrowable2) {
			}
		}
		return null;
	}

	private CompilationUnit createASTStructure(ITypeRoot astSource) {
		if ((astSource instanceof ICompilationUnit))
			return ASTParsingUtilities.parse((ICompilationUnit) astSource);
		if ((astSource instanceof IClassFile)) {
			return ASTParsingUtilities.parse((IClassFile) astSource);
		}
		throw new IllegalArgumentException(
				"The type object passed to this method must either be an ICompilationUnit or an IClassFile.");
	}
}
