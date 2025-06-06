package liquibase.serializer.core.xml;

import liquibase.Labels;
import liquibase.change.AddColumnConfig;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.*;
import liquibase.change.custom.CustomChangeWrapper;
import liquibase.changelog.ChangeSet;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.precondition.CustomPreconditionWrapper;
import liquibase.precondition.core.SequenceExistsPrecondition;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.statement.SequenceNextValueFunction;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class XMLChangeLogSerializerTest {
    @Test
    public void createNode_addAutoIncrementChange() throws Exception {
        AddAutoIncrementChange change = new AddAutoIncrementChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setColumnName("COLUMN_NAME");
        change.setColumnDataType("DATATYPE(255)");

        Element node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(change);

        assertEquals("addAutoIncrement", node.getTagName());

        NamedNodeMap attributes = node.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            if ("schemaName".equals(attribute.getNodeName())) {
                assertEquals("SCHEMA_NAME", attribute.getNodeValue());
            } else if ("tableName".equals(attribute.getNodeName())) {
                assertEquals("TABLE_NAME", attribute.getNodeValue());
            } else if ("columnName".equals(attribute.getNodeName())) {
                assertEquals("COLUMN_NAME", attribute.getNodeValue());
            } else if ("columnDataType".equals(attribute.getNodeName())) {
                assertEquals("DATATYPE(255)", attribute.getNodeValue());
            } else {
                fail("unexpected attribute " + attribute.getNodeName());
            }
        }
    }

    @Test
    public void createNode_addColumnChange() throws Exception {
        AddColumnChange refactoring = new AddColumnChange();
        refactoring.setTableName("TAB");
        AddColumnConfig column = new AddColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");
        refactoring.addColumn(column);

        Element node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(refactoring);
        assertEquals("addColumn", node.getTagName());
        assertEquals("TAB", node.getAttribute("tableName"));

        NodeList columns = node.getElementsByTagName("column");
        assertEquals(1, columns.getLength());
        assertEquals("column", ((Element) columns.item(0)).getTagName());
        assertEquals("NEWCOL", ((Element) columns.item(0)).getAttribute("name"));
        assertEquals("TYP", ((Element) columns.item(0)).getAttribute("type"));
    }

    @Test
    public void createNode_AddDefaultValueChange() throws Exception {
        AddDefaultValueChange change = new AddDefaultValueChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setColumnName("COLUMN_NAME");
        change.setDefaultValue("DEF STRING");
        change.setDefaultValueNumeric("42");
        change.setDefaultValueBoolean(true);
        change.setDefaultValueDate("2007-01-02");
        change.setDefaultValueSequenceNext(new SequenceNextValueFunction("sampleSeq"));

        Element node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(change);
        assertEquals("addDefaultValue", node.getTagName());
        assertEquals("SCHEMA_NAME", node.getAttribute("schemaName"));
        assertEquals("TABLE_NAME", node.getAttribute("tableName"));
        assertEquals("COLUMN_NAME", node.getAttribute("columnName"));
        assertEquals("DEF STRING", node.getAttribute("defaultValue"));
        assertEquals("42", node.getAttribute("defaultValueNumeric"));
        assertEquals("true", node.getAttribute("defaultValueBoolean"));
        assertEquals("2007-01-02", node.getAttribute("defaultValueDate"));
        assertEquals("sampleSeq", node.getAttribute("defaultValueSequenceNext"));
    }

    @Test
    public void createNode_AddForeignKeyConstraintChange() throws Exception {
        AddForeignKeyConstraintChange change = new AddForeignKeyConstraintChange();
        change.setConstraintName("FK_NAME");

        change.setBaseTableSchemaName("BASE_SCHEMA_NAME");
        change.setBaseTableName("BASE_TABLE_NAME");
        change.setBaseColumnNames("BASE_COL_NAME");

        change.setReferencedTableSchemaName("REF_SCHEMA_NAME");
        change.setReferencedTableName("REF_TABLE_NAME");
        change.setReferencedColumnNames("REF_COL_NAME");

        change.setDeferrable(true);
        change.setOnDelete("CASCADE");
        change.setOnUpdate("CASCADE");
        change.setInitiallyDeferred(true);
        change.setValidate(true);

        Element node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(change);
        assertEquals("addForeignKeyConstraint", node.getTagName());
        assertEquals("FK_NAME", node.getAttribute("constraintName"));
        assertEquals("BASE_SCHEMA_NAME", node.getAttribute("baseTableSchemaName"));
        assertEquals("BASE_TABLE_NAME", node.getAttribute("baseTableName"));
        assertEquals("BASE_COL_NAME", node.getAttribute("baseColumnNames"));
        assertEquals("REF_SCHEMA_NAME", node.getAttribute("referencedTableSchemaName"));
        assertEquals("REF_TABLE_NAME", node.getAttribute("referencedTableName"));
        assertEquals("REF_COL_NAME", node.getAttribute("referencedColumnNames"));
        assertEquals("true", node.getAttribute("deferrable"));
        assertEquals("true", node.getAttribute("initiallyDeferred"));
        assertEquals("true", node.getAttribute("validate"));
        assertEquals("CASCADE", node.getAttribute("onDelete"));
        assertEquals("CASCADE", node.getAttribute("onUpdate"));

    }

    @Test
    public void createNode_AddNotNullConstraintChange() throws Exception {
        AddNotNullConstraintChange change = new AddNotNullConstraintChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");
        change.setDefaultNullValue("DEFAULT_VALUE");

        Element node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(change);
        assertEquals("addNotNullConstraint", node.getTagName());
        assertEquals("TABLE_NAME", node.getAttribute("tableName"));
        assertEquals("COL_HERE", node.getAttribute("columnName"));
        assertEquals("DEFAULT_VALUE", node.getAttribute("defaultNullValue"));
    }

    @Test
    public void createNode_AddPrimaryKeyChange() throws Exception {
        AddPrimaryKeyChange change = new AddPrimaryKeyChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setColumnNames("COL_HERE");
        change.setConstraintName("PK_NAME");
        change.setTablespace("TABLESPACE_NAME");
        change.setValidate(true);

        Element node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(change);
        assertEquals("addPrimaryKey", node.getTagName());
        assertEquals("SCHEMA_NAME", node.getAttribute("schemaName"));
        assertEquals("TABLE_NAME", node.getAttribute("tableName"));
        assertEquals("COL_HERE", node.getAttribute("columnNames"));
        assertEquals("PK_NAME", node.getAttribute("constraintName"));
        assertEquals("TABLESPACE_NAME", node.getAttribute("tablespace"));
        assertEquals("true", node.getAttribute("validate"));
    }

    @Test
    public void createNode_AddUniqueConstraintChange() throws Exception {
        AddUniqueConstraintChange change = new AddUniqueConstraintChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setColumnNames("COL_HERE");
        change.setConstraintName("PK_NAME");
        change.setTablespace("TABLESPACE_NAME");
        change.setDeferrable(true);
        change.setValidate(true);
        change.setInitiallyDeferred(true);
        change.setDisabled(true);

        Element node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(change);
        assertEquals("addUniqueConstraint", node.getTagName());
        assertEquals("SCHEMA_NAME", node.getAttribute("schemaName"));
        assertEquals("TABLE_NAME", node.getAttribute("tableName"));
        assertEquals("COL_HERE", node.getAttribute("columnNames"));
        assertEquals("PK_NAME", node.getAttribute("constraintName"));
        assertEquals("TABLESPACE_NAME", node.getAttribute("tablespace"));
        assertEquals("true", node.getAttribute("disabled"));
        assertEquals("true", node.getAttribute("deferrable"));
        assertEquals("true", node.getAttribute("initiallyDeferred"));
        assertEquals("true", node.getAttribute("validate"));
    }

    @Test
    public void createNode_AlterSequenceChange_nullValues() throws Exception {
        AlterSequenceChange refactoring = new AlterSequenceChange();
        refactoring.setSequenceName("SEQ_NAME");

        Element node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(refactoring);
        assertEquals("alterSequence", node.getNodeName());
        assertEquals("SEQ_NAME", node.getAttribute("sequenceName"));
        assertFalse(node.hasAttribute("incrementBy"));
        assertFalse(node.hasAttribute("maxValue"));
        assertFalse(node.hasAttribute("minValue"));
        assertFalse(node.hasAttribute("ordered"));
    }

    @Test
    public void createNode_AlterSequenceChange() throws Exception {
        AlterSequenceChange refactoring = new AlterSequenceChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setSequenceName("SEQ_NAME");
        refactoring.setIncrementBy(new BigInteger("1"));
        refactoring.setMaxValue(new BigInteger("2"));
        refactoring.setMinValue(new BigInteger("3"));
        refactoring.setOrdered(true);
        refactoring.setCacheSize(new BigInteger("2008"));

        Element node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(refactoring);
        assertEquals("alterSequence", node.getNodeName());
        assertEquals("SCHEMA_NAME", node.getAttribute("schemaName"));
        assertEquals("SEQ_NAME", node.getAttribute("sequenceName"));
        assertEquals("1", node.getAttribute("incrementBy"));
        assertEquals("2", node.getAttribute("maxValue"));
        assertEquals("3", node.getAttribute("minValue"));
        assertEquals("true", node.getAttribute("ordered"));
        assertEquals("2008", node.getAttribute("cacheSize"));
    }

    @Test
    public void createNode_ColumnConfig() throws Exception {
        ColumnConfig column = new ColumnConfig();
        column.setName("id");
        column.setType("varchar(255)");
        column.setDefaultValue("test Value");
        column.setValue("some value here");

        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setDeferrable(Boolean.TRUE);
        constraints.setValidateNullable(Boolean.TRUE);
        constraints.setValidateUnique(Boolean.TRUE);
        constraints.setValidatePrimaryKey(Boolean.TRUE);
        constraints.setValidateForeignKey(Boolean.TRUE);
        constraints.setDeleteCascade(true);
        constraints.setForeignKeyName("FK_NAME");
        constraints.setInitiallyDeferred(true);
        constraints.setNullable(false);
        constraints.setPrimaryKey(true);
        constraints.setReferences("state(id)");
        constraints.setUnique(true);
        column.setConstraints(constraints);

        Element element = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(column);
        assertEquals("column", element.getTagName());
        assertEquals("id", element.getAttribute("name"));
        assertEquals("varchar(255)", element.getAttribute("type"));
        assertEquals("test Value", element.getAttribute("defaultValue"));
        assertEquals("some value here", element.getAttribute("value"));

        Element constraintsElement = (Element) element.getChildNodes().item(0);
        assertEquals(12, constraintsElement.getAttributes().getLength());
        assertEquals("true", constraintsElement.getAttribute("deferrable"));
        assertEquals("true", constraintsElement.getAttribute("validateNullable"));
        assertEquals("true", constraintsElement.getAttribute("validateUnique"));
        assertEquals("true", constraintsElement.getAttribute("validatePrimaryKey"));
        assertEquals("true", constraintsElement.getAttribute("validateForeignKey"));
        assertEquals("true", constraintsElement.getAttribute("deleteCascade"));
        assertEquals("FK_NAME", constraintsElement.getAttribute("foreignKeyName"));
        assertEquals("true", constraintsElement.getAttribute("initiallyDeferred"));
        assertEquals("false", constraintsElement.getAttribute("nullable"));
        assertEquals("true", constraintsElement.getAttribute("primaryKey"));
        assertEquals("state(id)", constraintsElement.getAttribute("references"));
        assertEquals("true", constraintsElement.getAttribute("unique"));
    }

    @Test
    public void createNode_CreateIndexChange() throws Exception {
        CreateIndexChange refactoring = new CreateIndexChange();
        refactoring.setIndexName("IDX_TEST");
        refactoring.setTableName("TAB_NAME");

        AddColumnConfig column1 = new AddColumnConfig();
        column1.setName("COL1");
        refactoring.addColumn(column1);

        AddColumnConfig column2 = new AddColumnConfig();
        column2.setName("COL2");
        column2.setDescending(true);
        refactoring.addColumn(column2);

        Element element = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(refactoring);
        assertEquals("createIndex", element.getTagName());
        assertEquals("IDX_TEST", element.getAttribute("indexName"));
        assertEquals("TAB_NAME", element.getAttribute("tableName"));

        assertEquals(2, element.getChildNodes().getLength());
        assertEquals("column", ((Element) element.getChildNodes().item(0)).getTagName());
        assertEquals("COL1", ((Element) element.getChildNodes().item(0)).getAttribute("name"));
        assertEquals("column", ((Element) element.getChildNodes().item(1)).getTagName());
        assertEquals("COL2", ((Element) element.getChildNodes().item(1)).getAttribute("name"));
        assertEquals("true", ((Element) element.getChildNodes().item(1)).getAttribute("descending"));
    }

    @Test
    public void createNode_CreateProcedureChange() throws Exception {
        CreateProcedureChange refactoring = new CreateProcedureChange();
        refactoring.setProcedureText("CREATE PROC PROCBODY HERE");
        refactoring.setComment("Comments go here");

        Element element = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(refactoring);
        assertEquals("createProcedure", element.getTagName());
        assertEquals("CREATE PROC PROCBODY HERE", element.getTextContent());
    }

    @Test
    public void createNode_CreateSequenceChange() throws Exception {
        CreateSequenceChange change = new CreateSequenceChange();
        change.setSequenceName("SEQ_NAME");

        Element node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(change);
        assertEquals("createSequence", node.getNodeName());
        assertEquals("SEQ_NAME", node.getAttribute("sequenceName"));
        assertFalse(node.hasAttribute("incrementBy"));
        assertFalse(node.hasAttribute("maxValue"));
        assertFalse(node.hasAttribute("minValue"));
        assertFalse(node.hasAttribute("ordered"));
        assertFalse(node.hasAttribute("startValue"));
        assertFalse(node.hasAttribute("cycle"));

        change.setIncrementBy(new BigInteger("1"));
        change.setMaxValue(new BigInteger("2"));
        change.setMinValue(new BigInteger("3"));
        change.setOrdered(true);
        change.setStartValue(new BigInteger("4"));
        change.setCycle(true);

        node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(change);
        assertEquals("createSequence", node.getNodeName());
        assertEquals("SEQ_NAME", node.getAttribute("sequenceName"));
        assertEquals("1", node.getAttribute("incrementBy"));
        assertEquals("2", node.getAttribute("maxValue"));
        assertEquals("3", node.getAttribute("minValue"));
        assertEquals("true", node.getAttribute("ordered"));
        assertEquals("4", node.getAttribute("startValue"));
        assertEquals("true", node.getAttribute("cycle"));
    }

    @Test
    public void createNode_CreateTableChange() throws Exception {
        CreateTableChange change = new CreateTableChange();
        change.setTableName("TABLE_NAME");

        ColumnConfig column1 = new ColumnConfig();
        column1.setName("id");
        column1.setType("int");
        ConstraintsConfig column1constraints = new ConstraintsConfig();
        column1constraints.setPrimaryKey(true);
        column1constraints.setNullable(false);
        column1.setConstraints(column1constraints);
        change.addColumn(column1);

        ColumnConfig column2 = new ColumnConfig();
        column2.setName("name");
        column2.setType("varchar(255)");
        change.addColumn(column2);

        ColumnConfig column3 = new ColumnConfig();
        column3.setName("state_id");
        ConstraintsConfig column3constraints = new ConstraintsConfig();
        column3constraints.setNullable(false);
        column3constraints.setInitiallyDeferred(true);
        column3constraints.setDeferrable(true);
        column3constraints.setForeignKeyName("fk_tab_ref");
        column3constraints.setReferences("state(id)");
        column3.setConstraints(column3constraints);
        change.addColumn(column3);

        ColumnConfig column4 = new ColumnConfig();
        column4.setName("phone");
        column4.setType("varchar(255)");
        column4.setDefaultValue("NOPHONE");
        change.addColumn(column4);

        ColumnConfig column5 = new ColumnConfig();
        column5.setName("phone2");
        column5.setType("varchar(255)");
        ConstraintsConfig column5constraints = new ConstraintsConfig();
        column5constraints.setUnique(true);
        column5.setConstraints(column5constraints);
        change.addColumn(column5);

        Element element = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(change);
        assertEquals("createTable", element.getTagName());
        assertEquals(5, element.getChildNodes().getLength());

        Element columnElement = ((Element) element.getChildNodes().item(0));
        assertEquals("column", columnElement.getTagName());
        assertEquals("id", columnElement.getAttribute("name"));
        assertEquals("int", columnElement.getAttribute("type"));
        Element constraintsElement = (Element) columnElement.getChildNodes().item(0);
        assertEquals("constraints", constraintsElement.getTagName());
        assertEquals(2, constraintsElement.getAttributes().getLength());
        assertEquals("true", constraintsElement.getAttribute("primaryKey"));
        assertEquals("false", constraintsElement.getAttribute("nullable"));

        columnElement = ((Element) element.getChildNodes().item(1));
        assertEquals("column", columnElement.getTagName());
        assertEquals("name", columnElement.getAttribute("name"));
        assertEquals("varchar(255)", columnElement.getAttribute("type"));

        columnElement = ((Element) element.getChildNodes().item(2));
        assertEquals("column", columnElement.getTagName());
        assertEquals("state_id", columnElement.getAttribute("name"));
        constraintsElement = (Element) columnElement.getChildNodes().item(0);
        assertEquals("constraints", constraintsElement.getTagName());
        assertEquals(5, constraintsElement.getAttributes().getLength());
        assertEquals("false", constraintsElement.getAttribute("nullable"));
        assertEquals("true", constraintsElement.getAttribute("deferrable"));
        assertEquals("true", constraintsElement.getAttribute("initiallyDeferred"));
        assertEquals("fk_tab_ref", constraintsElement.getAttribute("foreignKeyName"));
        assertEquals("state(id)", constraintsElement.getAttribute("references"));

        columnElement = ((Element) element.getChildNodes().item(3));
        assertEquals("column", columnElement.getTagName());
        assertEquals("phone", columnElement.getAttribute("name"));
        assertEquals("varchar(255)", columnElement.getAttribute("type"));

        columnElement = ((Element) element.getChildNodes().item(4));
        assertEquals("column", columnElement.getTagName());
        assertEquals("phone2", columnElement.getAttribute("name"));
        assertEquals("varchar(255)", columnElement.getAttribute("type"));
        constraintsElement = (Element) columnElement.getChildNodes().item(0);
        assertEquals("constraints", constraintsElement.getTagName());
        assertEquals(1, constraintsElement.getAttributes().getLength());
        assertEquals("true", constraintsElement.getAttribute("unique"));
    }

    @Test
    public void createNodeDropColumnChange() throws Exception {
        DropColumnChange change = new DropColumnChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_NAME");

        Element node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(change);
        assertEquals("dropColumn", node.getTagName());
        assertFalse(node.hasAttribute("schemaName"));
        assertEquals("TABLE_NAME", node.getAttribute("tableName"));
        assertEquals("COL_NAME", node.getAttribute("columnName"));
    }

    @Test
    public void createNode_DropColumnChange_withSchema() throws Exception {
        DropColumnChange change = new DropColumnChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_NAME");

        Element node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(change);
        assertEquals("dropColumn", node.getTagName());
        assertEquals("SCHEMA_NAME", node.getAttribute("schemaName"));
        assertEquals("TABLE_NAME", node.getAttribute("tableName"));
        assertEquals("COL_NAME", node.getAttribute("columnName"));
    }

    @Test
    public void createNode_DropDefaultValueChange() throws Exception {
        DropDefaultValueChange change = new DropDefaultValueChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_NAME");

        Element node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(change);
        assertEquals("dropDefaultValue", node.getTagName());
        assertFalse(node.hasAttribute("schemaName"));
        assertEquals("TABLE_NAME", node.getAttribute("tableName"));
        assertEquals("COL_NAME", node.getAttribute("columnName"));
    }

    @Test
    public void createNode_DropDefaultValueChange_withSchema() throws Exception {
        DropDefaultValueChange change = new DropDefaultValueChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_NAME");

        Element node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(change);
        assertEquals("dropDefaultValue", node.getTagName());
        assertEquals("SCHEMA_NAME", node.getAttribute("schemaName"));
        assertEquals("TABLE_NAME", node.getAttribute("tableName"));
        assertEquals("COL_NAME", node.getAttribute("columnName"));
    }

    @Test
    public void createNode_DropForeignKeyConstraintChange() throws Exception {
        DropForeignKeyConstraintChange change = new DropForeignKeyConstraintChange();
        change.setBaseTableSchemaName("SCHEMA_NAME");
        change.setBaseTableName("TABLE_NAME");
        change.setConstraintName("FK_NAME");

        Element node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(change);
        assertEquals("dropForeignKeyConstraint", node.getTagName());
        assertEquals("SCHEMA_NAME", node.getAttribute("baseTableSchemaName"));
        assertEquals("TABLE_NAME", node.getAttribute("baseTableName"));
        assertEquals("FK_NAME", node.getAttribute("constraintName"));
    }

    @Test
    public void createNode_DropIndexChange() throws Exception {
        DropIndexChange refactoring = new DropIndexChange();
        refactoring.setIndexName("IDX_NAME");
        Element element = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(refactoring);

        assertEquals("dropIndex", element.getTagName());
        assertEquals("IDX_NAME", element.getAttribute("indexName"));
    }

    @Test
    public void createNode_DropNotNullConstraintChange() throws Exception {
        DropNotNullConstraintChange change = new DropNotNullConstraintChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");

        Element node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(change);
        assertEquals("dropNotNullConstraint", node.getTagName());
        assertEquals("TABLE_NAME", node.getAttribute("tableName"));
        assertEquals("COL_HERE", node.getAttribute("columnName"));
    }

    @Test
    public void createNode_DropPrimaryKeyChange() throws Exception {
        DropPrimaryKeyChange change = new DropPrimaryKeyChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setConstraintName("PK_NAME");
        Element node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(change);
        assertEquals("dropPrimaryKey", node.getTagName());
        assertEquals("SCHEMA_NAME", node.getAttribute("schemaName"));
        assertEquals("TABLE_NAME", node.getAttribute("tableName"));
        assertEquals("PK_NAME", node.getAttribute("constraintName"));
    }

    @Test
    public void createNode_DropSequenceChange() throws Exception {
        DropSequenceChange change = new DropSequenceChange();
        change.setSequenceName("SEQ_NAME");

        Element element = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(change);

        assertEquals("dropSequence", element.getTagName());
        assertEquals("SEQ_NAME", element.getAttribute("sequenceName"));
    }

    @Test
    public void createNode_DropTableChange() throws Exception {
        DropTableChange change = new DropTableChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TAB_NAME");
        change.setCascadeConstraints(true);

        Element element = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(change);
        assertEquals("dropTable", element.getTagName());
        assertEquals("TAB_NAME", element.getAttribute("tableName"));
        assertEquals("true", element.getAttribute("cascadeConstraints"));
    }

    @Test
    public void createNode_DropTableChange_withSchema() throws Exception {
        DropTableChange change = new DropTableChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TAB_NAME");
        change.setCascadeConstraints(true);

        Element element = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(change);
        assertEquals("dropTable", element.getTagName());
        assertEquals("TAB_NAME", element.getAttribute("tableName"));
        assertEquals("true", element.getAttribute("cascadeConstraints"));
        assertTrue(element.hasAttribute("schemaName"));
    }

    @Test
    public void createNode_nullConstraint() throws Exception {
        DropTableChange change = new DropTableChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TAB_NAME");
        change.setCascadeConstraints(null);

        Element element = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(change);
        assertEquals("dropTable", element.getTagName());
        assertFalse(element.hasAttribute("cascadeConstraints"));
    }

    @Test
    public void createNode_DropUniqueConstraintChange() throws Exception {
        DropUniqueConstraintChange change = new DropUniqueConstraintChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TAB_NAME");
        change.setConstraintName("UQ_CONSTRAINT");

        Element element = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(change);
        assertEquals("dropUniqueConstraint", element.getTagName());
        assertEquals("SCHEMA_NAME", element.getAttribute("schemaName"));
        assertEquals("TAB_NAME", element.getAttribute("tableName"));
        assertEquals("UQ_CONSTRAINT", element.getAttribute("constraintName"));
    }

    @Test
    public void createNode_DropUniqueConstraintChange_noSchema() throws Exception {
        DropUniqueConstraintChange change = new DropUniqueConstraintChange();
        change.setTableName("TAB_NAME");
        change.setConstraintName("UQ_CONSTRAINT");

        Element element = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(change);
        assertEquals("dropUniqueConstraint", element.getTagName());
        assertEquals("TAB_NAME", element.getAttribute("tableName"));
        assertEquals("UQ_CONSTRAINT", element.getAttribute("constraintName"));
        assertFalse(element.hasAttribute("schemaName"));
    }

    @Test
    public void createNode_DropViewChange() throws Exception {
        DropViewChange change = new DropViewChange();
        change.setViewName("VIEW_NAME");

        Element node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(change);
        assertEquals("dropView", node.getTagName());
        assertFalse(node.hasAttribute("schemaName"));
        assertEquals("VIEW_NAME", node.getAttribute("viewName"));
    }

    @Test
    public void createNode_DropViewChange_withSchema() throws Exception {
        DropViewChange change = new DropViewChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setViewName("VIEW_NAME");

        Element node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(change);
        assertEquals("dropView", node.getTagName());
        assertEquals("SCHEMA_NAME", node.getAttribute("schemaName"));
        assertEquals("VIEW_NAME", node.getAttribute("viewName"));
    }

    @Test
    public void createNode_InsertDataChange() throws Exception {
        InsertDataChange refactoring = new InsertDataChange();
        refactoring.setTableName("TABLE_NAME");

        ColumnConfig col1 = new ColumnConfig();
        col1.setName("id");
        col1.setValueNumeric("123");

        ColumnConfig col2 = new ColumnConfig();
        col2.setName("name");
        col2.setValue("Andrew");

        ColumnConfig col3 = new ColumnConfig();
        col3.setName("age");
        col3.setValueNumeric("21");

        ColumnConfig col4 = new ColumnConfig();
        col4.setName("height");
        col4.setValueNumeric("1.78");

        refactoring.addColumn(col1);
        refactoring.addColumn(col2);
        refactoring.addColumn(col3);
        refactoring.addColumn(col4);

        Element node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(refactoring);

        assertEquals("insert", node.getTagName());
        assertEquals("TABLE_NAME", node.getAttribute("tableName"));

        NodeList columns = node.getChildNodes();
        assertEquals(4, columns.getLength());

        assertEquals("column", ((Element) columns.item(0)).getTagName());
        assertEquals("id", ((Element) columns.item(0)).getAttribute("name"));
        assertEquals("123", ((Element) columns.item(0)).getAttribute("valueNumeric"));

        assertEquals("column", ((Element) columns.item(1)).getTagName());
        assertEquals("name", ((Element) columns.item(1)).getAttribute("name"));
        assertEquals("Andrew", ((Element) columns.item(1)).getAttribute("value"));

        assertEquals("column", ((Element) columns.item(2)).getTagName());
        assertEquals("age", ((Element) columns.item(2)).getAttribute("name"));
        assertEquals("21", ((Element) columns.item(2)).getAttribute("valueNumeric"));

        assertEquals("column", ((Element) columns.item(3)).getTagName());
        assertEquals("height", ((Element) columns.item(3)).getAttribute("name"));
        assertEquals("1.78", ((Element) columns.item(3)).getAttribute("valueNumeric"));
    }

    @Test
    public void createNode_LoadDataChange() throws Exception {
        LoadDataChange refactoring = new LoadDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("FILE_NAME");
        refactoring.setEncoding(StandardCharsets.UTF_8.name());

        Element node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(refactoring);
        assertEquals("loadData", node.getNodeName());
        assertEquals("SCHEMA_NAME", node.getAttribute("schemaName"));
        assertEquals("TABLE_NAME", node.getAttribute("tableName"));
        assertEquals("FILE_NAME", node.getAttribute("file"));
        assertEquals(StandardCharsets.UTF_8.name(), node.getAttribute("encoding"));
    }

    @Test
    public void createNode_RawSQLChange() throws Exception {
        RawSQLChange refactoring = new RawSQLChange();
        refactoring.setSql("SOME SQL HERE");

        Element element = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(refactoring);
        assertEquals("sql", element.getTagName());

        assertEquals("SOME SQL HERE", element.getTextContent());
    }

    @Test
    public void createNode_RenameColumnChange() throws Exception {
        RenameColumnChange refactoring = new RenameColumnChange();

        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setOldColumnName("oldColName");
        refactoring.setNewColumnName("newColName");


        Element node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(refactoring);
        assertEquals("renameColumn", node.getTagName());
        assertEquals("SCHEMA_NAME", node.getAttribute("schemaName"));
        assertEquals("TABLE_NAME", node.getAttribute("tableName"));
        assertEquals("oldColName", node.getAttribute("oldColumnName"));
        assertEquals("newColName", node.getAttribute("newColumnName"));
    }

    @Test
    public void createNode_RenameTableChange() throws Exception {
        RenameTableChange refactoring = new RenameTableChange();

        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setOldTableName("OLD_NAME");
        refactoring.setNewTableName("NEW_NAME");

        Element node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(refactoring);
        assertEquals("renameTable", node.getTagName());
        assertEquals("SCHEMA_NAME", node.getAttribute("schemaName"));
        assertEquals("OLD_NAME", node.getAttribute("oldTableName"));
        assertEquals("NEW_NAME", node.getAttribute("newTableName"));
    }

    @Test
    public void createNode_RenameSequenceChange() throws Exception {
        RenameSequenceChange refactoring = new RenameSequenceChange();

        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setOldSequenceName("OLD_NAME");
        refactoring.setNewSequenceName("NEW_NAME");

        Element node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(refactoring);
        assertEquals("renameSequence", node.getTagName());
        assertEquals("SCHEMA_NAME", node.getAttribute("schemaName"));
        assertEquals("OLD_NAME", node.getAttribute("oldSequenceName"));
        assertEquals("NEW_NAME", node.getAttribute("newSequenceName"));
    }

    @Test
    public void createNode_RenameViewChange() throws Exception {
        RenameViewChange refactoring = new RenameViewChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setOldViewName("OLD_NAME");
        refactoring.setNewViewName("NEW_NAME");

        Element node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(refactoring);
        assertEquals("renameView", node.getTagName());
        assertEquals("SCHEMA_NAME", node.getAttribute("schemaName"));
        assertEquals("OLD_NAME", node.getAttribute("oldViewName"));
        assertEquals("NEW_NAME", node.getAttribute("newViewName"));
    }

    @Test
    public void createNode_SQLFileChange() throws Exception {

        String fileName = "liquibase/change/core/SQLFileTestData.sql";
        SQLFileChange change = new SQLFileChange();
        ClassLoaderResourceAccessor opener = new ClassLoaderResourceAccessor();
        change.setPath(fileName);

        Element element = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(change);
        assertEquals("sqlFile", element.getTagName());

        assertEquals(fileName, element.getAttribute("path"));
    }

    @Test
    public void createNode_TagDatabaseChange() throws Exception {
        TagDatabaseChange refactoring = new TagDatabaseChange();
        refactoring.setTag("TAG_NAME");

        Element node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(refactoring);
        assertEquals("tagDatabase", node.getTagName());
        assertEquals("TAG_NAME", node.getAttribute("tag"));
    }


    @Test
    public void createNode_CreateViewChange() throws Exception {
        CreateViewChange change = new CreateViewChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setViewName("VIEW_NAME");
        change.setSelectQuery("SELECT * FROM EXISTING_TABLE");

        Element node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(change);
        assertEquals("createView", node.getTagName());
        assertEquals("SCHEMA_NAME", node.getAttribute("schemaName"));
        assertEquals("VIEW_NAME", node.getAttribute("viewName"));
        assertEquals("SELECT * FROM EXISTING_TABLE", node.getTextContent());
    }

    @Test
    public void createNode_CustomChange() throws Exception {
        CustomChangeWrapper change = new CustomChangeWrapper();
        change.setClass("liquibase.change.custom.ExampleCustomSqlChange");
        change.setParam("tableName", "tab_name");
        change.setParam("columnName", "col_name");
        change.setParam("newValue", "");

        Element node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(change);

        assertEquals("element name", "customChange", node.getTagName());
        assertEquals("customChange attributes",
                     attsMap("class", "liquibase.change.custom.ExampleCustomSqlChange"),
                     attsMap(node));

        NodeList params = node.getChildNodes();
        assertEquals("params count", 3, params.getLength());

        assertEquals("element name", "param", params.item(0).getNodeName());
        assertEquals("param[0] attributes",
                     attsMap("name", "tableName", "value", "tab_name"),
                     attsMap(params.item(0)));

        assertEquals("element name", "param", params.item(1).getNodeName());
        assertEquals("param[1] attributes",
                     attsMap("name", "columnName", "value", "col_name"),
                     attsMap(params.item(1)));

        assertEquals("element name", "param", params.item(2).getNodeName());
        assertEquals("param[2] attributes",
                     attsMap("name", "newValue", "value", ""),
                     attsMap(params.item(2)));
    }

    @Test
    public void createNode_CustomPrecondition() throws Exception {
        CustomPreconditionWrapper precondition = new CustomPreconditionWrapper();
        precondition.setClassName("liquibase.precondition.ExampleCustomPrecondition");
        precondition.setParam("name", "test_1");
        precondition.setParam("count", "31");

        Element node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(precondition);

        assertEquals("element name", "customPrecondition", node.getTagName());
        assertEquals("customPrecondition attributes",
                     attsMap("className", "liquibase.precondition.ExampleCustomPrecondition"),
                     attsMap(node));

        NodeList params = node.getChildNodes();
        assertEquals("params count", 2, params.getLength());

        assertEquals("element name", "param", params.item(0).getNodeName());
        assertEquals("param[0] attributes",
                     attsMap("name", "name", "value", "test_1"),
                     attsMap(params.item(0)));

        assertEquals("element name", "param", params.item(1).getNodeName());
        assertEquals("param[1] attributes",
                     attsMap("name", "count", "value", "31"),
                     attsMap(params.item(1)));
    }

    @Test
    public void createNode_ChangeSetParameters() throws Exception {
    	ChangeSet changeSet = new ChangeSet("1", "tms", true, true, "path/to/file.json", "context", "mssql",null,null, false,ObjectQuotingStrategy.LEGACY, null);
    	changeSet.setCreated("created");
    	changeSet.setFailOnError(true);
    	changeSet.setLabels(new Labels("label"));
    	changeSet.setLogicalFilePath("path/to/file.json");

    	Element node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(changeSet);

    	assertEquals("element name","changeSet",node.getTagName());
    	assertEquals("changeSet Attributes",
    			attsMap("id","1",
    					"author","tms",
    					"runAlways","true",
    					"runOnChange","true",
    					"logicalFilePath","path/to/file.json",
    					"contextFilter","context",
    					"dbms","mssql",
    					"objectQuotingStrategy","LEGACY",
    					"failOnError","true",
    					"labels","label",
    					"created","created",
                        "runInTransaction","false"),
    			attsMap(node));

    }

    @Test
    public void createNode_SequenceExistsPrecondition() throws Exception {
        SequenceExistsPrecondition prec = new SequenceExistsPrecondition();
        prec.setCatalogName("catalog1");
        prec.setSchemaName("schema2");
        prec.setSequenceName("seq3");

        Element node = new XMLChangeLogSerializer(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()).createNode(prec);

        assertEquals("element name", "sequenceExists", node.getTagName());
        assertEquals("sequenceExistsPrecondition attributes",
                attsMap("catalogName", "catalog1", "schemaName", "schema2", "sequenceName", "seq3"),
                attsMap(node));

        NodeList params = node.getChildNodes();
        assertEquals("params count", 0, params.getLength());
    }

    @Test
    public void serialize_pretty() {
        UpdateDataChange change = new UpdateDataChange();
        change.setCatalogName("a");
        change.setSchemaName("b");
        change.setTableName("c");
        change.setWhere("Some Text");

        String out = new XMLChangeLogSerializer().serialize(change, true);
        assertEquals("<update catalogName=\"a\"\n" +
                "        schemaName=\"b\"\n" +
                "        tableName=\"c\">\n" +
                "    <where>Some Text</where>\n" +
                "</update>", out);
    }

    @Test
    public void serialize_pretty_ChangeSetParameters() throws Exception {
    	ChangeSet changeSet = new ChangeSet("1", "tms", true, true, "path/to/file.json", "context", "mssql","runWith","runWithSpoolFile", false,ObjectQuotingStrategy.LEGACY, null);
    	changeSet.setCreated("created");
    	changeSet.setFailOnError(true);
    	changeSet.setLabels(new Labels("label"));
    	changeSet.setLogicalFilePath("path/to/file.json");
        changeSet.setIgnore(true);
        changeSet.setRunOrder("last");

    	String out = new XMLChangeLogSerializer().serialize(changeSet, true);

    	assertEquals("<changeSet author=\"tms\"\n"
    			+ "        contextFilter=\"context\"\n"
    			+ "        created=\"created\"\n"
    			+ "        dbms=\"mssql\"\n"
    			+ "        failOnError=\"true\"\n"
    			+ "        id=\"1\"\n"
                + "        ignore=\"true\"\n"
    			+ "        labels=\"label\"\n"
    			+ "        logicalFilePath=\"path/to/file.json\"\n"
    			+ "        objectQuotingStrategy=\"LEGACY\"\n"
    			+ "        runAlways=\"true\"\n"
                + "        runInTransaction=\"false\"\n"
    			+ "        runOnChange=\"true\"\n"
                + "        runOrder=\"last\"/>", out);
    }

    @Test
    public void serialize_pretty_nestedNodeWithAttributes() {
        CreateTableChange change = new CreateTableChange();
        change.setCatalogName("a");
        change.setSchemaName("b");
        change.setTableName("c");
        change.setIfNotExists(true);
        change.addColumn(new ColumnConfig().setName("x").setDefaultValue("x1"));
        change.addColumn(new ColumnConfig().setName("y").setDefaultValue("y1"));

        String out = new XMLChangeLogSerializer().serialize(change, true);
        assertEquals("<createTable catalogName=\"a\"\n" +
                "        ifNotExists=\"true\"\n" +
                "        schemaName=\"b\"\n" +
                "        tableName=\"c\">\n" +
                "    <column defaultValue=\"x1\" name=\"x\"/>\n" +
                "    <column defaultValue=\"y1\" name=\"y\"/>\n" +
                "</createTable>", out);
    }

    @Test
    public void serialize_pretty_nestedNodeWithAttributesAndNotExistsIgnored() {
        CreateTableChange change = new CreateTableChange();
        change.setCatalogName("a");
        change.setSchemaName("b");
        change.setTableName("c");
        change.addColumn(new ColumnConfig().setName("x").setDefaultValue("x1"));
        change.addColumn(new ColumnConfig().setName("y").setDefaultValue("y1"));

        String out = new XMLChangeLogSerializer().serialize(change, true);
        assertEquals("<createTable catalogName=\"a\"\n" +
                "        schemaName=\"b\"\n" +
                "        tableName=\"c\">\n" +
                "    <column defaultValue=\"x1\" name=\"x\"/>\n" +
                "    <column defaultValue=\"y1\" name=\"y\"/>\n" +
                "</createTable>", out);
    }

    @Test
    public void serialize_pretty_justAttributes() {
        AddAutoIncrementChange change = new AddAutoIncrementChange();
        change.setCatalogName("a");
        change.setSchemaName("b");
        change.setTableName("c");

        String out = new XMLChangeLogSerializer().serialize(change, true);
        assertEquals("<addAutoIncrement catalogName=\"a\"\n" +
                "        schemaName=\"b\"\n" +
                "        tableName=\"c\"/>", out);
    }

    private static Map<String, String> attsMap(String... values) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            map.put(values[i], values[i + 1]);
        }
        return map;
    }

    private static Map<String, String> attsMap(Node elem) {
        Map<String, String> map = new LinkedHashMap<>();
        NamedNodeMap attributes = elem.getAttributes();
        for (int i = 0, len = attributes.getLength(); i < len; i++) {
            Node item = attributes.item(i);
            map.put(item.getNodeName(), item.getNodeValue());
        }
        return map;
    }

}
