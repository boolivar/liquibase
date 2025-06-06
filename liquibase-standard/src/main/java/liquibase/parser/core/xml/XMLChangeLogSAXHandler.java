package liquibase.parser.core.xml;

import liquibase.Scope;
import liquibase.change.ChangeFactory;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.logging.Logger;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.precondition.PreconditionFactory;
import liquibase.resource.ResourceAccessor;
import liquibase.sql.visitor.SqlVisitorFactory;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Stack;

class XMLChangeLogSAXHandler extends DefaultHandler {

    private final ChangeFactory changeFactory;
    private final PreconditionFactory preconditionFactory;
    private final SqlVisitorFactory sqlVisitorFactory;
    private final ChangeLogParserFactory changeLogParserFactory;

    protected Logger log;

	private final DatabaseChangeLog databaseChangeLog;
	private final ResourceAccessor resourceAccessor;
	private final ChangeLogParameters changeLogParameters;
    private final Stack<ParsedNode> nodeStack = new Stack<>();
    private final Stack<StringBuilder> textStack = new Stack<>();
    private ParsedNode databaseChangeLogTree;


    protected XMLChangeLogSAXHandler(String physicalChangeLogLocation, ResourceAccessor resourceAccessor, ChangeLogParameters changeLogParameters) {
		log = Scope.getCurrentScope().getLog(getClass());
		this.resourceAccessor = resourceAccessor;

		databaseChangeLog = new DatabaseChangeLog();
		databaseChangeLog.setPhysicalFilePath(physicalChangeLogLocation);
		databaseChangeLog.setChangeLogParameters(changeLogParameters);

        if (changeLogParameters == null) {
            this.changeLogParameters = new ChangeLogParameters();
        } else {
            this.changeLogParameters = changeLogParameters;
        }

        changeFactory = Scope.getCurrentScope().getSingleton(ChangeFactory.class);
        preconditionFactory = PreconditionFactory.getInstance();
        sqlVisitorFactory = SqlVisitorFactory.getInstance();
        changeLogParserFactory = ChangeLogParserFactory.getInstance();
    }

	public DatabaseChangeLog getDatabaseChangeLog() {
		return databaseChangeLog;
	}

    public ParsedNode getDatabaseChangeLogTree() {
        return databaseChangeLogTree;
    }

    @Override
    public void characters(char ch[], int start, int length) {
        textStack.peek().append(new String(ch, start, length));
    }


    @Override
    public void startElement(String uri, String localName, String qualifiedName, Attributes attributes) throws SAXException {
        ParsedNode node = new ParsedNode(null, localName);
        try {
            if (attributes != null) {
                for (int i=0; i< attributes.getLength(); i++) {
                    try {
                        node.addChild(null, attributes.getLocalName(i), attributes.getValue(i));
                    } catch (NullPointerException e) {
                        throw e;
                    }
                }
            }
            if (!nodeStack.isEmpty()) {
                nodeStack.peek().addChild(node);
            }
            if (nodeStack.isEmpty()) {
                if(!node.getName().equals(ChangeLogParser.DATABASE_CHANGE_LOG)) {
                    throw new SAXParseException(String.format("\"%s\" expected as root element", ChangeLogParser.DATABASE_CHANGE_LOG), null);
                }
                databaseChangeLogTree = node;

            }
            nodeStack.push(node);
            textStack.push(new StringBuilder());
        } catch (ParsedNodeException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        ParsedNode node = nodeStack.pop();
        try {
            String seenText = this.textStack.pop().toString();
            if (!StringUtils.trimToEmpty(seenText).isEmpty()) {
                node.setValue(seenText.trim());
            }
        } catch (ParsedNodeException e) {
            throw new SAXException(e);
        }
    }
}
