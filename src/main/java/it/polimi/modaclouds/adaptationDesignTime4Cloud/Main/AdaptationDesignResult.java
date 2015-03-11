package it.polimi.modaclouds.adaptationDesignTime4Cloud.Main;
import java.nio.file.Path;
import java.util.List;

import it.polimi.modaclouds.qos_models.monitoring_rules.*;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;
import it.polimi.modaclouds.qos_models.schema.MonitoringRules;

public class AdaptationDesignResult {
	
	private MonitoringRules responseTimeThresholdRules;
	
	private String pathToAdaptationModel;
	
	public AdaptationDesignResult(){
		
	}

	public MonitoringRules getResponseTimeThresholdRules() {
		return responseTimeThresholdRules;
	}

	public void setResponseTimeThresholdRules(
			MonitoringRules responseTimeThresholdRules) {
		this.responseTimeThresholdRules = responseTimeThresholdRules;
	}

	public String getPathToAdaptationModel() {
		return pathToAdaptationModel;
	}

	public void setPathToAdaptationModel(String pathToAdaptationModel) {
		this.pathToAdaptationModel = pathToAdaptationModel;
	}
	

}
