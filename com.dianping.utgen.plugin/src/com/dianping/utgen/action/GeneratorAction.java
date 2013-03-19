package com.dianping.utgen.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.jdt.ui.JavaUI;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.ui.packageview.PackageFragmentRootContainer;

import com.dianping.utgen.Activator;
import com.dianping.utgen.utils.CodeFormatterUtils;
import com.dianping.utgen.utils.FileUtils;
import com.dianping.utgen.utils.StringUtils;
import com.dianping.utgen.utils.TemplateUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.util.HashSet;
import java.util.Set;

/**
 * Sample code template for editor context menu and editor text selection or
 * file text processing.
 * 
 * @author muxi.zhang
 */
public class GeneratorAction implements IObjectActionDelegate {
	private static final String TITLE = "Do Stuff";
	private Shell shell;

	/**
	 * Constructor
	 */
	public GeneratorAction() {
		super();
	}

	/**
	 * Get the shell object for use in prompting error message.
	 * 
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	public void zz() throws JavaModelException {
		ITextEditor editor = (ITextEditor) Activator.getDefault()
				.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getActiveEditor();

		ITextSelection selection = (ITextSelection) editor
				.getSelectionProvider().getSelection();

		IEditorInput editorInput = editor.getEditorInput();
		IJavaElement elem = JavaUI.getEditorInputJavaElement(editorInput);
		if (elem instanceof ICompilationUnit) {
			ICompilationUnit unit = (ICompilationUnit) elem;
			IJavaElement selected = unit.getElementAt(selection.getOffset());

			System.out.println("selected=" + selected);
			System.out.println("selected.class=" + selected.getClass());
		}
	}

	public static IType getDeclaringInterface(IMethod method)
			throws JavaModelException {

		IType type = method.getDeclaringType();
		ITypeHierarchy typeHierarchy = type.newSupertypeHierarchy(null);
		return typeHierarchy.getSuperclass(type);
	}

	/**
	 * Get the selection text or text for whole file and pass it to process for
	 * modification.
	 * 
	 * @see IActionDelegate#run(IAction)
	 */
	@Override
	public void run(IAction action) {
		try {

			// get active editor
			IEditorPart editorPart = getActiveEditor();
			if (!isTextEditor(editorPart))
				return;
			IMethod selectedMethod = FileUtils.getSelectedMethod(editorPart);

			Set<String> interceptees = DirectDependencyAnalyzer.analyze(selectedMethod);
			
			IProject project = FileUtils.getCurrentProject();

			DataModel model = DirectDependencyAnalyzer.getModel(selectedMethod);
			String formattedSourceCode = createFormattedTempl(model);

			String genTmpl = decideGeneratorPath(selectedMethod);
			IFile testCase = FileUtils.writeFile(project, genTmpl,
					formattedSourceCode);

			StringBuffer config = TemplateUtils.renderConfig(interceptees);
			FileUtils.writeFile(project,
					"/src/test/resources/generatorContext.xml", config);

			String infl = "infl=" + StringUtils.join(interceptees, ",");
			FileUtils.writeFile(project, "/src/test/resources/path.properties",
					infl);

			FileUtils.openEditor(testCase);

		} catch (Exception e) {
			e.printStackTrace();
			MessageDialog.openError(shell, TITLE, "no method is selected");
		}
	}

	public String createFormattedTempl(IMethod selectedMethod)
			throws JavaModelException {
		StringBuffer sourceCode = TemplateUtils.renderSkeleton(selectedMethod);

		IProject project = FileUtils.getCurrentProject();
		Object codeFormatter = FileUtils.createCodeFormatter(project);
		return FileUtils.formatCode(sourceCode, codeFormatter);
	}

	public String createFormattedTempl(DataModel model)
			throws JavaModelException {
		String sourceCode = TemplateUtils.renderSkeleton(model).toString();

		IProject project = FileUtils.getCurrentProject();
		Object codeFormatter = FileUtils.createCodeFormatter(project);
		return FileUtils.formatCode(sourceCode, codeFormatter);
	}

	private IEditorPart getActiveEditor() {
		return Activator.getDefault().getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().getActiveEditor();
	}

	private boolean isTextEditor(IEditorPart editorPart) {
		return (editorPart instanceof AbstractTextEditor);
	}

	private static String decideGeneratorPath(IMethod selectedMethod) {
		return "/src/test/java/"
				+ StringUtils.replace(selectedMethod.getDeclaringType()
						.getPackageFragment().getElementName(), ".", "/", -1)
				+ "/Generator.java";

	}

	/**
	 * Skip implementation
	 * 
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

}
