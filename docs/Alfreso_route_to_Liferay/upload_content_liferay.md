![License](img/cc-by-sa-88x31.png)<br>
This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License. To view a copy of this license, visit [http://creativecommons.org/licenses/by-sa/3.0/](http://creativecommons.org/licenses/by-sa/3.0/) or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.

[Leave the tutorial](../index.md)<br>
[Back to tutorial overview](index.md)<br>
[Back to the previous chapter](using_cmis_download.md)

## Chapter 5.: Upload content to Liferay
In the previous chapter we downloaded content from Alfresco by using the Camel-CMIS component. Now we need to upload the document to Liferay. Liferay dropped CMIS support, so this time we are going to use REST for the purpose.

As I said earlier: Both, Alfresco and Liferay are following the *headless approach*. Offering an extensive REST API that makes the development of custom clients possible.

But there's even more. The [Open API Specification (formerly Swagger Specification)](https://swagger.io/docs/specification/about/) (OAS3) not only has standards for describing a REST API, but also there are a lot of tools available that can generate code from API specifications (Contract First) or vice versa API specifations from code (Code First).

Thanks to the fact that Alfresco and Liferay both follow this defacto standard, it is very easy to find out what we need to do next.

[Alfresco API on Swagger Hub](https://app.swaggerhub.com/apis/CPage/alfresco-content_services_rest_api/1)<br>
[Liferay API on Swagger Hub](https://app.swaggerhub.com/apis/liferayinc/headless-delivery/v1.0#/)

However, we still need some coding to do before we can upload our document to Liferay.

### Prepare the upload
Looking up the specification of the Liferay REST API that allows us to upload a document to Liferay you'll find this:

![Liferay OAS](img/liferay_oas.png)

Taking into account the necessary Authentication that means that we need
- The ID of a folder where our document will finally land in Liferay
- a multipart/form-data body with a *file* object and a *document* JSON to set metadata (e.g. the documents name) 
- a header with the (basic) authentication
- to create an url like *{host}:{port}/o/headless-delivery/v1.0/document-folders/{documentFolderId}/documents*
- to do a POST request with all this information

### Getting the FolderId
Let's make things short here. I did it like this: In Liferay I created a new folder called *Whitepapers* in the *Document and Media Library* and navigated into this folder:

![Liferay OAS](img/liferay_empty_folder.png)

And here's another task for you: Find the hidden folderId in the picture.

Honestly: In a production environment there must be a better way, but hardcoding this for the tutorial is acceptable.

### Getting the document / file name
Do you remember the CMIS download from the last chapter? (shortened)
```
[{cmis:objectId=8d03bfbc-ed24-4f96-8c4a-fc8f333b7b37;1.0, 
cmis:contentStreamFileName=myWhitepaper.pdf, 
cmis:name=myWhitepaper.pdf, 
CamelCMISContent=java.io.BufferedInputStream@53ad0d89
```
If you look at this a little bit closer, it is a JAVA Array with only one entry. And this entry is a JAVA Map.

As this is still available in the exchange body, let's quickly put the *cmis:name* in a exchange property: Add a *Set Property* component to the route. Set the expression language to *simple*, the simple expression to *${body[0]["cmis:name"]}* and the Property Name to *FileName*

Was that to quick? You want a picture here? Come on! You've done that before! 

What's noteworthy is the simple language that is used here. If you want to find out why this expression works, have a look at [the documentation of the simple language](https://camel.apache.org/components/latest/languages/simple-language.html)

### Getting the file object
Did you notice the *CamelCMISContent=java.io.BufferedInputStream@53ad0d89* in the CMIS response? That's our file. 

What we learn here is that the Camel exchange object can transport JAVA objects! That's amazing, if you think about it. POJOs (Plain old JAVA objects) in the exchange. WOW! We come back to this in a minute.

Just put the *CamelCMISContent* into another property. Like above: Set the expression language to *simple*, the simple expression to *${body[0]["CamelCMISContent"]}* and the Property Name to *CamelCMISContent*

### Getting the *document* JSON
Up to this point we have two new properties on the exchange. The *FileName* and the *CamelCMISContent*. But the *FileName* needs to be posted to Liferay as a JSON structure like this:
```json
{
	"title": "myWhitepaper.pdf"
}
```
Thank Camel, there is the [Freemarker Component](https://access.redhat.com/documentation/en-us/red_hat_fuse/7.6/html/apache_camel_component_reference/freemarker-component).

Freemarker is a powerfull template engine that will create the JSON for us.

The Freemarker template needs to be on the classpath to be found by the component. So create a new file called *liferay_document.post.ftl* under ```[Your fuse integration project]/src/main/java```
However, that is not really a good place for a resource. So if you like you can also change the classpath settings for your project and create a location like ```[Your fuse integration project]/src/main/resources```

In that file put **2DO The property shoud be used here!**
```json
{
	"title": "${body[0]["cmis:name"]}"
}
```

Now place a *Generic* component onto the route, like you did with *Camel-CMIS*. But this time select the *Freemarker* component.

The *Uri* setting goes to *freemarker:liferay_document.post.ftl* and the id to *_freemarker_liferay*

The resulting JSON will be in the exchange body afterwards.

### Creating the multipart/form-data body and the header
Yes, that sounds complicated...

Let's look what we got until here. Your route from Camel-CMIS to Freemarker looks like this:

![Freemarker](img/fuse_cmis_to_freemarker.png)

The *FileName* and the *CamelCMISContent* are stored in exchange properties and the JSON we need is in the exchange body.

Reading the [documentation of the Camel-HTTP4 component](https://access.redhat.com/documentation/en-us/red_hat_fuse/7.6/html/apache_camel_component_reference/http4-component), that we will use to POST the request, it's clear that the component is able to use the exchange *body* and *header* to create the request.





[Back to the previous chapter](using_cmis_download.md)<br>
[Back to tutorial overview](index.md)<br>
[Leave the tutoral](../index.md)
