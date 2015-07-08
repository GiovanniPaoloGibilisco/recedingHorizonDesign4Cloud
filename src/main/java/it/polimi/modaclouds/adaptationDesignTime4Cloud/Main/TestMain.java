package it.polimi.modaclouds.adaptationDesignTime4Cloud.Main;

/**
 * Hello world!
 *
 */
public class TestMain 
{
    public static void main( String[] args )
    {
    	
    	
    	
    	AdaptationModelBuilder builder= new AdaptationModelBuilder("/Users/ft/Development/workspace-s4c-runtime/modaclouds-models/MiCforJSS/localDBConnection.properties");
    	builder.createAdaptationModelAndRules(
    			"/Users/ft/Development/workspace-s4c-runtime/modaclouds-models/MiCforJSS/space4cloud",
    			"/Users/ft/Development/workspace-s4c-runtime/modaclouds-models/MiCforJSS/space4cloud/solutionAmazon.xml",
    			"/Users/ft/Desktop/tmp/trash/functionalityChain2Tier_example.xml", 
    			"/Users/ft/Development/workspace-s4c-runtime/modaclouds-models/MiCforJSS/space4cloud/performanceAmazon.xml",
    			5, 5, "Test");
    	

    }
}
