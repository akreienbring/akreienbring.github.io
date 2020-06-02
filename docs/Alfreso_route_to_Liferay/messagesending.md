[Back to the top](../index.md)<br>
[Back to tutorial overwiew](index.md)

## Sending a message from Alfreso to ActiveMQ
Our route to Liferay starts when the Camel middleware receives a ActiveMQ message. More on the route later...
![The Start of the route](img/start_of_route.jpg)

Therefor we need to do something within Alfreso.


### Now it's time to write some JAVA code. 

Remember the [Developing custom Actions tutorial](https://ecmarchitect.com/alfresco-developer-series-tutorials/actions/tutorial/tutorial.html) from the [Alfresco Developer Series](https://ecmarchitect.com/alfresco-developer-series)?

![Publish to Liferay Action](img/publish_action.png)

That gave us 3 new actions in Alfreso 
1. *set-web-flag* 
2. *enable-web-flag*
3. *disable-web-flag*
I just relabled them (like some other things too) to make clear what we are here for.

What you see in the picture enables the web flag when you (could) click on it. The latter two actions extend the *set-web-flag* ActionExecuter. So let's put the central logic that handles messages there.

As you can download the code from this repository, I will not post the whole code here. Just the main and important changes I made to the

**SetWebFlag.java class**
```Java			
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
```

[Back to tutorial overwiew](index.md)<br> 
[Back to the top](../index.md)

