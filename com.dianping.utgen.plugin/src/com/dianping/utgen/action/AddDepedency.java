package com.dianping.utgen.action;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.dianping.utgen.Activator;

public class AddDepedency implements IObjectActionDelegate {

	private Shell shell;

	// pom.xml
	private Document pomDoc;
	private Document resourcesDoc;

	private IProject myPomProject;

	private IFile myPomFile;

	/**
	 * Constructor for Action1.
	 */
	public AddDepedency() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if (this.myPomFile != null && this.myPomFile.exists()) {
			this.myPomProject = myPomFile.getProject();
			analysisPom(myPomFile, myPomProject.getLocation().toString());
		} else {
			MessageDialog.openInformation(shell, "Gen", "no pom.xml found, please double check");
			return;
		}
	}

	@SuppressWarnings("deprecation")
	private void analysisPom(IFile pomFile, String fullPath) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.setValidating(true);
			DocumentBuilder builder = factory.newDocumentBuilder();

			InputSource inputSource=new InputSource();
			inputSource.setByteStream(pomFile.getContents());
			inputSource.setEncoding("UTF-8");
			this.pomDoc = builder.parse(inputSource);

			Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
			URL url = bundle.getResource("Resources.xml");
			InputStream is = FileLocator.toFileURL(url).openStream();
			this.resourcesDoc = builder.parse(is);

			addDepedencies(fullPath);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addDepedencies(String fullPath) {
		try {
			if (this.resourcesDoc != null && this.pomDoc != null) {
				
				Element resourceRootElement = resourcesDoc.getDocumentElement();
				NodeList resourcesDepedencyList = resourceRootElement.getElementsByTagName("dependency");

				Element pomRootElement = pomDoc.getDocumentElement();
				NodeList pomDepedenciesList = pomRootElement.getElementsByTagName("dependencies");
				
				if (pomDepedenciesList == null || pomDepedenciesList.getLength() == 0) {
					Element depedencies = pomDoc.createElement("dependencies");
					pomRootElement.appendChild(depedencies);
					pomDepedenciesList = pomRootElement.getElementsByTagName("dependencies");
				}
				for (int i = 0; i < resourcesDepedencyList.getLength(); i++) {
					Element depedency = (Element) resourcesDepedencyList.item(i);
					if (!hasDuplicate(depedency)) {
						if (isConflict(depedency)) {
							MessageDialog.openInformation(shell, "Gen", depedency.getElementsByTagName("artifactId")
									.item(0).getTextContent()
									+ "version conflict, please double check and retry");
							continue;
						} else {
							Element depedencyItem = pomDoc.createElement("dependency");

							Element groupId = pomDoc.createElement("groupId");
							groupId.setTextContent(depedency.getElementsByTagName("groupId").item(0).getTextContent()
									.trim());
							depedencyItem.appendChild(groupId);

							Element artifactId = pomDoc.createElement("artifactId");
							artifactId.setTextContent(depedency.getElementsByTagName("artifactId").item(0)
									.getTextContent().trim());
							depedencyItem.appendChild(artifactId);

							Element version = pomDoc.createElement("version");
							version.setTextContent(depedency.getElementsByTagName("version").item(0).getTextContent()
									.trim());
							depedencyItem.appendChild(version);
							
							//check if there is scope option
							NodeList scopeList=depedency.getElementsByTagName("scope");
							if(scopeList!=null&&scopeList.getLength()>0)
							{
								Element scope=pomDoc.createElement("scope");
								scope.setTextContent(scopeList.item(0).getTextContent().trim());
								depedencyItem.appendChild(scope);
							}

							pomDepedenciesList.item(0).appendChild(depedencyItem);
						}

					}
				}

				toSave(pomDoc, fullPath);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	private boolean isConflict(Element depedency) {
		boolean flag = false;
		if (this.pomDoc != null) {
			Element rootElement = pomDoc.getDocumentElement();
			NodeList dependeciesList = rootElement.getElementsByTagName("dependencies");
			NodeList dependecyList = ((Element) dependeciesList.item(0)).getElementsByTagName("dependency");
			if (dependecyList != null) {
				for (int i = 0; i < dependecyList.getLength(); i++) {
					Element item = (Element) dependecyList.item(i);
					if (item.getElementsByTagName("groupId").item(0).getTextContent()
							.equals(depedency.getElementsByTagName("groupId").item(0).getTextContent())
							&& item.getElementsByTagName("artifactId").item(0).getTextContent()
									.equals(depedency.getElementsByTagName("artifactId").item(0).getTextContent())
							&& !item.getElementsByTagName("version").item(0).getTextContent()
									.equals(depedency.getElementsByTagName("version").item(0).getTextContent())) {
						flag = true;
						break;
					}
				}

			}
		}

		return flag;
	}

	private boolean hasDuplicate(Element depedency) {
		boolean flag = false;
		// fetch the depedency list
		if (this.pomDoc != null) {
			Element rootElement = pomDoc.getDocumentElement();
			NodeList dependeciesList = rootElement.getElementsByTagName("dependencies");
			NodeList dependecyList = ((Element) dependeciesList.item(0)).getElementsByTagName("dependency");
			if (dependecyList != null) {
				for (int i = 0; i < dependecyList.getLength(); i++) {
					Element item = (Element) dependecyList.item(i);
					if (item.getElementsByTagName("groupId").item(0).getTextContent()
							.equals(depedency.getElementsByTagName("groupId").item(0).getTextContent())
							&& item.getElementsByTagName("artifactId").item(0).getTextContent()
									.equals(depedency.getElementsByTagName("artifactId").item(0).getTextContent())
							&& item.getElementsByTagName("version").item(0).getTextContent()
									.equals(depedency.getElementsByTagName("version").item(0).getTextContent())) {
						flag = true;
						break;
					}
				}

			}
		}

		return flag;
	}

	private void toSave(Document pomDoc, String fullPath) throws Exception {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		DOMSource source = new DOMSource(pomDoc);
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		PrintWriter pw = new PrintWriter(new FileOutputStream(fullPath + java.io.File.separator + "pom.xml"));
		StreamResult result = new StreamResult(pw);
		transformer.transform(source, result);
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		try {
			if (selection != null && selection.isEmpty() == false && selection instanceof IStructuredSelection) {
				IStructuredSelection ssel = (IStructuredSelection) selection;
				Object obj = ssel.getFirstElement();
				if (obj instanceof IFile) {
					String fileName = ((IFile) obj).getName();
					if (fileName.equals("pom.xml")) {
						this.myPomFile = (IFile) obj;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
