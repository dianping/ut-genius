package com.dianping.utgen.utils;

import java.util.Map;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jface.text.Document;

import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

public class CodeFormatterUtils {

	private static Object createCodeFormatter(IProject project) {
		IJavaProject javaProject = JavaCore.create(project);
		Map options = javaProject.getOptions(true);
		return ToolFactory.createCodeFormatter(options);
	}

	public static String formatCode(String contents, IProject project) {
		Object codeFormatter = createCodeFormatter(project);
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

}
