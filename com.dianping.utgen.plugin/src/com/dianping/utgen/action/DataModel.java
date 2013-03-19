package com.dianping.utgen.action;

import java.util.ArrayList;
import java.util.List;

public class DataModel {
	private String packageName;
	private String implementationName;
	private String interfaceName;
	private String interfacePackage;
	private List<Method> methods;
	public List<Method> getMethods() {
		return methods;
	}
	DataModel() {
		methods = new ArrayList<Method>();
	}
	
	public void addMethod(String methodName, String[] parameters) {
		methods.add(new Method(methodName, parameters));
	}	

	public String getInterfacePackage() {
		return interfacePackage;
	}

	public void setInterfacePackage(String interfacePackage) {
		this.interfacePackage = interfacePackage;
	}

	public void setImplementationName(String implementationName) {
		this.implementationName = implementationName;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	

	public String getImplementationName() {
		return implementationName;
	}
	
	public static class Method {
		private String methodName;
		private String[] parameters;
		Method(String methodName, String[] parameters) {
			this.parameters = parameters;
			this.methodName = methodName;
		}
		public String[] getParameters() {
			return parameters;
		}

		public String getMethodName() {
			return methodName;
		}

	}
}
