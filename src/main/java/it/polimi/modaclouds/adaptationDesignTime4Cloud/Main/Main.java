package it.polimi.modaclouds.adaptationDesignTime4Cloud.Main;

import it.polimi.modaclouds.qos_models.monitoring_rules.*;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;

/**
 * Hello world!
 *
 */
public class Main 
{
    public static void main( String[] args )
    {
    	AdaptationModelBuilder builder= new AdaptationModelBuilder();
    	builder.createAdaptationModelAndRules("/home/mik/workspace/adaptationDesignTime4Cloud/resource/solution.xml",
    			"/home/mik/workspace/adaptationDesignTime4Cloud/resource/mapping.xml", 
    			"/home/mik/workspace/adaptationDesignTime4Cloud/resource/performance.xml");
    	
    }
}
