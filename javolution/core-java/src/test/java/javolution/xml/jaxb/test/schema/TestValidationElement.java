//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.08.02 at 09:04:17 PM EDT 
//


package javolution.xml.jaxb.test.schema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for testValidationElement complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="testValidationElement">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="testUnboundedEnumElement" type="{http://javolution.org/xml/schema/javolution}testEnumElement" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="testRequiredLongElement" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/sequence>
 *       &lt;attribute name="testRequiredAttribute" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "testValidationElement", propOrder = {
    "testUnboundedEnumElement",
    "testRequiredLongElement"
})
public class TestValidationElement {

    @XmlSchemaType(name = "string")
    protected List<TestEnumElement> testUnboundedEnumElement;
    protected long testRequiredLongElement;
    @XmlAttribute(name = "testRequiredAttribute", required = true)
    protected String testRequiredAttribute;

    /**
     * Gets the value of the testUnboundedEnumElement property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the testUnboundedEnumElement property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTestUnboundedEnumElement().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TestEnumElement }
     * 
     * 
     */
    public List<TestEnumElement> getTestUnboundedEnumElement() {
        if (testUnboundedEnumElement == null) {
            testUnboundedEnumElement = new ArrayList<TestEnumElement>();
        }
        return this.testUnboundedEnumElement;
    }

    /**
     * Gets the value of the testRequiredLongElement property.
     * 
     */
    public long getTestRequiredLongElement() {
        return testRequiredLongElement;
    }

    /**
     * Sets the value of the testRequiredLongElement property.
     * 
     */
    public void setTestRequiredLongElement(long value) {
        this.testRequiredLongElement = value;
    }

    /**
     * Gets the value of the testRequiredAttribute property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTestRequiredAttribute() {
        return testRequiredAttribute;
    }

    /**
     * Sets the value of the testRequiredAttribute property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTestRequiredAttribute(String value) {
        this.testRequiredAttribute = value;
    }

}
