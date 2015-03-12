package it.polimi.modaclouds.adaptationDesignTime4Cloud.Main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

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
    	AdaptationDesignResult output=builder.createAdaptationModelAndRules("/home/mik/workspace/adaptationDesignTime4Cloud/resource/ResourceContainerExtension.xml",
    			"/home/mik/workspace/adaptationDesignTime4Cloud/resource/Functionality2Tier.xml", 
    			"/home/mik/workspace/adaptationDesignTime4Cloud/resource/performance.xml",
    			"tier", 5);
    	
    	System.out.println(output.getPathToAdaptationModel());
    	
    	
		//print also the rules for testing purpose
    	JAXBContext context;
		try {
			context = JAXBContext.newInstance("it.polimi.modaclouds.qos_models.schema");
	    	Marshaller marshaller=context.createMarshaller();
			marshaller.setProperty("jaxb.formatted.output",Boolean.TRUE);
			OutputStream out = new FileOutputStream( "rules.xml" );
			marshaller.marshal(output.getResponseTimeThresholdRules(),out);

		} catch (JAXBException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//
    }
}
