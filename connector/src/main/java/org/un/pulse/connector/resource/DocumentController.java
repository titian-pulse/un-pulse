package org.un.pulse.connector.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.un.pulse.connector.model.Document;
import org.un.pulse.connector.service.DocumentProcessor;
import org.un.pulse.connector.service.DocumentProcessor.DocumentReference;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: gregswensen
 * Date: 11/11/14
 * Time: 12:58 PM
 */
@RestController
public class DocumentController {

    @Autowired
    private DocumentProcessor processor;

    @RequestMapping(value = "index", method = RequestMethod.POST)
    public Document indexDocument(@RequestBody DocumentReference docRef) throws IOException {
        Document doc = processor.parseDocument(docRef);
        processor.indexDocument(doc, docRef.index);
        return doc;
    }

}
