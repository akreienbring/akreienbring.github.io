![License](img/cc-by-sa-88x31.png)<br>
This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License. To view a copy of this license, visit [http://creativecommons.org/licenses/by-sa/3.0/](http://creativecommons.org/licenses/by-sa/3.0/) or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.

[Back to the top](../index.md)<br>
[Back to tutorial overview](index.md)<br>
[Back to the previous chapter](getting_started_fuse.md)

## Chapter 4.: Using CMIS to download a document from Alfresco
Having the ID of an Alsfresco document in the server log file is not yet what we want. We need to download it to be able to upload it to Liferay later on.

One way to do this is the Camel-CMIS component that can be added to our route. 
But first we need to extract the information from the JSON message.

### From JSON text to exchange properties
JSON and XML are both very handy when one needs to extract information from the text. For XML there's XPATH and for JSON there's JSONPATH. What a luck that these languages are included in Fuse. Let's see how simple that is.

Go back to the Design mode of your route and drop a *Set Property* component on the *Log* component. It is hidden in the Transformation section of the palette.

![Set Property](img/fuse_setProperty_simple.png)



[Back to the previous chapter](getting_started_fuse.md)<br>
[Back to tutorial overview](index.md)<br>
[Leave the tutoral](../index.md)
