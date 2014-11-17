
package org.unpulse.workflow.crawler;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;
import java.util.regex.Pattern;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import org.unpulse.workflow.AmazonDynamoHelper;
import org.unpulse.workflow.AmazonSqsHelper;

import java.util.List;


/**
 * Created by asundaram on 11/11/14.
 */
public class Crawler extends WebCrawler  {

    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g"
            + "|png|tiff?|mid|mp2|mp3|mp4"
            + "|wav|avi|mov|mpeg|ram|m4v|pdf"
            + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");


    /**
     * You should implement this function to specify whether
     * the given url should be crawled or not (based on your
     * crawling logic).
     */
    @Override
    public boolean shouldVisit(WebURL url) {
        String href = url.getURL().toLowerCase();
        return !FILTERS.matcher(href).matches() && href.startsWith("http://www.un.org/");
    }

    /**
     * This function is called when a page is fetched and ready
     * to be processed by your program.
     */
    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        System.out.println("URL: " + url);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            //String text = htmlParseData.getText();
            //String html = htmlParseData.getHtml();
            List<WebURL> links = htmlParseData.getOutgoingUrls();

            // Iterate through the links

            for(WebURL link : links){
                // Check if link is a PDFlink.
                String linkUrl = link.getURL();
                if(linkUrl.toLowerCase().endsWith(".pdf")){
                    // If the Link is a PDF check if its already there

                    if(!AmazonDynamoHelper.CheckIfAdded(linkUrl)){
                        // If not add the url to the Dynamo table and the queue
                        AmazonDynamoHelper.AddItemToDynamo(linkUrl);
                        AmazonSqsHelper.AddMessageToQueue(linkUrl);
                    }
                }
            }
        }
    }
}
