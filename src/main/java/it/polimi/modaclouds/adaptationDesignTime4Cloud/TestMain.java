package it.polimi.modaclouds.adaptationDesignTime4Cloud;

import java.util.HashMap;
import java.util.Map;

/**
 * Hello world!
 *
 */
public class TestMain 
{
    public static void main( String[] args ) throws Exception
    {
    	Map<String, Double> functionalityDemands=new HashMap<String,Double>();
    	
    	functionalityDemands.put("getPage", 2.0);
    	
    	
    	AdaptationModelBuilder builder= new AdaptationModelBuilder("/home/micheleguerriero/workspace/modaclouds-DesignToRuntimeConnector/src/main/resources/localDBConnection.properties");
    	builder.createAdaptationModelAndRules(
    			"/home/micheleguerriero/workspace/modaclouds-DesignToRuntimeConnector/src/main/resources",
    			"/home/micheleguerriero/workspace/modaclouds-DesignToRuntimeConnector/src/main/resources/solutionAmazon.xml",
    			"/home/micheleguerriero/workspace/modaclouds-DesignToRuntimeConnector/src/main/resources/functionalityChain2Tier.xml", 
    			"/home/micheleguerriero/workspace/modaclouds-DesignToRuntimeConnector/src/main/resources/performanceAmazon.xml",
    			5, 5, "Amazon", functionalityDemands);
    	

    }
}
