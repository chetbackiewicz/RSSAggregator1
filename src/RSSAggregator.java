import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.xmltree.XMLTree;
import components.xmltree.XMLTree1;

/**
 * Program to convert an XML RSS (version 2.0) feed from a given URL into the
 * corresponding HTML output file.
 *
 * @author Chet Backiewicz
 *
 */
public final class RSSAggregator {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private RSSAggregator() {
    }

    /**
     * Outputs the "opening" tags in the generated HTML file. These are the
     * expected elements generated by this method:
     *
     * <html> <head> <title>the channel tag title as the page title</title>
     * </head> <body>
     * <h1>the page title inside a link to the <channel> link</h1>
     * <p>
     * the channel description
     * </p>
     * <table border="1">
     * <tr>
     * <th>Date</th>
     * <th>Source</th>
     * <th>News</th>
     * </tr>
     *
     * @param channel
     *            the channel element XMLTree
     * @param out
     *            the output stream
     * @updates out.content
     * @requires [the root of channel is a <channel> tag] and out.is_open
     * @ensures out.content = #out.content * [the HTML "opening" tags]
     */
    private static void outputHeader(XMLTree channel, SimpleWriter out) {
        assert channel != null : "Violation of: channel is not null";
        assert out != null : "Violation of: out is not null";
        assert channel.isTag() && channel.label().equals("channel") : ""
                + "Violation of: the label root of channel is a <channel> tag";
        assert out.isOpen() : "Violation of: out.is_open";

        String title = "Empty Title";
        String description = "No description";

        out.println("<html>");
        out.println("<head>");

        out.println("<title>");
        int titleIndex = getChildElement(channel, "title");

        if (titleIndex != -1) {
            title = channel.child(titleIndex).child(0).toString();
        }

        out.println(title);

        out.println("</title>");

        out.println("</head>");
        out.println("<body>");

        out.println("<h1>");
        int linkIndex = getChildElement(channel, "link");
        String link = channel.child(linkIndex).child(0).toString();
        out.println("<a href=\"" + link + "\"> " + title + "</a>");
        out.println("</h1>");

        out.println("<p>");
        int desIndex = getChildElement(channel, "description");
        if (desIndex != -1) {
            description = channel.child(desIndex).child(0).toString();
        }
        out.println(description);

        out.println("</p>");

        //Output table first row
        out.println("<table border=\"1\">");
        out.println("<tr>");
        out.println("<th>");
        out.println("Date");
        out.println("</th>");
        out.println("<th>");
        out.println("Source");
        out.println("</th>");
        out.println("<th>");
        out.println("News");
        out.println("</th>");
        out.println("</tr>");
    }

    /**
     * Outputs the "closing" tags in the generated HTML file. These are the
     * expected elements generated by this method:
     *
     * </table>
     * </body> </html>
     *
     * @param out
     *            the output stream
     * @updates out.contents
     * @requires out.is_open
     * @ensures out.content = #out.content * [the HTML "closing" tags]
     */
    private static void outputFooter(SimpleWriter out) {
        assert out != null : "Violation of: out is not null";
        assert out.isOpen() : "Violation of: out.is_open";

        // Output footer
        out.println("</table>");
        out.println("</body>");
        out.println("</html>");

    }

    /**
     * Finds the first occurrence of the given tag among the children of the
     * given {@code XMLTree} and return its index; returns -1 if not found.
     *
     * @param xml
     *            the {@code XMLTree} to search
     * @param tag
     *            the tag to look for
     * @return the index of the first child of type tag of the {@code XMLTree}
     *         or -1 if not found
     * @requires [the label of the root of xml is a tag]
     * @ensures <pre>
     * getChildElement =
     *  [the index of the first child of type tag of the {@code XMLTree} or
     *   -1 if not found]
     * </pre>
     */
    private static int getChildElement(XMLTree xml, String tag) {
        assert xml != null : "Violation of: xml is not null";
        assert tag != null : "Violation of: tag is not null";
        assert xml.isTag() : "Violation of: the label root of xml is a tag";

        int index = -1;

        //Use loop to check for tag existence as a child of xml
        for (int i = 0; i < xml.numberOfChildren(); i++) {

            if (xml.child(i).label().equals(tag)) {
                index = i;
            }
        }

        return index;
    }

    /**
     * Processes one news item and outputs one table row. The row contains three
     * elements: the publication date, the source, and the title (or
     * description) of the item.
     *
     * @param item
     *            the news item
     * @param out
     *            the output stream
     * @updates out.content
     * @requires [the label of the root of item is an <item> tag] and
     *           out.is_open
     * @ensures <pre>
     * out.content = #out.content *
     *   [an HTML table row with publication date, source, and title of news item]
     * </pre>
     */
    private static void processItem(XMLTree item, SimpleWriter out) {
        assert item != null : "Violation of: item is not null";
        assert out != null : "Violation of: out is not null";
        assert item.isTag() && item.label().equals("item") : ""
                + "Violation of: the label root of item is an <item> tag";
        assert out.isOpen() : "Violation of: out.is_open";

        String date = "No date available";
        String source = "No source available";
        String url = "";
        String title = "No title available";
        String description = "No title available";
        String link = "";

        //Prints Date Section
        out.println("<tr>");
        int dateIndex = getChildElement(item, "pubDate");
        if (dateIndex != -1) {
            date = item.child(dateIndex).child(0).toString();
        }

        out.println("<td>");
        out.println(date);
        out.println("</td>");

        //Prints source section
        int sourceIndex = getChildElement(item, "source");
        if (sourceIndex != -1) {
            source = item.child(sourceIndex).child(0).label();
            url = item.child(sourceIndex).attributeValue("url");
        }

        out.println("<td>");
        if (url.length() > 0) {
            out.println("<a href=\"" + url + "\">" + source + "</a>");
        } else {
            out.println(source);
        }
        out.println("</td>");

        //Prints news section
        int titleIndex = getChildElement(item, "title");
        int descriptionIndex = getChildElement(item, "description");
        int linkIndex = getChildElement(item, "link");

        //If title, link, or description exist, set them to a new value
        if (titleIndex != -1
                && item.child(titleIndex).numberOfChildren() != 0) {
            title = item.child(titleIndex).child(0).label();
        }
        if (linkIndex != -1 && item.child(linkIndex).numberOfChildren() != 0) {
            link = item.child(linkIndex).child(0).label();
        }
        if (descriptionIndex != -1
                && item.child(descriptionIndex).numberOfChildren() != 0) {
            description = item.child(descriptionIndex).child(0).toString();

        }

        out.println("<td>");
        if (linkIndex != -1) {
            out.print("<a href=\"");
            out.print(link);
            out.print("\">");
        }

        if (titleIndex != -1
                && item.child(titleIndex).numberOfChildren() != 0) {
            out.print(title);
        } else if (descriptionIndex != -1
                && item.child(descriptionIndex).numberOfChildren() != 0) {
            out.print((description).replaceAll("\\<.*?>", ""));
        }

        if (linkIndex != -1) {
            out.println("</a>");
        }

        out.println("</td>");

        out.println("</tr>");

    }

    /**
     * Processes one XML RSS (version 2.0) feed from a given URL converting it
     * into the corresponding HTML output file.
     *
     * @param url
     *            the URL of the RSS feed
     * @param file
     *            the name of the HTML output file
     * @param out
     *            the output stream to report progress or errors
     * @updates out.content
     * @requires out.is_open
     * @ensures <pre>
     * [reads RSS feed from url, saves HTML document with table of news items
     *   to file, appends to out.content any needed messages]
     * </pre>
     */
    private static void processFeed(String url, String file, SimpleWriter out) {

        XMLTree rss = new XMLTree1(url);
        XMLTree channel = rss.child(0);
        String htmlFile = file;
        SimpleWriter outFile = new SimpleWriter1L(htmlFile);

        outputHeader(channel, outFile);

        for (int i = 0; i < channel.numberOfChildren(); i++) {
            if (channel.child(i).isTag()
                    && channel.child(i).label().equals("item")) {
                processItem(channel.child(i), outFile);
            }
        }

        outputFooter(outFile);

        outFile.close();

    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();

        out.print("Enter a name of an XML file containing valid URLs: ");
        String xmlFile = in.nextLine();
        XMLTree xml = new XMLTree1(xmlFile);

        //Creates an html file with unordered list
        if (xml.label().toString().equals("feeds") && xml.hasAttribute("title")
                && xml.numberOfChildren() > 0) {
            out.print("Enter the name of a file ending in .html: ");
            String htmlFile = in.nextLine();
            SimpleWriter outFile = new SimpleWriter1L(htmlFile);

            String title = "Empty Title";
            if (xml.hasAttribute("title")) {
                title = xml.attributeValue("title");
            }

            outFile.println("<html>");
            outFile.println("<head>");
            outFile.println("<title>");
            outFile.println(title);
            outFile.println("</title>");
            outFile.println("</head>");
            outFile.println("<h1>");
            outFile.println(title);
            outFile.println("</h1>");

            //creates unordered list
            outFile.println("<ul>");
            for (int i = 0; i < xml.numberOfChildren(); i++) {
                if (xml.child(i).label().equals("feed")) {
                    String indexFile = xml.child(i).attributeValue("file");

                    SimpleWriter feedWriter = new SimpleWriter1L(indexFile);

                    String url = xml.child(i).attributeValue("url");
                    String listItem = xml.child(i).attributeValue("name");

                    processFeed(url, indexFile, feedWriter);

                    outFile.println("<li>");
                    outFile.println("<a href=\"" + indexFile + "\">" + listItem
                            + "</a>");
                    outFile.println("</li>");

                    feedWriter.close();
                }

            }
            outFile.println("</ul>");
            outFile.println("</body>");
            outFile.println("</html>");
            outFile.close();

        } else {
            out.print("Invalid file");
        }

        in.close();
        out.close();
    }

}
