package com.dianping.utgen.utils;

import java.io.File;
import java.util.Set;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

import com.dianping.utgen.action.DataModel;
import com.dianping.utgen.action.DirectDependencyAnalyzer;

public class TemplateUtils {
	public static StringBuffer renderSkeleton(IMethod selectedMethod) throws JavaModelException {
		return renderSkeleton(DirectDependencyAnalyzer.getModel(selectedMethod));
	}

	public static StringBuffer renderSkeleton(DataModel model) throws JavaModelException {

		String name = model.getInterfaceName();

		StringBuffer buffer = new StringBuffer();
		buffer.append("package ");
		buffer.append(model.getPackageName());
		buffer.append(";\n\n");

		buffer.append("import org.junit.Test;\n");
		buffer.append("import org.junit.runner.RunWith;\n");
		buffer.append("import org.springframework.beans.factory.annotation.Autowired;\n");
		buffer.append("import org.springframework.test.context.ContextConfiguration;\n");
		buffer.append("import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;\n");

		buffer.append("import com.dianping.utgen.aop.UnitTestGenTargetManager;\n");
		buffer.append("import com.dianping.utgen.conf.UnitTestGenSimpleTargetManager;\n");
		buffer.append("import ");
		buffer.append(model.getInterfacePackage());
		buffer.append(".");
		buffer.append(name);
		buffer.append(";\n");

		buffer.append("\n\n");

		buffer.append("@RunWith(SpringJUnit4ClassRunner.class)\n");
		buffer.append("@ContextConfiguration(locations = {\n");
		buffer.append("\"classpath*:/config/spring/common/appcontext-*.xml\",\n");
		buffer.append("\"classpath*:/config/spring/local/appcontext-*.xml\",\n");
		buffer.append("\"classpath*:/config/spring/test/appcontext-*.xml\",\n");
		buffer.append("\"classpath*:generatorContext.xml\"})\n");

		buffer.append("public class Generator {\n");

		buffer.append("@Autowired private ");

		buffer.append(name);
		buffer.append(" ");
		buffer.append(StringUtils.uncap(name));
		buffer.append(";\n");
		buffer.append("@Autowired private UnitTestGenTargetManager unitTestGenTargetManager;\n");

		for (DataModel.Method method : model.getMethods()) {
			buffer.append("@Test public void generate");
			buffer.append(StringUtils.cap(method.getMethodName()));
			buffer.append("Test(){\n");

			buffer.append("((UnitTestGenSimpleTargetManager) unitTestGenTargetManager).setTarget(\"");
			buffer.append(model.getPackageName());
			buffer.append(".");
			buffer.append(model.getImplementationName());
			buffer.append("#");
			buffer.append(method.getMethodName());
			buffer.append("\");\n");
			buffer.append(StringUtils.uncap(name));
			buffer.append(".");
			buffer.append(method.getMethodName());
			buffer.append("(");
			buffer.append(StringUtils.join(method.getParameters(), ","));
			buffer.append(");\n}");
		}
		buffer.append("\n}");

		return buffer;
	}

	public static StringBuffer renderConfig(Set<String> aa) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		buffer.append("<beans default-autowire=\"byName\" xmlns=\"http://www.springframework.org/schema/beans\"\n");
		buffer.append("       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:aop=\"http://www.springframework.org/schema/aop\"\n");
		buffer.append("       xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.5.xsd\n");
		buffer.append("       http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-2.5.xsd\">\n");

		buffer.append("    <aop:aspectj-autoproxy/>\n");

		buffer.append("    <bean id=\"unitTestGenTargetManager\" class=\"com.dianping.utgen.conf.UnitTestGenSimpleTargetManager\"/>\n");

		buffer.append("    <bean id=\"fileResourceDelivery\" class=\"com.dianping.utgen.output.impl.FileResourceDelivery\">\n");
		buffer.append("        <property name=\"directory\" value=\"src" + File.separator + "test\"/>\n");
		buffer.append("    </bean>\n");
		buffer.append("    <bean id=\"generatorFactory\" class=\"com.dianping.utgen.output.impl.AdvancedJsonUnitTestGeneratorFactory\"/>\n");
		buffer.append("    <bean id=\"unitTestGenTargetMethodInterceptor\" class=\"com.dianping.utgen.aop.UnitTestGenTargetMethodInterceptor\">\n");
		buffer.append("        <property name=\"targetManager\" ref=\"unitTestGenTargetManager\" />\n");
		buffer.append("        <property name=\"generatorFactory\" ref=\"generatorFactory\"/>\n");
		buffer.append("        <property name=\"resourceDelivery\" ref=\"fileResourceDelivery\"/>\n");
		buffer.append("    </bean>\n");

		buffer.append("    <aop:config>\n");
		buffer.append("        <aop:advisor id=\"unitTestGenTargetAdvisor\"\n");
		buffer.append("                     advice-ref=\"unitTestGenTargetMethodInterceptor\"\n");
		buffer.append("                     pointcut=\"\n");
		String[] bb = new String[aa.size()];
		int i = 0;
		for (String str : aa) {
			bb[i++] = "execution(* " + str + ".*(..))\n";
		}
		buffer.append(StringUtils.join(bb, " or "));
		buffer.append("                     \"\n");
		buffer.append("                     order=\"1000\"\n");
		buffer.append("                />\n");

		buffer.append("    </aop:config>\n");

		buffer.append("</beans>\n");

		return buffer;
	}

}
