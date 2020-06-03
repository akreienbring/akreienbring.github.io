André Kreienbring, Cologne, June 2020

![License](img/cc-by-sa-88x31.png)<br>
This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License. To view a copy of this license, visit [http://creativecommons.org/licenses/by-sa/3.0/](http://creativecommons.org/licenses/by-sa/3.0/) or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.

[Leave tutorial](../index.md)

## Alfreso - Liferay integration based on Camel / Fuse
This is a Tutorial that extends the [Alfresco Developer Series](https://ecmarchitect.com/alfresco-developer-series) from Jeff Potts which is an essential start if you try to dig into the Alfresco Customization / Developer world.

In that series Jeff builds a scenario where a company, called SomeCo, publishes whitepapers, stored in Alfresco, to an external website. He explains and realizes all the necessary steps on the Alfresco site without actually transporting /managing documents to / in the external repository.

### The route to Liferay
Thinking of [Alfresco](https://en.wikipedia.org/wiki/Alfresco_Software) as the leading Open Source ECM System and [Liferay](https://en.wikipedia.org/wiki/Liferay) as the leading Portal Server in the JAVA world, an integration of the two is worth thinking about. Actually it has been done many times. For example by using [CMIS](https://en.wikipedia.org/wiki/Content_Management_Interoperability_Services). But CMIS seems not to be a rising star anymore and Liferay dropped the CMIS store for a reason.

Both systems follow the "Headless" approach. That means that the GUI and the server logic are getting decoupled and the core functionality is offered by REST services.

Having said that you might think: *OK, let's use these services and connect Alfresco with Liferay. That's no rocket sience!*

You surely could. But thinking a little bit further you might want to reuse your connection components. You might want to have some system-independent businesslogic or workflow tooling to make everything part of a business process. Or you may have the need to connect more than two systems to build the solution.

That's where [Apache Camel](https://en.wikipedia.org/wiki/Apache_Camel) and [Red Hat Fuse](https://en.wikipedia.org/wiki/Fuse_ESB) come into the game for designing a shiny middleware.

So I decided to have a closer look...

### About this work
Although the solution that will be build in this tutorial is finally working, it is far from being production ready and might not even be always done in the perfect way. So, if you find anything that can be done better for any reason, don't hesitate to let me know. For example you could raise an [issue](https://github.com/akreienbring/akreienbring.github.io/issues) to start a discussion.

During this turorial you will 
**2DO Brief description of all sections**

### Part 1: Now let's start working steb by step
We will first check what we need for the tutorial to work out. As I said it absolutely depends on Jeffs [SomeCo tutorial](https://ecmarchitect.com/alfresco-developer-series). If you haven't done it, I recommend starting there and come back later. 
At a minimum go, get and install the tutorial code from [his github repo](https://github.com/jpotts/alfresco-developer-series).

... Welcome back! 

At the very first I will give you an overview over all the main software I used in the process. Needless to say that I guarantee nothing. Nor do I support the things we will create here. It is allways a good idea to checkout the respective Community Forum if you get stuck.

[Chapter 1.: A bunch of tools we need to get up and running](softwarestack.md)

### Part 2: Extending the Alfreso SomeCo Module with an ActiveMQ message sender
Now you mastered the *Open Source Version Hell* you've earned a first preview to the route to Liferay.

![The Start of the route](img/start_of_route.jpg)

We will get back to the details later, but note the ActiveMQ Camel Component at the beginning. That's the entry point to our middleware, to our integration framework. Also note that some properties are extracted from the incoming message. 
This all means that Alfresco has to be able to send such a message.

Follow on with:<br>
[Chapter 2.: Sending an ActiveMQ message from Alfresco](messagesending.md)

### Part 3: Consuming ActiveMQ messages with Camel / Fuse
*Even the nicest message fails if nobody wants to hear it.* (If this is a illegal quotation, let me know)

Now on to Fuse! We will set up a *Fuse Integration Project* with Eclipse and start building our route. Our first task is to cope with messages of the form
```
{
	"alfrescoID":"8d03bfbc-ed24-4f96-8c4a-fc8f333b7b37",
	"action":"create"
}
```
which was created by Alfresco and send to ActiveMQ. The message holds the Alfresco document ID (of type whitepaper) and the action that we finally want to perform in Liferay.

[Chapter 3.: Getting started with Fuse](getting_started_fuse.md)

### Part 4: Retrieve a Document by using Camel-CMIS
Until now we only have the ID of an Alfresco document. The next step is getting our hands on the document object itself. We will use the Camel-CMIS component to achieve this.

During this, it will also be shown how to add Camel components to Fuse which are not in the Fuse palette.

[Chapter 4.: Using CMIS to download a document from Alfresco](using_cmis_download.md)

### Part 5: Upload content to Liferay
Using the so called *headless delivery* REST API of Liferay, we are going to upload what we download via CMIS from Alfresco in this chapter.

Using Camel this is one of the rare spots where a small JAVA bean must be written to achieve what we want.

[Chapter 5.: Upload content to Liferay](upload_content_liferay.md)

[Leave tutorial](../index.md)
