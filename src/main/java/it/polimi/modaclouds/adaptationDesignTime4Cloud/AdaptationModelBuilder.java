package it.polimi.modaclouds.adaptationDesignTime4Cloud;

import it.polimi.modaclouds.adaptationDesignTime4Cloud.cloudDBAccess.DataHandler;
import it.polimi.modaclouds.adaptationDesignTime4Cloud.exceptions.StaticInputBuildingException;
import it.polimi.modaclouds.adaptationDesignTime4Cloud.functionality2tier.Functionality2Tier;
import it.polimi.modaclouds.adaptationDesignTime4Cloud.util.ConfigManager;
import it.polimi.modaclouds.qos_models.schema.ApplicationTier;
import it.polimi.modaclouds.qos_models.schema.Container;
import it.polimi.modaclouds.qos_models.schema.Containers;
import it.polimi.modaclouds.qos_models.schema.Functionality;
import it.polimi.modaclouds.qos_models.schema.HourValueType;
import it.polimi.modaclouds.qos_models.schema.ObjectFactory;
import it.polimi.modaclouds.qos_models.schema.Performance;
import it.polimi.modaclouds.qos_models.schema.Performance.Seffs.Seff;
import it.polimi.modaclouds.qos_models.schema.Performance.Tiers.Tier;
import it.polimi.modaclouds.qos_models.schema.ResourceContainer;
import it.polimi.modaclouds.qos_models.schema.ResourceModelExtension;
import it.polimi.modaclouds.qos_models.schema.ResponseTimeThreshold;
import it.polimi.modaclouds.qos_models.util.XMLHelper;
import it.polimi.modaclouds.resourcemodel.cloud.CloudResource;
import it.polimi.modaclouds.resourcemodel.cloud.Cost;
import it.polimi.modaclouds.resourcemodel.cloud.VirtualHWResource;
import it.polimi.modaclouds.resourcemodel.cloud.VirtualHWResourceType;
import it.polimi.tower4clouds.rules.MonitoringRules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eclipse.emf.common.util.EList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class AdaptationModelBuilder {
	
	private static final Logger logger = LoggerFactory.getLogger(AdaptationModelBuilder.class);
	
	
	private DataHandler dbHandler;

	public AdaptationModelBuilder(String dbConnectionFile) {
		try {
			DataHandler.initDatabaseConfiguration(dbConnectionFile);
			
			this.dbHandler = new DataHandler();
		} catch (Exception e1) {
			logger.error("Error while initializing the database.", e1);
		}
	}
	
	public static Functionality2Tier parseFunctionalityToTierFile(String functionalityToTierPath) throws Exception {
		try {
			JAXBContext jc = JAXBContext.newInstance(Functionality2Tier.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			File xml = ConfigManager.getPathToFile(functionalityToTierPath).toFile();
			return (Functionality2Tier) unmarshaller.unmarshal(xml);
		} catch (JAXBException e) {
			throw new Exception("Error while loading the functionality2tier file.", e);
		}
	}
	
	public static Performance parsePerformancesFile(String space4cloudPerformancePath) throws Exception {
		try {
			JAXBContext jc = JAXBContext.newInstance(Performance.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			File xml = ConfigManager.getPathToFile(space4cloudPerformancePath).toFile();
			return (Performance) unmarshaller.unmarshal(xml);
		} catch (JAXBException e) {
			throw new Exception("Error while loading the performances file.", e);
		}
	}
	
	public List<File> createAdaptationModelAndRules(String basePath, String functionalityToTierPath, 
			int optimizationWindowLenght, int timestepDuration, String suffix) throws Exception {
		return createAdaptationModelAndRules(basePath, null, functionalityToTierPath, null, optimizationWindowLenght, timestepDuration, suffix);
	}
	
	public static final int DEFAULT_OPTIMIZATION_WINDOW_LENGTH = 5;
	public static final int DEFAULT_TIMESTEP_DURATION = 5;
	
	public static final String SOLUTION_FILE_NAME = "solution%s.xml";
	public static final String PERFORMANCE_FILE_NAME = "performance%s.xml";
	public static final String FUNCTIONALITY2TIER_FILE_NAME = "functionalityChain2Tier.xml";
	
	public List<File> createAdaptationModelAndRules(String basePath, String space4cloudSolutionPath, String functionalityToTierPath, 
																String space4cloudPerformancePath, int optimizationWindowLenght, int timestepDuration, String suffix) throws Exception {
		{
			if (basePath == null || basePath.trim().length() == 0)
				throw new RuntimeException("The base path parameter is null or not valid!");
			if (suffix == null || suffix.trim().length() == 0)
				throw new RuntimeException("The suffix parameter is null or not valid!");
			
			File basePathFolder = ConfigManager.getPathToFile(basePath).toFile();
			if (!basePathFolder.exists() || !basePathFolder.isDirectory())
				throw new RuntimeException("The base path (" + basePath + ") doesn't refer to a correct folder.");
			
			if (space4cloudSolutionPath == null)
				space4cloudSolutionPath = Paths.get(basePathFolder.toString(), String.format(SOLUTION_FILE_NAME, suffix)).toString();
			
			if (space4cloudPerformancePath == null)
				space4cloudPerformancePath = Paths.get(basePathFolder.toString(), String.format(PERFORMANCE_FILE_NAME, suffix)).toString();
			
			if (functionalityToTierPath == null)
				functionalityToTierPath = Paths.get(basePathFolder.getParent(), FUNCTIONALITY2TIER_FILE_NAME).toString();
			
			File f = new File(space4cloudPerformancePath);
			if (!f.exists() && !f.isFile())
				throw new RuntimeException("Performance file not found!");
			
			f = new File(space4cloudSolutionPath);
			if (!f.exists() && !f.isFile())
				throw new RuntimeException("Solution file not found!");
			
			f = new File(functionalityToTierPath);
			if (!f.exists() && !f.isFile())
				throw new RuntimeException("Functionality2Tier file not found!");
			
			if (optimizationWindowLenght < 0)
				optimizationWindowLenght = DEFAULT_OPTIMIZATION_WINDOW_LENGTH;
			
			if (timestepDuration < 0)
				timestepDuration = DEFAULT_TIMESTEP_DURATION;
		}
		
		
		logger.info("Reading the input files and preparing the results...");
		
		ObjectFactory factory= new ObjectFactory();
		 	
		List<it.polimi.modaclouds.adaptationDesignTime4Cloud.functionality2tier.Tier> tiersInFunctionalityToTier = parseFunctionalityToTierFile(functionalityToTierPath).getTier();
		
		Performance p = parsePerformancesFile(space4cloudPerformancePath);
		List<Seff> performance = p.getSeffs().getSeff();
		List<Tier> tiersPerformance = p.getTiers().getTier();
		
		MonitoringRulesHelper rulesHelper= new MonitoringRulesHelper();
		
		Containers model=factory.createContainers();
		model.setSpeedNorm(1200);
		model.setOptimizationWindowsLenght(optimizationWindowLenght);
		model.setTimestepDuration(timestepDuration);
		
		try {

			
			ResourceModelExtension solution = (ResourceModelExtension) XMLHelper
					.deserialize(new FileInputStream(space4cloudSolutionPath),
							ResourceModelExtension.class);
			
			for (ResourceContainer tier : solution.getResourceContainer()) {
							
				ApplicationTier newTier = factory.createApplicationTier();
				Map<String, String> functionalityNameToId = new HashMap<String, String>();
				
				CloudResource resource=this.dbHandler.getCloudResource(tier.getProvider(), 
						tier.getCloudElement().getServiceName(), 
						tier.getCloudElement().getResourceSizeID());
				
				for (Tier t : tiersPerformance){
					if (t.getId().equals(tier.getId())) {
						newTier.setId(t.getName());
					}
				}
				
							
					
				for (it.polimi.modaclouds.adaptationDesignTime4Cloud.functionality2tier.Tier t : tiersInFunctionalityToTier) {
					if (t.getId().equals(tier.getId())) {
						List<it.polimi.modaclouds.adaptationDesignTime4Cloud.functionality2tier.Functionality> functionalities = t.getFunctionality();
						for (it.polimi.modaclouds.adaptationDesignTime4Cloud.functionality2tier.Functionality f : functionalities) {
							Functionality toAdd=factory.createFunctionality();
							toAdd.setId(f.getName());
							functionalityNameToId.put(f.getName(), f.getId());
							newTier.getFunctionality().add(toAdd);
						}
					}
				}
					
				
				
				
				this.setResponseTimeThresholds(performance, newTier, functionalityNameToId);
						
				boolean existingContainer=false;
				Container toUpdate=null;
							
				double capacity= this.getResourceCapacity(resource, model.getSpeedNorm());
				
				for(Container c: model.getContainer()){
					if(c.getCapacity()==capacity){

						existingContainer=true;
						toUpdate=c;
					}
				}			
				
				if(!existingContainer){
					Container toAdd= factory.createContainer();
					toAdd.setId("capacity"+capacity+"Container");
					toAdd.setCapacity(capacity);
					toAdd.setProcessingRate(this.getProcessingRate(resource));
					toAdd.setNCore(this.getNCore(resource));
					toAdd.setMaxReserved(0);
					toAdd.setOnDemandCost(this.getResourceOnDemandCost(resource, tier.getCloudElement().getLocation().getRegion()));
					toAdd.setReservedCost(this.getResourceReservedCost(resource, tier.getCloudElement().getLocation().getRegion()));
					toAdd.setVmType(tier.getCloudElement().getResourceSizeID());
					toAdd.getApplicationTier().add(newTier);
					model.getContainer().add(toAdd);
				}
				else{
					toUpdate.getApplicationTier().add(newTier);
				}		
					
			}
			
			List<File> res = new ArrayList<>();
			
			res.add(createConfigFile(
					model,
					Paths.get(basePath, "S4COpsConfig" + suffix + ".xml")));
			
			res.add(createRulesFile(
					rulesHelper.createResponseTimeThresholdRules(model, timestepDuration),
					Paths.get(basePath, "lowerSLARules" + suffix + ".xml")));
			
			return res;
		} catch ( JAXBException | SAXException | StaticInputBuildingException | IOException e) {
			throw new Exception("Error while producing the results.", e);
		} 

	}
	
	public static File createConfigFile(Containers containers, Path p) throws Exception {
		try {
			File f = p.toFile();
	        try (OutputStream out = new FileOutputStream(f)) {
	        	XMLHelper.serialize(containers, out);
	        	logger.info("Config file {} created!", f.toString());
	        }
	        return f;
		} catch (Exception e) {
			throw new Exception("Error while writing the config file.", e);
		}
	}
	
	public static File createRulesFile(MonitoringRules rules, Path p) throws Exception {
		try {
			File f = p.toFile();
	        try (OutputStream out = new FileOutputStream(f)) {
	        	XMLHelper.serialize(rules, out);
	        	logger.info("Rules file {} created!", f.toString());
	        }
	        return f;
		} catch (Exception e) {
			throw new Exception("Error while writing the rules file.", e);
		}
	}
	
	private void setResponseTimeThresholds(List<Seff> performance, ApplicationTier newTier, Map<String, String> functionalityNameToId) {
		List<float[]> sumWeigthedRT= new ArrayList<float[]>();
		List<float[]> sumTHR=new ArrayList<float[]>();
		
		for(Seff e : performance){
			for(Functionality f: newTier.getFunctionality()){
				if(e.getId().equals(functionalityNameToId.get(f.getId()))) {
					float[] weigthedResponseTime=new float[24];
					float[] throughput=new float[24];
					
					List<HourValueType> thr = e.getThroughput();
					List<HourValueType> rt = e.getAvgRT();

					
					for(HourValueType t: thr){
						throughput[t.getHour()] = t.getValue();
					}
					
					
					for(HourValueType r: rt){
						weigthedResponseTime[r.getHour()] = r.getValue();
					}

					for(int i=0; i<24; i++){
						weigthedResponseTime[i]=weigthedResponseTime[i]*throughput[i];
					}
					
					sumWeigthedRT.add(weigthedResponseTime);
					sumTHR.add(throughput);
				}

			}
		}

		float[] thresholds=new float[24];
		
		for(int i=0; i<24 ; i++){
			
			float num=0;
			
			for(float[] temp: sumWeigthedRT){
				num=num+temp[i];
			}
			
			float den=0;
			
			for(float[] temp: sumTHR){
				den=den+temp[i];
			}
			
			thresholds[i]=num/den;
			
			ResponseTimeThreshold toAdd=new ResponseTimeThreshold();
			toAdd.setHour(i);
			toAdd.setValue(num/den);
			newTier.getResponseTimeThreshold().add(toAdd);
			
		}
	}
	
	private float getResourceCapacity(CloudResource resource, double speedNorm) throws StaticInputBuildingException{
		
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
	
	private int getNCore(CloudResource resource){
		for (VirtualHWResource virtualResource : resource.getComposedOf()) {
			
			if (virtualResource.getType().equals(VirtualHWResourceType.CPU)) {
				return virtualResource.getNumberOfReplicas();

			}
		}
		
		
		return 0;
	} 
	
	private double getProcessingRate(CloudResource resource){
		for (VirtualHWResource virtualResource : resource.getComposedOf()) {
			
			if (virtualResource.getType().equals(VirtualHWResourceType.CPU)) {
				return virtualResource.getProcessingRate();
			}
		}
		
		return 0;
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
