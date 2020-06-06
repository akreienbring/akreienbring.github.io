package org.test;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.HttpMultipartMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

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
	        
	        //multipartEntityBuilder.addPart("file", new FileBody(file, ContentType.MULTIPART_FORM_DATA, filename));
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
