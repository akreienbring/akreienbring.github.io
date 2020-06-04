![License](img/cc-by-sa-88x31.png)<br>
This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License. To view a copy of this license, visit [http://creativecommons.org/licenses/by-sa/3.0/](http://creativecommons.org/licenses/by-sa/3.0/) or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.

[Leave the tutorial](../index.md)<br>
[Back to tutorial overview](index.md)<br>
[Back to the previous chapter](upload_content_liferay.md)

## Chapter 6.: Preparations for C(R)UD
In the the previous chapter we successfully created a Alfresco document in Liferay. If the document is updated / deleted in Alfresco it must also be updated / deleted in Liferay.
That is the idea of CRUD Create-Read-Update-Delete. 

We don't have to read from Liferay in our scenario, but thinking about Update / Delete it gets clear, that we need to store the ID of the Liferay document in Alfresco.

In this chapter we will adjust the content model of Alfresco to store an external ID with a document. After that we will get the Liferay ID from the JSON response of the upload and update the Alfresco document with this ID.

### Extend the Alfresco content model of Whitepapers
During the development of the [SomeCo Module](https://ecmarchitect.com/alfresco-developer-series-tutorials/content/tutorial/tutorial.html) a new type "Whitepaper" was created in the Alfresco Repository tier. Also a new aspect "webable" with the properties "isActive" (boolean) and "published" (date) was added.

For beeing able to store the Liferay ID with a *webable Whitepaper* this aspect needs to be extended like this:
```xml
<aspect name="sc:webable">
   <title>Someco Webable</title>
   <properties>
       <property name="sc:published">
           <type>d:date</type>
        </property>
        <property name="sc:isActive">
            <type>d:boolean</type>
            <default>false</default>
        </property>
        <property name="sc:externalID">
            <type>d:int</type>
        </property>
   </properties>
</aspect>
```

It's also a good idea to have this properties in the corresponding JAVA class for later usage:
```java
public static final String PROP_PUBLISHED = "published";
public static final String PROP_IS_ACTIVE = "isActive";
public static final String PROP_EXTERNALID = "externalID";
```

[Back to the previous chapter](upload_content_liferay.md)<br>
[Back to tutorial overview](index.md)<br>
[Leave the tutoral](../index.md)
