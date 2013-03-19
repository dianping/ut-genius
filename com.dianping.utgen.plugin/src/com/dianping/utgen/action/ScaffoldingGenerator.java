package com.dianping.utgen.action;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.dianping.utgen.utils.CodeFormatterUtils;
import com.dianping.utgen.utils.FileUtils;
import com.dianping.utgen.utils.StringUtils;
import com.dianping.utgen.utils.TemplateUtils;

public class ScaffoldingGenerator implements IObjectActionDelegate {
	private Shell shell;

	public static IProject getCurrentSelectedProject() {
		IProject project = null;
		ISelectionService selectionService = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getSelectionService();

		ISelection selection = selectionService.getSelection();

		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection)
					.getFirstElement();

			if (element instanceof IResource) {
				project = ((IResource) element).getProject();
			} else if (element instanceof PackageFragmentRoot) {
				IJavaProject jProject = ((PackageFragmentRoot) element)
						.getJavaProject();
				project = jProject.getProject();
			} else if (element instanceof IJavaElement) {
				IJavaProject jProject = ((IJavaElement) element)
						.getJavaProject();
				project = jProject.getProject();
			}
		}
		return project;
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	@Override
	public void run(IAction action) {
		if (selectedElement instanceof ICompilationUnit) {
			try {
				createOutput(shell, selectedElement);
			} catch (CoreException e) {
				e.printStackTrace();
			}

		} else {
			MessageDialog.openInformation(shell, "Info",
					"Please select a Java source file");
		}
	}

	/*
	 * private static CompilationUnit parse(ICompilationUnit unit) {
	 * 
	 * ASTParser parser = ASTParser.newParser(AST.JLS3);
	 * parser.setKind(ASTParser.K_COMPILATION_UNIT); parser.setSource(unit);
	 * parser.setResolveBindings(true); return (CompilationUnit)
	 * parser.createAST(null); // parse }
	 */
	private void createOutput(Shell shell, Object selectedElement)
			throws CoreException {
		ICompilationUnit cu = (ICompilationUnit) selectedElement;
		CompilationUnit parse = ASTParsingUtilities.parse(cu);
		generateFile(cu, parse, getCurrentSelectedProject());

	}

	private static String decideGeneratorPath(CompilationUnit parse) {
		return "/src/test/java/"
				+ StringUtils.replace(parse.getPackage().getName().toString(),
						".", "/", -1) + "/Generator.java";

	}

	public String createFormattedTempl(DataModel model)
			throws JavaModelException {
		String sourceCode = TemplateUtils.renderSkeleton(model).toString();

		IProject project = FileUtils.getCurrentProject();
		Object codeFormatter = FileUtils.createCodeFormatter(project);
		return FileUtils.formatCode(sourceCode, codeFormatter);
	}

	private void generateFile(ICompilationUnit cu, CompilationUnit parse,
			IProject project) throws CoreException, JavaModelException {

		DataModel model = DirectDependencyAnalyzer.getModel(cu, parse);
		String formattedSourceCode = createFormattedTempl(model);
		String genTmpl = decideGeneratorPath(parse);
		IFile testCase = FileUtils.writeFile(project, genTmpl,
				formattedSourceCode);

		final Set<String> interceptees = new HashSet<String>();		
		interceptees.add(cu.getTypes()[0].getFullyQualifiedName());
		DirectDependencyAnalyzer.analyze(parse, interceptees);
		for(String aa : interceptees) {
			if(aa.startsWith(cu.getTypes()[0].getFullyQualifiedName()+".")) {
				interceptees.remove(aa);
			}
		}
		
		IFile xml = project.getFile("/src/test/resources/generatorContext.xml");
		String config = TemplateUtils.renderConfig(interceptees).toString();

		IFile context = FileUtils.writeFile(project,
				"/src/test/resources/generatorContext.xml", config);

		String infl = "infl=" + StringUtils.join(interceptees, ",");
		FileUtils.writeFile(project, "/src/test/resources/path.properties",
				infl);
		
		FileUtils.openEditor(testCase);
	}

	Object selectedElement = null;

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		try {
			if (selection != null && selection.isEmpty() == false
					&& selection instanceof IStructuredSelection) {
				IStructuredSelection ssel = (IStructuredSelection) selection;
				Object obj = ssel.getFirstElement();
				if (obj instanceof ICompilationUnit) {
					this.selectedElement = obj;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}