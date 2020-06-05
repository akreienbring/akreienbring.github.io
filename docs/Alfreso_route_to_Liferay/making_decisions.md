![License](img/cc-by-sa-88x31.png)<br>
This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License. To view a copy of this license, visit [http://creativecommons.org/licenses/by-sa/3.0/](http://creativecommons.org/licenses/by-sa/3.0/) or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.

[Leave the tutorial](../index.md)<br>
[Back to tutorial overview](index.md)<br>
[Back to the previous chapter](implement_crud.md)

## Chapter 8: Making decisions on the route
Up to this point our way was straight forward. We got a message of the form
```
{
	"alfrescoID":"8d03bfbc-ed24-4f96-8c4a-fc8f333b7b37",
	"action":"create"
}
```

and our route created the Alfresco document in Liferay and submitted the Liferay ID  of this document back to Alfresco.

But now messages can also look like this
```
{
	"alfrescoID":"8d03bfbc-ed24-4f96-8c4a-fc8f333b7b37",
	"liferayID":32607
	"action":"delete"
}
```
```
{
	"alfrescoID":"8d03bfbc-ed24-4f96-8c4a-fc8f333b7b37",
	"liferayID":32607
	"action":"update"
}
```
and the route has to make decisions.

### Routing with When - Otherwise
The start of the route looks like this

![The Start of the route](img/start_of_route.png)

As we need to grap the liferayID in a new property drop a new *Set Property* component from the palette precisely on the arrow between the existing *Set Property* components. Use the *jsonPath* expression *$.liferayID* to get it and set the property name to *liferayID*



[Back to the previous chapter](prepare_crud.md)<br>
[Back to tutorial overview](index.md)<br>
[Leave the tutorial](../index.md)
