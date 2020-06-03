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
- a body with a *file* object and a *document* JSON to set metadata (e.g. the documents name)
- a header with the (basic) authentication
- to create an url like *{host}:{port}/o/headless-delivery/v1.0/document-folders/{documentFolderId}/documents*

### Getting the FolderId
Let's make things short here. I did it like this. In Liferay I created a new folder called *whitepapers* in the *Document and Media Library* and navigated into this folder:

![Liferay OAS](img/liferay_empty_folder.png)

And here's another task for you: Find the hidden folderId in the picture.

[Back to the previous chapter](using_cmis_download.md)<br>
[Back to tutorial overview](index.md)<br>
[Leave the tutoral](../index.md)
