package it.polimi.modaclouds.adaptationDesignTime4Cloud.Main;

import it.polimi.modaclouds.adaptationDesignTime4Cloud.cloudDBAccess.DataHandler;
import it.polimi.modaclouds.adaptationDesignTime4Cloud.exceptions.StaticInputBuildingException;
import it.polimi.modaclouds.adaptationDesignTime4Cloud.model.ApplicationTier;
import it.polimi.modaclouds.adaptationDesignTime4Cloud.model.Container;
import it.polimi.modaclouds.adaptationDesignTime4Cloud.model.Containers;
import it.polimi.modaclouds.adaptationDesignTime4Cloud.model.ObjectFactory;
import it.polimi.modaclouds.qos_models.schema.ResourceContainer;
import it.polimi.modaclouds.qos_models.schema.ResourceModelExtension;
import it.polimi.modaclouds.qos_models.util.XMLHelper;
import it.polimi.modaclouds.resourcemodel.cloud.CloudResource;
import it.polimi.modaclouds.resourcemodel.cloud.Cost;
import it.polimi.modaclouds.resourcemodel.cloud.VirtualHWResource;
import it.polimi.modaclouds.resourcemodel.cloud.VirtualHWResourceType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.eclipse.emf.common.util.EList;
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
	
	public AdaptationDesignResult createAdaptationModelAndRules(String space4cloudSolutionPath, String functionalityToTierPath, String space4cloudPerformancePath){

		ObjectFactory factory= new ObjectFactory();
				
		Containers model=factory.createContainers();
		
		try {
			ResourceModelExtension solution = (ResourceModelExtension) XMLHelper
					.deserialize(new FileInputStream(space4cloudSolutionPath),
							ResourceModelExtension.class);

			for (ResourceContainer tier : solution.getResourceContainer()) {
				
				
				ApplicationTier newTier = factory.createApplicationTier();
				
				CloudResource temp=this.dbHandler.getCloudResource(tier.getProvider(), 
						tier.getCloudResource().getServiceName(), 
						tier.getCloudResource().getResourceSizeID());
				
				
				newTier.setId(tier.getId());
				newTier.setInitialNumberOfVMs(tier.getCloudResource().getReplicas().getReplicaElement().get(0).getValue());
				newTier.setResponseTimeThreshold(0);
				
				float capacity= this.getResourceCapacity(temp, 1200);
				
				boolean existingContainer=false;
				Container toUpdate=null;
				
				for(Container c: model.getContainer()){
					if(c.getCapacity()==capacity){

						existingContainer=true;
						toUpdate=c;
					}
				}
				
				if(existingContainer==false){
					Container toAdd= factory.createContainer();
					toAdd.setCapacity(capacity);
					toAdd.setMaxReserved(0);
					toAdd.setOnDemandCost(this.getResourceOnDemandCost(temp, tier.getCloudResource().getLocation().getRegion()));
					toAdd.setReservedCost(this.getResourceReservedCost(temp, tier.getCloudResource().getLocation().getRegion()));
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
			marshaller.marshal(model,System.out);
		} catch (FileNotFoundException | JAXBException | SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StaticInputBuildingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return null;
	}
	
	private float getResourceCapacity(CloudResource resource, float speedNorm) throws StaticInputBuildingException{
		
		float toReturn=0;
		
		for (VirtualHWResource virtualResource : resource.getComposedOf()) {
			
			//il parametro CPU in realtà andrebbe letto da soluzione amazon ovvero è il serviceType che già memorizziamo e che può variare ad esempio storage ecc
			//per ora teniamo così...
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
			
			// di tutte le opzioni per l'ondemand della macchina
			// individuata ipotizzo di selezionare la più economica
			// nel caso in cui l'opzione on demand fosse unica non c è
			// bisogno di questo frammento nel ciclo e l'individuazione
			// del costo può essere effettuata in una singola
			// chiamata all esterno
			//this.getResource().getRegion()
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
						
				// di tutte le opzioni per il reserved della macchina
				// individuata ipotizzo di selezionare la più economica tra
				// quelle a 3 anni
					if (toReturn == 0)
						toReturn=(float) c.getValue();
					
					else if (c.getValue() < toReturn)
						toReturn=(float) c.getValue();
				
			}

		}
		
		return toReturn;
	}
	

}
