package de.binformed.platform.action.executer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.InvalidQNameException;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.binformed.platform.activemq.Sender;
import de.binformed.platform.model.BInformedModel;



public class SetWebFlag extends ActionExecuterAbstractBase {
	
	public final static String NAME = "set-web-flag";
	public final static String PARAM_ACTIVE = "active";
	
	private static Log logger = LogFactory.getLog(SetWebFlag.class);
	
	/** The NodeService to be used by the bean */
	protected NodeService nodeService;
	
	/**
	* @param nodeService The NodeService to set.
	*/
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		paramList.add(
		         new ParameterDefinitionImpl(               // Create a new parameter definition to add to the list
		            PARAM_ACTIVE,                           // The name used to identify the parameter
		            DataTypeDefinition.BOOLEAN,             // The parameter value type
		            false,                                  // Indicates whether the parameter is mandatory
		            getParamDisplayLabel(PARAM_ACTIVE)		// The parameters display label
		         )
		);   
	}

	@Override
	protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
			
		try {
			if (logger.isDebugEnabled()) logger.debug("Inside SetWebFlag executeImpl");
			
			Boolean activeFlag = (Boolean)action.getParameterValue(PARAM_ACTIVE);

			if (activeFlag == null) activeFlag = true;
			
			Map<QName, Serializable> properties = nodeService.getProperties(actionedUponNodeRef);
			// set the bi:isActive property to the value of the parameter
			properties.put(QName.createQName(BInformedModel.NAMESPACE_BINFORMED_CONTENT_MODEL, BInformedModel.PROP_IS_ACTIVE), activeFlag);
			
			//If properties exist: get them else set them to null
			Serializable lastPublishedProperty = properties.get(QName.createQName(BInformedModel.NAMESPACE_BINFORMED_CONTENT_MODEL, BInformedModel.PROP_PUBLISHED));
			Serializable liferayIDProperty = properties.get(QName.createQName(BInformedModel.NAMESPACE_BINFORMED_CONTENT_MODEL, BInformedModel.PROP_EXTERNALID));
			Date lastPublished = (lastPublishedProperty != null) ? (Date) lastPublishedProperty : null;
			int liferayID = (liferayIDProperty != null) ? (Integer) liferayIDProperty : 0;
			
			String crud = "";
			Date justNow = new Date();
			
					
			String message = "";
			if (activeFlag) {
				// set the bi:published property to now
				properties.put(QName.createQName(BInformedModel.NAMESPACE_BINFORMED_CONTENT_MODEL, BInformedModel.PROP_PUBLISHED), justNow);
				
				if(lastPublished == null && liferayID == 0) {
					crud = "create";
				}else {
					crud = "update";
				}
				
				//Crate a activemq message
				message = 
						"{\r\n" + 
						"	\"alfrescoID\":\"" + actionedUponNodeRef.getId() + "\",\r\n" +
						"	\"liferayID\":"+ liferayID + ",\r\n" +
						"	\"action\":\""+ crud + "\"\r\n" +
						"}";
			}else {
				// reset the properties
				properties.put(QName.createQName(BInformedModel.NAMESPACE_BINFORMED_CONTENT_MODEL, BInformedModel.PROP_PUBLISHED), null);
				properties.put(QName.createQName(BInformedModel.NAMESPACE_BINFORMED_CONTENT_MODEL, BInformedModel.PROP_EXTERNALID), 0);
				
				message = 
						"{\r\n" + 
						"	\"alfrescoID\":\"" + actionedUponNodeRef.getId() + "\",\r\n" +
						"	\"liferayID\":\""+ liferayID + "\",\r\n" +
						"	\"action\":\"delete\"\r\n" +
						"}";
			}
			
			Sender.send(message);
			
			// if the aspect has already been added, set the properties
			if (nodeService.hasAspect(actionedUponNodeRef, QName.createQName(BInformedModel.NAMESPACE_BINFORMED_CONTENT_MODEL, BInformedModel.ASPECT_BI_WEBABLE))) {
				nodeService.setProperties(actionedUponNodeRef, properties);
				if (logger.isDebugEnabled()) logger.debug("Node has aspect");
			} else {
				// otherwise, add the aspect and set the properties
				nodeService.addAspect(actionedUponNodeRef, QName.createQName(BInformedModel.NAMESPACE_BINFORMED_CONTENT_MODEL, BInformedModel.ASPECT_BI_WEBABLE), properties);
				if (logger.isDebugEnabled()) logger.debug("Node does not have aspect");
			}                  
		} catch (InvalidAspectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidQNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidNodeRefException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
                                 
	}


}