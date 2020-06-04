![License](img/cc-by-sa-88x31.png)<br>
This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License. To view a copy of this license, visit [http://creativecommons.org/licenses/by-sa/3.0/](http://creativecommons.org/licenses/by-sa/3.0/) or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.

[Leave the tutorial](../index.md)<br>
[Back to tutorial overview](index.md)<br>
[Back to the previous chapter](using_cmis_download.md)

## Chapter 5.: Upload content to Liferay
In the previous chapter we downloaded content from Alfresco by using the Camel-CMIS component. Now we need to upload the document to Liferay. Liferay dropped CMIS support, so this time we are going to use REST for the purpose.

As I said earlier: Both, Alfresco and Liferay are following the *headless approach*. Offering an extensive REST API that makes the development of custom clients possible.

But there's even more. The [Open API Specification (formerly Swagger Specification)](https://swagger.io/docs/specification/about/) (OAS3) not only has standards for describing a REST API, but also there are a lot of tools available that can generate code from API specifications (Contract First) or vice versa API specifications from code (Code First).

Thanks to the fact that Alfresco and Liferay both follow this defacto standard, it is very easy to find out what we need to do next.

[Alfresco API on Swagger Hub](https://app.swaggerhub.com/apis/CPage/alfresco-content_services_rest_api/1)<br>
[Liferay API on Swagger Hub](https://app.swaggerhub.com/apis/liferayinc/headless-delivery/v1.0#/)

However, we still need some coding to do before we can upload our document to Liferay.

### Prepare the upload
First of all you need to enable uploads to Liferay by changing the */WEB-INF/web.xml* as they are disabled by default. Locate the *Module Framework Servlet* and change it to the following
```xml
<servlet>
	<servlet-name>Module Framework Servlet</servlet-name>
	<servlet-class>com.liferay.portal.module.framework.ModuleFrameworkServletAdapter</servlet-class>
	<load-on-startup>1</load-on-startup>
	<async-supported>true</async-supported>
        <multipart-config>
            <location>/tmp</location>
            <max-file-size>20848820</max-file-size>
            <max-request-size>418018841</max-request-size>
            <file-size-threshold>1048576</file-size-threshold>
        </multipart-config>
</servlet>
```
Back to the REST API:<br>
Looking up the OAS3 specification of the Liferay REST API, that allows us to upload a document to Liferay, you'll find this:

![Liferay OAS](img/liferay_oas.png)

Taking into account the necessary authentication that means that we need
- The ID of a folder where our document will finally land in Liferay
- the document file
- a JSON with the documents name
- a multipart/form-data body with the *file* object and the *document* JSON 
- a header with the (basic) authentication
- to create an url like *[myHost]:[myPort]/o/headless-delivery/v1.0/document-folders/[myDocumentFolderId]/documents*
- to do a POST request with all this information

### Getting the FolderId
Let's make things short here. I did it like this: In Liferay I created a new folder called *Whitepapers* in the *Document and Media Library* and navigated into this folder:

![Liferay OAS](img/liferay_empty_folder.png)

And here's another task for you: Find the hidden *folderId* in the picture.

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

[Freemarker](https://freemarker.apache.org/) is a powerfull template engine that will create the JSON for us.

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

Our 2Do list now looks like this
- The ID of a folder where our document will finally land in Liferay :heavy_check_mark: (we know it from the Liferay url)
- the document file :x: (we have only the input stream)
- a JSON with the documents name :heavy_check_mark: (thanks to Freemarker it's in the exchange body)
- a multipart/form-data body with the *file* object and the *document* JSON :x:
- a header with the (basic) authentication :x:
- to create an url like *[myHost]:[myPort]/o/headless-delivery/v1.0/document-folders/[myDocumentFolderId]/documents* :x:
- to do a POST request with all this information :x:

The *CamelCMISContent* input stream is stored in an exchange property and the JSON is in the exchange body.

Reading the [documentation of the Camel-HTTP4 component](https://access.redhat.com/documentation/en-us/red_hat_fuse/7.6/html/apache_camel_component_reference/http4-component), that we will use to POST the request, it's clear that the component is able to use the exchange *body* and *header* to create the request.

The best thing we can do here is some coding. We will create a custom bean and add it to the route. This is called a *Processor*. It simply gives us the possibiliy to do several things in one component.

Add a new file named *myDocumentSender.java* under ```[Your fuse integration project]/src/main/java/org/test```

**myDocumentSender.java class**
```java
/* Gets the CMIS Content from the Inputstream and writes it to a local file.
 * After that the multipart/form header is constructed and put in the exchange body.
 * TODO: not hardcode the directory
 */
public class MyDocumentSender implements Processor {

    public void process(Exchange exchange) {
        Map<String, Object> properties = exchange.getProperties();
        String json = exchange.getIn().getBody(String.class);
	InputStream inputstream = (InputStream) properties.get("CamelCMISContent");
	String pathToFile = "C:/OpenSource/tmp/" + properties.get("FileName");
	Path path = Paths.get(pathToFile);
	File file;
	
	try {
		Files.copy(inputstream, path, StandardCopyOption.REPLACE_EXISTING);
		file = new File(pathToFile);
		MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
	        multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
	        
	        multipartEntityBuilder.addBinaryBody("file", file);
	        multipartEntityBuilder.addTextBody("document", json);
	        exchange.getOut().setHeader("Authorization","Basic YWtyZWllbmJyaW5nQGdtYWlsLmNvbTphcHBsZXBpZQ==");
	        exchange.getOut().setBody(multipartEntityBuilder.build());
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    }
}
```

To make the bean available to Camel just add it to the source of your *jboss-camel-context.xml*
```
    <bean class="org.test.MyDocumentSender" id="DocumentSender"/>
    <camelContext id="spring-context" xmlns="http://camel.apache.org/schema/spring">
```
And add a *Bean* Component to your route. In the properties of that component set *Ref* to the id of the bean *DocumentSender*

And all of a sudden our 2Do list now looks like this
- The ID of a folder where our document will finally land in Liferay :heavy_check_mark: 
- the document file :heavy_check_mark: 
- a JSON with the documents name :heavy_check_mark: 
- a multipart/form-data body with the *file* object and the *document* JSON :heavy_check_mark:
- a header with the (basic) authentication :heavy_check_mark:
- to create an url like *[myHost]:[myPort]/o/headless-delivery/v1.0/document-folders/[myDocumentFolderId]/documents* :x:
- to do a POST request with all this information :x:

Did you notice the *exchange.getIn()* and *exchange.getOut()* statements? That is the *consumer* and *producer* pattern we already spoke about. The bean (a Processor) simply consumes the exchange, has access to properties, body, header etc. and produces a new exchange object with manipulated properties, body, header etc. that is passed on to the next component in the route.

A word on the Authentication Header: The *Basic Authentication* is created by a Base64 encryption of your Liferay credentials in the form "username:password" (e.g. ```"test@liferay.com:test```"). I got mine using Postman to test the REST services of Liferay. You can also use JAVA to encrypt your credentials or use an online tool such as [https://www.base64encode.org/](https://www.base64encode.org/). (I haven't tried that one)

### Post the request with Camel-HTTP4
We're almost there! Use the *Generic* Component once more to drop a [HTTP4 Component](https://access.redhat.com/documentation/en-us/red_hat_fuse/7.6/html/apache_camel_component_reference/http4-component) onto the *Document Sender* bean.

Set the *Uri* of that component to 
```
http4://[myHost]:[myPort]/o/headless-delivery/v1.0/document-folders/[myDocumentFolderId]/documents?httpMethod=POST
```
and the *Id* to *_createLiferayDocument*

The response of that request goes into the body of the exchange. Use a *Log* component to output it.




[Back to the previous chapter](using_cmis_download.md)<br>
[Back to tutorial overview](index.md)<br>
[Leave the tutoral](../index.md)
