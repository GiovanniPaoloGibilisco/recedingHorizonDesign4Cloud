package it.polimi.modaclouds.adaptationDesignTime4Cloud.Main;

/**
 * Hello world!
 *
 */
public class TestMain 
{
    public static void main( String[] args )
    {
    	
    	
    	
    	AdaptationModelBuilder builder= new AdaptationModelBuilder("/home/micheleguerriero/workspace/modaclouds-DesignToRuntimeConnector/resources/localDBConnection.properties");
    	builder.createAdaptationModelAndRules(
    			"/home/micheleguerriero/workspace/modaclouds-DesignToRuntimeConnector/resources",
    			"/home/micheleguerriero/workspace/modaclouds-DesignToRuntimeConnector/resources/solutionAmazon.xml",
    			"/home/micheleguerriero/workspace/modaclouds-DesignToRuntimeConnector/resources/functionalityChain2Tier.xml", 
    			"/home/micheleguerriero/workspace/modaclouds-DesignToRuntimeConnector/resources/performanceAmazon.xml",
    			5, 5, "Amazon");
    	

    }
}
