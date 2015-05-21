package it.polimi.modaclouds.adaptationDesignTime4Cloud.Main;

/**
 * Hello world!
 *
 */
public class TestMain 
{
    public static void main( String[] args )
    {
    	
    	
    	
    	AdaptationModelBuilder builder= new AdaptationModelBuilder();
    	builder.createAdaptationModelAndRules("resources/resourceExtention_example.xml",
    			"resources/functionalityChain2Tier_example.xml", 
    			"resources/performance_example.xml",
    			5, 5);
    	

    }
}
