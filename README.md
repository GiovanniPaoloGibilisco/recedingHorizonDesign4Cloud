# recedingHorizonDesign4Cloud
Design time component of the redeingHorizonScaling4Cloud adaptation engine (https://github.com/deib-polimi/modaclouds-recedingHorizonScaling4Cloud)

This Java library has been designed in order to be part of SPACE4Clouds ( https://github.com/deib-polimi/modaclouds-space4cloud ) within the MODAClouds project. 
Its main tasks are the following:

1) to generate an xml file modeling the design time information relevant to the runtime adaptation mechanism
	
2) to generate rules for the response time thresholds overcoming. Thesholds specified in these rule are derived from SPACE4Clouds performance analysis. 
	
The library takes as inputs the following three files:

1) functionalityChain2Tier.xml: it contains a mapping between each application tier and the set of functionalities it contains. For each functionality also the set of external calls is reported in order to derive proper response time thresholds.
	
2) resource_model_extention.xml: it is one of SPACE4CLouds output file and contains information about the optimized deployment solution ( https://github.com/deib-polimi/modaclouds-qos-models/blob/master/metamodels/s4cextension/resource_model_extension.xsd )
	
3) performance.xml: it is one of SPACE4CLouds output and contains performance information derived from SPACE4Clouds analysis ( https://github.com/deib-polimi/modaclouds-qos-models/blob/master/metamodels/s4cextension/performances.xsd ).
	
The outputs are an xml file to be passed at runtime to the adaptation mechanism and an xml file with a set of monitoring rules that need to be installed at runtime.