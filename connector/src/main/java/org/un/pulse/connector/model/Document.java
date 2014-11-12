package org.un.pulse.connector.model;

/**
 * Created with IntelliJ IDEA.
 * User: gregswensen
 * Date: 11/11/14
 * Time: 12:31 PM
 */
public class Document {
    public static enum DocumentType {
        pdf;
    }

    public DocumentType type;
    public String url;
    public String text;
    public String title;
    public int segment;
    public String createDate;

    @Override
    public String toString() {
        return "Document{" +
                "type=" + type +
                ", url='" + url + '\'' +
                ", text='" + text + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
