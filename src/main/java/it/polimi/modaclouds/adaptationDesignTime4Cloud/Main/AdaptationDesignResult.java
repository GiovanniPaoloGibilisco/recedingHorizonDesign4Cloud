package it.polimi.modaclouds.adaptationDesignTime4Cloud.Main;
import java.nio.file.Path;
import java.util.List;

import it.polimi.modaclouds.qos_models.monitoring_rules.*;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;
import it.polimi.modaclouds.qos_models.schema.MonitoringRules;

public class AdaptationDesignResult {
	
	private List<MonitoringRule> responseTimeThresholdRules;
	
	private Path pathToAdaptationModel;
	
	public AdaptationDesignResult(){
		
	}

	public List<MonitoringRule> getResponseTimeThresholdRules() {
		return responseTimeThresholdRules;
	}

	public void setResponseTimeThresholdRules(
			List<MonitoringRule> responseTimeThresholdRules) {
		this.responseTimeThresholdRules = responseTimeThresholdRules;
	}

	public Path getPathToAdaptationModel() {
		return pathToAdaptationModel;
	}

	public void setPathToAdaptationModel(Path pathToAdaptationModel) {
		this.pathToAdaptationModel = pathToAdaptationModel;
	}
	

}
