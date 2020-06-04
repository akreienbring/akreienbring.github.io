![License](img/cc-by-sa-88x31.png)<br>
This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License. To view a copy of this license, visit [http://creativecommons.org/licenses/by-sa/3.0/](http://creativecommons.org/licenses/by-sa/3.0/) or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.

[Leave the tutorial](../index.md)<br>
[Back to tutorial overview](index.md)<br>
[Back to the previous chapter](prepare_crud.md)

## Chapter 7.: Implementing the C(R)UD behaviour
As of now Alfresco sends an ActiveMQ message that contains the *create* action. But now, that we have the ID of the created Liferay document, stored as a property of the Alfresco document, we are able to do the next step.

And that is: updating / deleting the Liferay document when it's updated / deleted in Alfresco.

During the [Implementing Custom Behaviors in Alfresco](https://ecmarchitect.com/alfresco-developer-series-tutorials/behaviors/tutorial/tutorial.html) you learned what a behavior is and why it's suited for the ratings usecase.

Basically a behaviour defines what code to execute when a certain event was triggered by the Alfresco platform. As behaviours can be tied to types and aspects they can literally be added to objects in Alfreso.

If you think about it: thats exactly what we need to implement the update and delete behaviors of our Whitepapers.

Looking through the [list of available events](https://docs.alfresco.com/6.2/references/dev-extension-points-behaviors.html) I picked this two for implementing the update and delete behaviours

- *VersionServicePolicies / afterCreateVersion*
- *NodeServicePolicies / beforeDeleteNode*

For this tutorial I prefer *afterCreateVersion* over *onUpdateNode* because the latter gets also fired when a property of the node / document is updated.
In a usecase where we transfer content to Liferay with more medadata then simply the documents name, then the decision could be different.

*afterCreateVersion* is triggered when a new version of a document is uploaded to the repository. So let's go for it with JAVA. I prefer JAVA instead of (the also possible) Javascript. But thats just my opinion.

One more thought before we start coding: I first just wanted to use a simple rule that triggers the already existing
```enable-web-flag``` <br>
```disable-web-flag```
actions. But the rule configuration in Alfresco does not provide a possibility to react on the upload of a new version out of the box.

The central point where all the magic o message sending happens is the ```set-web-flag``` action. So let's revisit the code.

### Making SetWebFlag smart
Don't blame me for using my own package names and namespaces different from the original SomeCo tutorial. That should be easy to fix for you, if you follow on. BTW: This is a good practice of the things you learned.

**SetWebFlag.java**
```java
Boolean activeFlag = (Boolean)action.getParameterValue(PARAM_ACTIVE);

if (activeFlag == null) activeFlag = true;
			
Map<QName, Serializable> properties = nodeService.getProperties(actionedUponNodeRef);
      
// set the bi:isActive property to the value of the parameter
properties.put(QName.createQName(
	BInformedModel.NAMESPACE_BINFORMED_CONTENT_MODEL,
        BInformedModel.PROP_IS_ACTIVE),activeFlag);
			
//If properties exist: get them else set them to null
Serializable lastPublishedProperty = properties.get(QName.createQName(
	BInformedModel.NAMESPACE_BINFORMED_CONTENT_MODEL,
        BInformedModel.PROP_PUBLISHED));
      
Serializable liferayIDProperty = properties.get(QName.createQName(
	BInformedModel.NAMESPACE_BINFORMED_CONTENT_MODEL,
       	BInformedModel.PROP_EXTERNALID));
      
Date lastPublished = (lastPublishedProperty != null) ? (Date) lastPublishedProperty : null;
int liferayID = (liferayIDProperty != null) ? (Integer) liferayIDProperty : 0;
			
String crud = "";
Date justNow = new Date();
					
String message = "";
if (activeFlag) {
	// set the bi:published property to now
	properties.put(QName.createQName(
		BInformedModel.NAMESPACE_BINFORMED_CONTENT_MODEL, 
          	BInformedModel.PROP_PUBLISHED), justNow);
				
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
	properties.put(QName.createQName(
		BInformedModel.NAMESPACE_BINFORMED_CONTENT_MODEL, 
          	BInformedModel.PROP_PUBLISHED), null);
					
	properties.put(QName.createQName(
		BInformedModel.NAMESPACE_BINFORMED_CONTENT_MODEL, 
         	BInformedModel.PROP_EXTERNALID), 0);
				
	message = 
		"{\r\n" + 
		"	\"alfrescoID\":\"" + actionedUponNodeRef.getId() + "\",\r\n" +
		"	\"liferayID\":\""+ liferayID + "\",\r\n" +
		"	\"action\":\"delete\"\r\n" +
		"}";
}
			
Sender.send(message);
```

As always this is only the main excerpt of the code.

What has changed is that we send ActiveMQ messages with the action set to "delete" or "update" depending on the *isActive* flag and the *published* properties. Clearly if the document has a publish date, the it needs to be updated. If it has not, it needs to be created.

And that the *liferayID* is now included in the message.

### Creating the "behavior" bean
The bean definition of the new behaviour goes to the *service-context.xml* in the Repository tier of our Alfresco module.
```xml
	<!--
	 Whitpaper external action for updating or deleting documents on the external platform if active.
	-->
	<bean id="externalAction"
		class="[myPackage].behavior.ExternalAction" init-method="init">
		<property name="nodeService">
			<ref bean="NodeService" />
		</property>
		<property name="actionService">
			<ref bean="ActionService" />
		</property>
		<property name="policyComponent">
			<ref bean="policyComponent" />
		</property>
	</bean>
```

Note that the bean references the *NodeService* and the *ActionService*. Spring will inject them for us in the JAVA class we will generate. Thats handy. These services will let us access Alfresco Core elements.
The same is true for the *policyComponent* which we need to bind to the mentioned events.

### Writing the "behavior" code
Create the JAVA file where the behaviour will be defined. Look at the main parts of the code here:

**ExternalAction.java**
```java
public void init() {
	if (logger.isDebugEnabled()) logger.debug("Initializing external actions");

	// Create behaviours
	this.afterCreateVersion = new JavaBehaviour(this, "afterCreateVersion", NotificationFrequency.TRANSACTION_COMMIT);
	this.beforeDeleteNode = new JavaBehaviour(this, "beforeDeleteNode", NotificationFrequency.EVERY_EVENT);

	// Bind behaviours to node policies
	this.policyComponent.bindClassBehaviour(
		QName.createQName(NamespaceService.ALFRESCO_URI, "afterCreateVersion"),
		QName.createQName(
			BInformedModel.NAMESPACE_BINFORMED_CONTENT_MODEL,
			BInformedModel.TYPE_BI_WHITEPAPER
		),
		this.afterCreateVersion
	);
		
	this.policyComponent.bindClassBehaviour(
		QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
		QName.createQName(
			BInformedModel.NAMESPACE_BINFORMED_CONTENT_MODEL,
			BInformedModel.TYPE_BI_WHITEPAPER
		),
		this.beforeDeleteNode
	);
}

public void afterCreateVersion(NodeRef versionableNode, Version version){
	if (logger.isDebugEnabled()) logger.debug("Inside afterCreateVersion");
		
	if(isExternalActionRequired(versionableNode)){
		if (logger.isDebugEnabled()) logger.debug("A new version was created. Calling enable-web-flag");
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
}

/* 
* Determines if the external repository needs to be manipulated.
* This is only necessary if the NodeRef has the webable aspect and is active.
* 
*/
private boolean isExternalActionRequired(NodeRef nodeRef) {
	boolean hasWebableAspect = nodeService.hasAspect(
		nodeRef, 	
		QName.createQName(
			BInformedModel.NAMESPACE_BINFORMED_CONTENT_MODEL, 
			BInformedModel.ASPECT_BI_WEBABLE)
		);
		
	if (hasWebableAspect) {
		boolean isActive = (Boolean) nodeService.getProperty(
			nodeRef, 
			QName.createQName(
				BInformedModel.NAMESPACE_BINFORMED_CONTENT_MODEL, 
				BInformedModel.PROP_IS_ACTIVE)
			);
		return isActive ? true: false;
	}else {
		return false;
	}
}
```
Some notes on the code: <br>
The *init* method is called when Alfresco starts up. It "binds" the behaviour to the events *afterCreateVersion* and *beforeDeleteNode*. But ONLY if the type of the document is Whitepaper.

The methods that implement the behaviour that happens when the corresponding event occurs both check if any action must be triggered. This depends on the question if the node has the *webable* aspect and is active. 
Being active means: The document was published to Liferay and hence must be updated or deleted.

In case of the *beforeDeleteNode* event there is some extra work to do. This event is fired in some situations when the node is **not** finally deleted from the repository. Hence we have to check for the *working copy aspect*

If all tests are passed then the *actionService* is used to execute the *enable-web-flag* or *disable-web-flag* actions that you already know. These, in turn, execute the *set-web-flag* action with the *active* parameter set to *true* or *false*.

This way the logic, that we implemented in *SetWebFlag.java* will send the JSON message with the *"action": "update"* or *"action":"delete"* content.


[Back to the previous chapter](prepare_crud.md)<br>
[Back to tutorial overview](index.md)<br>
[Leave the tutorial](../index.md)
