package it.polimi.modaclouds.adaptationDesignTime4Cloud.Main;


import it.polimi.modaclouds.adaptationDesignTime4Cloud.model.ApplicationTier;
import it.polimi.modaclouds.adaptationDesignTime4Cloud.model.Container;
import it.polimi.modaclouds.adaptationDesignTime4Cloud.model.Containers;
import it.polimi.modaclouds.qos_models.schema.Condition;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;
import it.polimi.modaclouds.qos_models.schema.MonitoringRules;
import it.polimi.modaclouds.qos_models.schema.CollectedMetric;
import it.polimi.modaclouds.qos_models.schema.MonitoredTarget;
import it.polimi.modaclouds.qos_models.schema.Action;
import it.polimi.modaclouds.qos_models.schema.MonitoringMetricAggregation;
import it.polimi.modaclouds.qos_models.schema.Parameter;
import it.polimi.modaclouds.qos_models.schema.ObjectFactory;


public class MonitoringRulesHelper {
	
	public MonitoringRulesHelper(){
		
	}
	
	public MonitoringRules createResponseTimeThresholdRules(Containers model, int timestepDuration){
		
		
		ObjectFactory factory= new ObjectFactory();
		MonitoringRules toReturn= factory.createMonitoringRules();
		MonitoringRule rule;
		MonitoredTarget target;
		Action action;
		CollectedMetric collectedMetric;
		Condition condition;
		MonitoringMetricAggregation aggregation;
		Parameter tempParam;
		
			for(Container c: model.getContainer()){
				for(ApplicationTier t: c.getApplicationTier()){
					
					if(t.getFunctionality().size()>0){
					
						float threshold= t.getResponseTimeThreshold().get(0).getValue();
						
						rule= factory.createMonitoringRule();
						rule.setMonitoredTargets(factory.createMonitoredTargets());
						rule.setId("respTimeThreshold_"+t.getId());
						rule.setTimeStep(Integer.toString(timestepDuration*60));
						rule.setTimeWindow(Integer.toString(timestepDuration*60));
						
						target=factory.createMonitoredTarget();
						target.setClazz("VM");
						target.setType(t.getId());
							
						rule.getMonitoredTargets().getMonitoredTargets().add(target);
						
						
						collectedMetric=factory.createCollectedMetric();
						collectedMetric.setMetricName("ResponseTime");
						tempParam=factory.createParameter();
						tempParam.setName("samplingProbability");
						tempParam.setValue("1");
						collectedMetric.getParameters().add(tempParam);
						
						rule.setCollectedMetric(collectedMetric);
						
						aggregation=factory.createMonitoringMetricAggregation();
						aggregation.setAggregateFunction("Average");
						aggregation.setGroupingClass("VM");
						
						rule.setMetricAggregation(aggregation);
						
						
						condition=factory.createCondition();
	
						condition.setValue("METRIC &gt; "+threshold);
						
						rule.setCondition(condition);
						
						action=factory.createAction();
						action.setName("OutputMetric");
						
		
						
						tempParam=factory.createParameter();
						tempParam.setName("metric");
						tempParam.setValue("ResponseTimeOverThreshold");
						action.getParameters().add(tempParam);
						
						tempParam=factory.createParameter();
						tempParam.setName("value");
						tempParam.setValue("METRIC");
						action.getParameters().add(tempParam);
						
						tempParam=factory.createParameter();
						tempParam.setName("resourceId");
						tempParam.setValue("ID");
						action.getParameters().add(tempParam);
						
						rule.setActions(factory.createActions());
						
						rule.getActions().getActions().add(action);
		
						toReturn.getMonitoringRules().add(rule);
					
					}
				}
			}
		
	
		return toReturn;
	}

}


