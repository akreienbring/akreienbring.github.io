package de.binformed.platform.behavior;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.version.VersionServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

import de.binformed.platform.model.BInformedModel;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;

public class ExternalAction implements NodeServicePolicies.BeforeDeleteNodePolicy, NodeServicePolicies.OnDeleteNodePolicy, VersionServicePolicies.AfterCreateVersionPolicy {

	// Dependencies
	private NodeService nodeService;
	private ActionService actionService;
	private PolicyComponent policyComponent;

	// Behaviors
	private Behaviour afterCreateVersion;
	private Behaviour beforeDeleteNode;
	private Behaviour onDeleteNode;

	private Logger logger = Logger.getLogger(ExternalAction.class);

	public void init() {
		if (logger.isDebugEnabled())
			logger.debug("Initializing external actions");

		// Create behaviours
		this.afterCreateVersion = new JavaBehaviour(this, "afterCreateVersion", NotificationFrequency.TRANSACTION_COMMIT);
		this.beforeDeleteNode = new JavaBehaviour(this, "beforeDeleteNode", NotificationFrequency.EVERY_EVENT);
		this.onDeleteNode = new JavaBehaviour(this, "onDeleteNode", NotificationFrequency.EVERY_EVENT);

		// Bind behaviours to node policies
		this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "afterCreateVersion"),
				QName.createQName(BInformedModel.NAMESPACE_BINFORMED_CONTENT_MODEL,
						BInformedModel.TYPE_BI_WHITEPAPER),
				this.afterCreateVersion);
		
		this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
				QName.createQName(BInformedModel.NAMESPACE_BINFORMED_CONTENT_MODEL,
						BInformedModel.TYPE_BI_WHITEPAPER),
				this.beforeDeleteNode);
		
		this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteNode"),
				QName.createQName(BInformedModel.NAMESPACE_BINFORMED_CONTENT_MODEL,
						BInformedModel.TYPE_BI_WHITEPAPER),
				this.onDeleteNode);
	}

	public void afterCreateVersion(NodeRef versionableNode, Version version){
		if (logger.isDebugEnabled()) logger.debug("Inside afterCreateVersion");
		
		if(isExternalActionRequired(versionableNode)){
			if (logger.isDebugEnabled()) logger.debug("A new version was created. Calling enable-web-flag");
			//getAction is NOT working here. What is it good for?
			//Action action = actionService.getAction(versionableNode, "enable-web-flag");
			Action action = actionService.createAction("enable-web-flag");
			actionService.executeAction(action, versionableNode);
		}
	}

	public void beforeDeleteNode(NodeRef nodeRef) {
		if (logger.isDebugEnabled()) logger.debug("Inside beforeDeleteNode");
		
		if(!hasWorkingCopyAspect(nodeRef) && isExternalActionRequired(nodeRef)){
			//if a node has the working copy aspect then this event was triggered even thought
			//the NODE / Document is NOT finally deleted. For example if a new version is uploaded or was 
			//checked out / in
			
			if (logger.isDebugEnabled()) logger.debug("A webable and active Node was deleted. Calling disable-web-flag");
			Action action = actionService.createAction("disable-web-flag");
			actionService.executeAction(action, nodeRef);
			
		};
	}

	/*
	 * Just left here to see the event in the logs.
	 */
	public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived) {
		if (logger.isDebugEnabled()) logger.debug("Inside onDeleteNode. Calling disable-web-flag");
		
		NodeRef parentRef = childAssocRef.getParentRef();
	}
	
	/* 
	 * Determines if the external repository needs to be manipulated.
	 * This is only necessary if the NodeRef has the webable aspect and  is active.
	 * 
	 */
	private boolean isExternalActionRequired(NodeRef nodeRef) {
		boolean hasWebableAspect = nodeService.hasAspect(nodeRef, QName.createQName(BInformedModel.NAMESPACE_BINFORMED_CONTENT_MODEL, BInformedModel.ASPECT_BI_WEBABLE));
		if (hasWebableAspect) {
			boolean isActive = (Boolean) nodeService.getProperty(nodeRef, QName.createQName(BInformedModel.NAMESPACE_BINFORMED_CONTENT_MODEL, BInformedModel.PROP_IS_ACTIVE));
			return isActive ? true: false;
		}else {
			return false;
		}
	}

	/*
	 * Check a node for the presence of the working copy aspect. 
	 */
	private boolean hasWorkingCopyAspect(NodeRef nodeRef) {
		if (logger.isDebugEnabled()) logger.debug("Node exists = " + nodeService.exists(nodeRef));
		
		if (nodeService.hasAspect(nodeRef, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,"workingcopy"))) {
			if (logger.isDebugEnabled()) logger.debug("Node has the working copy aspect.");
			
			return true;
		} else {
			if (logger.isDebugEnabled()) logger.debug("Node doesn't have the working copy aspect.");
			return false;
		}
	}
	
	public NodeService getNodeService() {
		return nodeService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public ActionService getActionService() {
		return actionService;
	}

	public void setActionService(ActionService actionService) {
		this.actionService = actionService;
	}
	
	public PolicyComponent getPolicyComponent() {
		return policyComponent;
	}

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

}
