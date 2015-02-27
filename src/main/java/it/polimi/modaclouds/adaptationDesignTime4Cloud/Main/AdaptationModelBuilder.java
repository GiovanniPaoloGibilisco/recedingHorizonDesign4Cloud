package it.polimi.modaclouds.adaptationDesignTime4Cloud.Main;

import it.polimi.modaclouds.adaptationDesignTime4Cloud.cloudDBAccess.DataHandler;
import it.polimi.modaclouds.adaptationDesignTime4Cloud.exceptions.StaticInputBuildingException;
import it.polimi.modaclouds.adaptationDesignTime4Cloud.model.ApplicationTier;
import it.polimi.modaclouds.adaptationDesignTime4Cloud.model.Container;
import it.polimi.modaclouds.adaptationDesignTime4Cloud.model.Containers;
import it.polimi.modaclouds.adaptationDesignTime4Cloud.model.Functionality;
import it.polimi.modaclouds.adaptationDesignTime4Cloud.model.ObjectFactory;
import it.polimi.modaclouds.adaptationDesignTime4Cloud.model.ResponseTimeThreshold;
import it.polimi.modaclouds.adaptationDesignTime4Cloud.util.GenericXMLHelper;
import it.polimi.modaclouds.qos_models.schema.ResourceContainer;
import it.polimi.modaclouds.qos_models.schema.ResourceModelExtension;
import it.polimi.modaclouds.qos_models.util.XMLHelper;
import it.polimi.modaclouds.resourcemodel.cloud.CloudResource;
import it.polimi.modaclouds.resourcemodel.cloud.Cost;
import it.polimi.modaclouds.resourcemodel.cloud.VirtualHWResource;
import it.polimi.modaclouds.resourcemodel.cloud.VirtualHWResourceType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.eclipse.emf.common.util.EList;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class AdaptationModelBuilder {
	
	
	private DataHandler dbHandler;

	public AdaptationModelBuilder(){
		try {
			this.dbHandler = new DataHandler();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
	
	public AdaptationDesignResult createAdaptationModelAndRules(String space4cloudSolutionPath, String functionalityToTierPath, 
																String space4cloudPerformancePath, String adaptationType ){

		ObjectFactory factory= new ObjectFactory();
		 	
		AdaptationDesignResult output=new AdaptationDesignResult();
		GenericXMLHelper xmlHelp= new GenericXMLHelper(functionalityToTierPath);
		List<Element> mapping=xmlHelp.getElements("tier");
		xmlHelp= new GenericXMLHelper(space4cloudPerformancePath);
		List<Element> performance=xmlHelp.getElements("Seff");
		MonitoringRulesHelper rulesHelper= new MonitoringRulesHelper();
			
		Containers model=factory.createContainers();
		
		try {
			ResourceModelExtension solution = (ResourceModelExtension) XMLHelper
					.deserialize(new FileInputStream(space4cloudSolutionPath),
							ResourceModelExtension.class);
			
			for (ResourceContainer tier : solution.getResourceContainer()) {
							
				ApplicationTier newTier = factory.createApplicationTier();
				
				CloudResource resource=this.dbHandler.getCloudResource(tier.getProvider(), 
						tier.getCloudResource().getServiceName(), 
						tier.getCloudResource().getResourceSizeID());				
				
				newTier.setId(tier.getId());
				newTier.setInitialNumberOfVMs(tier.getCloudResource().getReplicas().getReplicaElement().get(0).getValue());	
							
				//the mapping funztionalities2tier is inserted in the file sent to the SAR at runtime only if the 
				//optimization is performed considering the method as classes
				if(adaptationType.equals("method")){		
					this.setMapping(mapping, newTier, xmlHelp);
				}
				
				//design response time thresholds will be setted depending on the adaptation type (method or tier)
				this.setResponseTimeThresholds(adaptationType, performance, newTier, xmlHelp);
						
				boolean existingContainer=false;
				Container toUpdate=null;
							
				//does the control between container be based just on the capacity?? (region...)
				float capacity= this.getResourceCapacity(resource, 1200);
				
				for(Container c: model.getContainer()){
					if(c.getCapacity()==capacity){

						existingContainer=true;
						toUpdate=c;
					}
				}			
				
				if(!existingContainer){
					Container toAdd= factory.createContainer();
					toAdd.setCapacity(capacity);
					toAdd.setMaxReserved(0);
					toAdd.setOnDemandCost(this.getResourceOnDemandCost(resource, tier.getCloudResource().getLocation().getRegion()));
					toAdd.setReservedCost(this.getResourceReservedCost(resource, tier.getCloudResource().getLocation().getRegion()));
					toAdd.getApplicationTier().add(newTier);
					model.getContainer().add(toAdd);
				}
				else{
					toUpdate.getApplicationTier().add(newTier);
				}		
					
			}
			
			JAXBContext context = JAXBContext.newInstance("it.polimi.modaclouds.adaptationDesignTime4Cloud.model");
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty("jaxb.formatted.output",Boolean.TRUE);
            OutputStream out = new FileOutputStream( "outputModelExample.xml" );
			marshaller.marshal(model,out);
			
			
			context=JAXBContext.newInstance("it.polimi.modaclouds.qos_models.schema");
			marshaller=context.createMarshaller();
			marshaller.setProperty("jaxb.formatted.output",Boolean.TRUE);
            out = new FileOutputStream( "rules.xml" );
			marshaller.marshal(rulesHelper.createResponseTimeThresholdRules(model),out);	
			
			out.close();
		} catch ( JAXBException | SAXException | StaticInputBuildingException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
		return null;
	}
	
	
	private void setMapping(List<Element> mapping, ApplicationTier newTier, GenericXMLHelper xmlHelp){
		
		for(Element e: mapping){
			if(e.getAttribute("id").equals(newTier.getId())){
				List<Element> functionalities=xmlHelp.getElements(e, "functionality");
				for(Element f : functionalities){
					Functionality toAdd=new Functionality();
					toAdd.setId(f.getAttribute("id"));
					
					newTier.getFunctionality().add(toAdd);
				}
			}
		}
		
	}
	
	//calculating the design response time thresholds for tier or for method
	private void setResponseTimeThresholds(String adaptationType, List<Element> performance, ApplicationTier newTier, GenericXMLHelper xmlHelp){
		
		if(adaptationType.equals("method")){
			
		}else if(adaptationType.equals("tier")){
			
			List<float[]> allWeigthedRT= new ArrayList<float[]>();
			List<float[]> allTHR=new ArrayList<float[]>();
			
			for(Element e: performance){
				for(Functionality f: newTier.getFunctionality()){
					if(e.getAttribute("id").equals(f.getId())){
						float[] weigthedResponseTime=new float[24];
						float[] throughput=new float[24];
						
						List<Element> thr=xmlHelp.getElements(e, "throughput");
						
						for(Element t: thr){
							throughput[Integer.parseInt(t.getAttribute("hour"))]=Float.parseFloat(t.getAttribute("value"));
						}
						
						List<Element> rt=xmlHelp.getElements(e, "avgRT");
						
						for(Element r: rt){
							weigthedResponseTime[Integer.parseInt(r.getAttribute("hour"))]=Float.parseFloat(r.getAttribute("value"));
						}

						for(int i=0; i<24; i++){
							weigthedResponseTime[i]=weigthedResponseTime[i]*throughput[i];
						}
						
						allWeigthedRT.add(weigthedResponseTime);
						allTHR.add(throughput);
					}

				}
			}
			
			float[] thresholds=new float[24];
			
			for(int i=0; i<24 ; i++){
				
				float num=0;
				
				for(float[] temp: allWeigthedRT){
					num=num+temp[i];
				}
				
				float den=0;
				
				for(float[] temp: allTHR){
					den=den+temp[i];
				}
				
				thresholds[i]=num/den;
				
				ResponseTimeThreshold toAdd=new ResponseTimeThreshold();
				toAdd.setHour(i);
				toAdd.setValue(num/den);
				newTier.getResponseTimeThreshold().add(toAdd);
				
			}
			
		}
		
	}
	
	private float getResourceCapacity(CloudResource resource, float speedNorm) throws StaticInputBuildingException{
		
		float toReturn=0;
		
		for (VirtualHWResource virtualResource : resource.getComposedOf()) {
			
			if (virtualResource.getType().equals(VirtualHWResourceType.CPU)) {

				toReturn= (float) (virtualResource
						.getNumberOfReplicas()
						* virtualResource.getProcessingRate()
						/ speedNorm);
			}
		}
		if(toReturn==0)
			throw new StaticInputBuildingException("Error calculating execution capacity: capacity cannot be equal to 0. Check the resource retrived from the CloudDB");	
		else
			return toReturn;
		
	}
	
	private float getResourceOnDemandCost(CloudResource resource, String region){
		
		EList<Cost> cl = resource.getHasCost();
		float toReturn=0;
		
		for (Cost c : cl) {
			
			if (c.getRegion().equals(region) && c.getDescription().contains("On-Demand")) {
					
					if (toReturn == 0)
						toReturn=(float) c.getValue();
					else if (c.getValue() < toReturn)
						toReturn=(float) c.getValue();			
			}

		}
		
		return toReturn;
		
	}
	
	private float getResourceReservedCost(CloudResource resource, String region){
		EList<Cost> cl = resource.getHasCost();
		float toReturn=0;

		for (Cost c : cl) {
			if (c.getRegion().equals(region) && c.getDescription().contains("Reserved 3year")) {

					if (toReturn == 0)
						toReturn=(float) c.getValue();
					
					else if (c.getValue() < toReturn)
						toReturn=(float) c.getValue();
				
			}

		}
		
		return toReturn;
	}
	

}
