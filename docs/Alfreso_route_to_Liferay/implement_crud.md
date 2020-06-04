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

*afterCreateVersion* is triggered when a new version of a document is uploaded to the repository. So let's go for it.

One more Thought before we start coding: I first just wanted to use a simple rule that triggers the already existing
```enable-web-flag```
```disable-web-flag```
actions. But the rule configuration in Alfresco does not provide a possibility to react on the upload of a new version out of the box.



[Back to the previous chapter](prepare_crud.md)<br>
[Back to tutorial overview](index.md)<br>
[Leave the tutorial](../index.md)
