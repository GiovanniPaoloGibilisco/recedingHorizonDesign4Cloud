package it.polimi.modaclouds.adaptationDesignTime4Cloud.Main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import it.polimi.modaclouds.adaptationDesignTime4Cloud.model.Containers;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;
import it.polimi.modaclouds.qos_models.schema.ResourceModelExtension;
import it.polimi.modaclouds.qos_models.util.XMLHelper;

/**
 * Hello world!
 *
 */
public class TestMain 
{
    public static void main( String[] args )
    {
    	
    	
    	
    	AdaptationModelBuilder builder= new AdaptationModelBuilder();
    	builder.createAdaptationModelAndRules("/home/mik/workspace/adaptationDesignTime4Cloud/resource/solution.xml",
    			"/home/mik/workspace/adaptationDesignTime4Cloud/resource/mapping.xml", 
    			"/home/mik/workspace/adaptationDesignTime4Cloud/resource/performance.xml");
    	
    }
}
