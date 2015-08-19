package it.polimi.modaclouds.adaptationDesignTime4Cloud.Main;

/**
 * Hello world!
 *
 */
public class TestMain 
{
    public static void main( String[] args )
    {
    	
    	
    	
    	AdaptationModelBuilder builder= new AdaptationModelBuilder("/home/micheleguerriero/workspace/adaptationDesignTime4Cloud/resources/localDBConnection.properties");
    	builder.createAdaptationModelAndRules(
    			"/home/micheleguerriero/workspace/adaptationDesignTime4Cloud/resources",
    			"/home/micheleguerriero/workspace/adaptationDesignTime4Cloud/resources/solutionTotal.xml",
    			"/home/micheleguerriero/workspace/adaptationDesignTime4Cloud/resources/functionalityChain2Tier_example.xml", 
    			"/home/micheleguerriero/workspace/adaptationDesignTime4Cloud/resources/performanceAmazon.xml",
    			5, 5, "Test");
    	

    }
}
