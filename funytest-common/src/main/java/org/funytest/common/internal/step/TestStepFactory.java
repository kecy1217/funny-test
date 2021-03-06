package org.funytest.common.internal.step;

import java.util.HashMap;
import java.util.Map;

import org.dom4j.Element;
import org.funytest.common.internal.IConfiguration;
import org.funytest.common.model.teststep.ITestStep;
import org.funytest.common.utils.Constant;

public class TestStepFactory {
	
	public static Map<String, ITestStepBuilder> builderMap;
	
	static{
		builderMap = new HashMap<String, ITestStepBuilder>();
		builderMap.put(Constant.STEP_TYPE_INIT, new InitTestStepBuilder());
		builderMap.put(Constant.STEP_TYPE_METHOD, new MethodTestStepBuilder());
		builderMap.put(Constant.STEP_TYPE_CHECK, new CheckTableStepBuilder());
		builderMap.put(Constant.STEP_TYPE_HTTP, new HttpTestStepBuilder());
	}

	/**
	 * 根据节点内容，提炼出对应的ITestStep，采用策略设计模式
	 * @param name  名称
	 * @param element 对应上述节点
	 * @return
	 */
	public ITestStep buildStep(String name, Element element, IConfiguration config){
		
		ITestStepBuilder builder = this.builderMap.get(name);
		
		ITestStep step = builder != null ? builder.buildStep(name, element, config) : null;
				
		return step;
	}
	
	/**
	 * 添加结点构造器，对外预留扩展点
	 * @param stepName step结点名称
	 * @param builder  step结点的工厂实例
	 */
	public void addBuilder(String stepName, ITestStepBuilder builder){
		if (builderMap == null) builderMap = new HashMap<String, ITestStepBuilder>();
		builderMap.put(stepName, builder);
	}
	
	public Map<String, ITestStepBuilder> getBuilderMap() {
		return builderMap;
	}

	public void setBuilderMap(Map<String, ITestStepBuilder> builderMap) {
		this.builderMap = builderMap;
	}
}
