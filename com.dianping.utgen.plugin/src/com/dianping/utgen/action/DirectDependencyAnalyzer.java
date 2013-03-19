package com.dianping.utgen.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.Type;

import com.dianping.utgen.utils.StringUtils;

public class DirectDependencyAnalyzer {
	public static void analyze(final CompilationUnit compilationUnit,
			final Set<String> interceptees) throws JavaModelException {
		final FileLogger logger = new FileLogger();
		compilationUnit.accept(new ASTVisitor() {

			public boolean visit(MethodInvocation node) {
		
				if(node.toString().contains(".") && node.toString().startsWith("this.")) return true;
				
				IMethodBinding methodBinding = node.resolveMethodBinding();

				ITypeBinding typeBinding = methodBinding.getDeclaringClass();
				if(Modifier.isAbstract(typeBinding.getModifiers())) return true;
				if(!typeBinding.isTopLevel()) return true;
				if (typeBinding.isEnum()) return true;
				if(methodBinding.isConstructor()) return true;
				int modifiers = methodBinding.getModifiers();
				if (Modifier.isStatic(modifiers))
					return true;
				String packageName = typeBinding.getPackage().getName();
				if (isPackageFiltered(packageName))
					return true;
				
//				try {
//					getAdditionalMock(compilationUnit,interceptees);					
//				} catch (JavaModelException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} 
				String name = getTypeBindngName(typeBinding);
				interceptees.add(impl2Declare(packageName + "." + name));
				
				//interceptees.add(getInterface(typeBinding));
				return true;
			}
		});
		logger.close();
	}
	
	public static Set<String> analyze(IMethod type) throws JavaModelException {
		final Set<String> interceptees = new HashSet<String>();
		
		CompilationUnit compilationUnit = ASTParsingUtilities.parse(type
				.getCompilationUnit());
		
		interceptees.add(type.getDeclaringType().getFullyQualifiedName());
		analyze(compilationUnit, interceptees);
		//getAdditionalMock(compilationUnit,interceptees);
		
		/*
		 * CompilationUnit compilationUnit2 = ASTParsingUtilities.parse(type
		 * .getDeclaringType().getCompilationUnit());
		 * compilationUnit2.accept(new ASTVisitor() { public boolean
		 * visit(VariableDeclarationFragment node) { ASTNode parent =
		 * node.getParent(); if ((parent instanceof FieldDeclaration)) {
		 * FieldDeclaration field = (FieldDeclaration) parent;
		 * 
		 * Logger.log(printModifiers(((FieldDeclaration) parent)
		 * .getModifiers())); Logger.log(node.getName().getIdentifier()); }
		 * return true; }
		 * 
		 * });
		 */

		return interceptees;
	}
	
	private String getInterface(IType itype) throws JavaModelException {
			
			ITypeHierarchy hierarchy = itype.newSupertypeHierarchy(null);
			IType[] allInterfaces = hierarchy.getAllInterfaces();
			for (IType iInterfaceType : allInterfaces) {
				String interfaceName = iInterfaceType.getElementName();
				String interfacePackageName = iInterfaceType.getPackageFragment().getElementName();
				// select the first interface under package 'com.dianping'
				if (!StringUtils.isEmpty(interfacePackageName) && interfacePackageName.startsWith("com.dianping.")) {
					return interfacePackageName+"."+interfaceName;
				}
			}
			return null;
		
	}
	
	private static String getTypeBindngName(ITypeBinding typeBinding) {
		String name = typeBinding.getName();
		if (name.contains("<")) {
			int index = name.indexOf("<");
			name = name.substring(0, index - 1);
		}
		return name;
	}
	
	private static boolean isPackageFiltered(String packageName) {
		if (packageName.contains("result"))
			return true;
		if (packageName.contains("log"))
			return true;
		if (packageName.contains("dto"))
			return true;
		if (packageName.startsWith("java"))
			return true;
		
		return false;
	}

	

	private static String impl2Declare(String impl) {
		return StringUtils.replace(StringUtils.replace(impl, ".impl", "", 1),
				"Impl", "", 1);
	}

	/*
	 * private static String printModifiers(int modifiers) { StringBuffer buffer
	 * = new StringBuffer(); if (Modifier.isPublic(modifiers)) {
	 * buffer.append("public"); } if (Modifier.isProtected(modifiers)) { if
	 * (buffer.length() > 0) { buffer.append(','); } buffer.append("protected");
	 * } if (Modifier.isPrivate(modifiers)) { if (buffer.length() > 0) {
	 * buffer.append(','); } buffer.append("private"); } if
	 * (Modifier.isAbstract(modifiers)) { if (buffer.length() > 0) {
	 * buffer.append(','); } buffer.append("abstract"); } if
	 * (Modifier.isStatic(modifiers)) { if (buffer.length() > 0) {
	 * buffer.append(','); } buffer.append("static"); } if
	 * (Modifier.isFinal(modifiers)) { if (buffer.length() > 0) {
	 * buffer.append(','); } buffer.append("final"); } if
	 * (Modifier.isSynchronized(modifiers)) { if (buffer.length() > 0) {
	 * buffer.append(','); } buffer.append("synchronized"); } if
	 * (Modifier.isNative(modifiers)) { if (buffer.length() > 0) {
	 * buffer.append(','); } buffer.append("native"); } if
	 * (Modifier.isTransient(modifiers)) { if (buffer.length() > 0) {
	 * buffer.append(','); } buffer.append("transient"); } if
	 * (Modifier.isVolatile(modifiers)) { if (buffer.length() > 0) {
	 * buffer.append(','); } buffer.append("volatile"); } if
	 * (Modifier.isStrictfp(modifiers)) { if (buffer.length() > 0) {
	 * buffer.append(','); } buffer.append("strictfp"); } return
	 * buffer.toString(); }
	 */

	public static MethodDeclaration convertToAstNode(final IMethod method)
			throws JavaModelException {
		final IType type = method.getDeclaringType();
		CompilationUnit compilationUnit = ASTParsingUtilities.parse(type
				.getCompilationUnit());
		final ASTParser astParser = ASTParser.newParser(AST.JLS3);
		// astParser.setSource( compilationUnit );
		astParser.setKind(ASTParser.K_COMPILATION_UNIT);
		astParser.setResolveBindings(true);
		astParser.setBindingsRecovery(true);

		final ASTNode rootNode = astParser.createAST(null);

		final CompilationUnit compilationUnitNode = (CompilationUnit) rootNode;

		final String key = method.getKey();
		System.out.println("############## " + key);

		final ASTNode javaElement = compilationUnitNode.findDeclaringNode(key);

		final MethodDeclaration methodDeclarationNode = (MethodDeclaration) javaElement;

		return methodDeclarationNode;
	}

	public static DataModel getModel(ICompilationUnit icu, CompilationUnit cu) throws JavaModelException {
		final DataModel model = new DataModel();
	
		IType[] types = icu.getTypes();
		if (null != types && types.length > 0) {
			IType itype = types[0];
	
			model.setImplementationName(itype.getElementName());
			model.setPackageName(itype.getPackageFragment().getElementName());
			
			ITypeHierarchy hierarchy = itype.newSupertypeHierarchy(null);
			IType[] allInterfaces = hierarchy.getAllInterfaces();
			for (IType iInterfaceType : allInterfaces) {
				String interfaceName = iInterfaceType.getElementName();
				String interfacePackageName = iInterfaceType.getPackageFragment().getElementName();
				// select the first interface under package 'com.dianping'
				if (!StringUtils.isEmpty(interfacePackageName) && interfacePackageName.startsWith("com.dianping.")) {
					model.setInterfaceName(interfaceName);
					model.setInterfacePackage(interfacePackageName);
					break;
				}
			}
		}
	
		cu.accept(new ASTVisitor() {
	
			public boolean visit(MethodDeclaration node) {
				if(node.isConstructor()) return true;
				 ITypeBinding declaringClass = node.resolveBinding().getDeclaringClass();
				 if(!declaringClass.isTopLevel()) return true;
				 
				String methodName = node.getName().getIdentifier();
				if(methodName.startsWith("set")){
					return true;
				}
				int modifiers = node.getModifiers();
				if (Modifier.isPrivate(modifiers)) return true;
				
				List<SingleVariableDeclaration> paramList = node.parameters();
				if (paramList.size() == 0)
					return true;
				String[] cc = new String[paramList.size()];
				int i = 0;
				for (SingleVariableDeclaration param : paramList) {
					cc[i++] = param.getName().toString();
				}
				model.addMethod(node.getName().getIdentifier(), cc);
				return true;
			}
		});
		return model;
	}
	
	public static void getAdditionalMock(IMethod type,final Set<String> aaa) throws JavaModelException {		
		CompilationUnit compilationUnit = ASTParsingUtilities.parse(type
				.getCompilationUnit());
		
		 getAdditionalMock(compilationUnit,aaa);
	}
	
	public static void getAdditionalMock(CompilationUnit cu,final Set<String> aaa) throws JavaModelException {
		//final List<String> aaa = new ArrayList<String>();
		cu.accept(new ASTVisitor() {
			
			public boolean visit(MethodDeclaration node) {
				if(node.isConstructor()) return true;
				 ITypeBinding declaringClass = node.resolveBinding().getDeclaringClass();
				 if(!declaringClass.isTopLevel()) return true;
				 
				String methodName = node.getName().getIdentifier();
				if(methodName.startsWith("set")){
					return true;
				}
				int modifiers = node.getModifiers();
				if (Modifier.isPrivate(modifiers)) return true;
				
				List<SingleVariableDeclaration> paramList = node.parameters();
				if (paramList.size() == 0)
					return true;
				String[] cc = new String[paramList.size()];
				int i = 0;
				for (SingleVariableDeclaration param : paramList) {
					Type type = param.getType();
					if(type.isPrimitiveType()) continue;
					
					ITypeBinding typeBinding = type.resolveBinding();
					 IPackageBinding packageBinding = typeBinding.getPackage();
					if(packageBinding != null) {
						String name = getTypeBindngName(typeBinding);
						String fullName = packageBinding.getName() + "." + name;
						if(fullName.equals("java.lang.String")) continue;
						aaa.add(impl2Declare(packageBinding.getName() + "." + name));
					}
				}

				return true;
			}
		});
	}
	public static DataModel getModel(IMethod selectedMethod) throws JavaModelException {
		DataModel model = new DataModel();
		model.setPackageName(selectedMethod.getDeclaringType().getPackageFragment().getElementName());
		model.setInterfacePackage(StringUtils.replace(selectedMethod.getDeclaringType().getPackageFragment()
				.getElementName(), ".impl", "", 1));
		model.setInterfaceName(StringUtils.replace(selectedMethod.getDeclaringType().getElementName(), "Impl", "", 1));
		model.setImplementationName(selectedMethod.getDeclaringType().getElementName());
	
		model.addMethod(selectedMethod.getElementName(), selectedMethod.getRawParameterNames());
		return model;
	}
}
