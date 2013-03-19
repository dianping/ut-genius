package com.dianping.utgen.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import com.dianping.utgen.Activator;

public class FileUtils {
	public static IMethod getSelectedMethod(IEditorPart activeEditor) {
		if (activeEditor instanceof JavaEditor) {
			ICompilationUnit root = (ICompilationUnit) EditorUtility
					.getEditorInputJavaElement(activeEditor, false);
			try {
				ITextSelection sel = (ITextSelection) ((JavaEditor) activeEditor)
						.getSelectionProvider().getSelection();
				int offset = sel.getOffset();
				IJavaElement element = root.getElementAt(offset);
				if (element.getElementType() == IJavaElement.METHOD) {
					return (IMethod) element;
				}
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	public static IProject getCurrentProject() {
		IEditorInput input = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getActiveEditor()
				.getEditorInput();
		IFile file = ((FileEditorInput) input).getFile();
		System.out.println(file.getFullPath().toFile().getAbsolutePath());
		return file.getProject();
	}

	public static void prepare(IFolder folder) throws CoreException {
		if (!folder.exists()) {
			prepare((IFolder) folder.getParent());
			folder.create(false, false, null);
		}
	}

	public static IMethod getMethod(final IEditorPart editor) {
		final ITypeRoot element = JavaUI.getEditorInputTypeRoot(editor
				.getEditorInput());
		if (element != null) {
			IMethod selectedMethod = null;
			if (Display.getCurrent() == null) {
				final IMethod[] temp = new IMethod[1];
				Runnable runnable = new Runnable() {
					public void run() {
						temp[0] = FileUtils.resolveSelectedMethodName(editor,
								element);
					}
				};
				Display.getDefault().syncExec(runnable);
				selectedMethod = temp[0];
			} else {
				selectedMethod = FileUtils.resolveSelectedMethodName(editor,
						element);
			}
			return selectedMethod;
		}
		return null;
	}
	

	public static Object createCodeFormatter(IProject project) {
		IJavaProject javaProject = JavaCore.create(project);
		Map options = javaProject.getOptions(true);
		return ToolFactory.createCodeFormatter(options);
	}

	public static IMethod resolveSelectedMethodName(IEditorPart editor,
			ITypeRoot element) {
		try {
			ISelectionProvider selectionProvider = editor.getSite()
					.getSelectionProvider();

			if (selectionProvider == null)
				return null;

			ISelection selection = selectionProvider.getSelection();
			if (!(selection instanceof ITextSelection))
				return null;

			ITextSelection textSelection = (ITextSelection) selection;

			IJavaElement elementAtOffset = SelectionConverter
					.getElementAtOffset(element, textSelection);
			if (!(elementAtOffset instanceof IMethod))
				return null;

			IMethod method = (IMethod) elementAtOffset;

			ISourceRange nameRange = method.getNameRange();
			if (nameRange.getOffset() <= textSelection.getOffset()
					&& textSelection.getOffset() + textSelection.getLength() <= nameRange
							.getOffset() + nameRange.getLength())
				return method;
		} catch (JavaModelException e) {
			// ignore
		}
		return null;
	}
	public static String formatCode(StringBuffer contents, Object codeFormatter) {
		return formatCode(contents.toString(),codeFormatter);
	}

	public static String formatCode(String contents, Object codeFormatter) {
		if (codeFormatter instanceof CodeFormatter) {
			IDocument doc = new Document(contents);
			TextEdit edit = ((CodeFormatter) codeFormatter).format(
					CodeFormatter.K_COMPILATION_UNIT, doc.get(), 0, doc.get()
							.length(), 0, null);
			if (edit != null) {
				try {
					edit.apply(doc);
					contents = doc.get();
				} catch (Exception e) {
					System.out.println(e);
				}
			}
		}
		return contents;
	}

	public static void openEditor(IFile fileToBeOpened) throws PartInitException {
		IEditorInput editorInput = new FileEditorInput(fileToBeOpened);
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		IEditorDescriptor ed = Activator.getDefault().getWorkbench()
				.getEditorRegistry()
				.getDefaultEditor(fileToBeOpened.getFullPath().toString());
		if (ed != null) {
			page.openEditor(editorInput, ed.getId());
		}
		// page.openEditor(editorInput, "org.eclipse.ui.DefaultTextEdtior");
	}
	public static IFile writeFile(IProject project, String path, StringBuffer content) throws CoreException {
		return writeFile(project, path, content.toString());
	}
	public static IFile writeFile(IProject project, String path, String content)
			throws CoreException {
		IFile properties = project.getFile(path);
		InputStream bytes = new ByteArrayInputStream(content.getBytes());
		if (properties.exists()) {
			properties.setContents(bytes, false, false, null);
		} else {
			FileUtils.prepare((IFolder) properties.getParent());
			properties.create(bytes, false, null);
		}
		return properties;
	}
}
