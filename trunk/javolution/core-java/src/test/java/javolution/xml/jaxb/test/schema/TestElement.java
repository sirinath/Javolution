//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.08.17 at 09:13:55 PM EDT 
//


package javolution.xml.jaxb.test.schema;

import java.math.BigDecimal;
import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for testElement complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="testElement">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="testIntElement" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="testLongElement" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="testBooleanElement" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="testStringElement" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="testFloatElement" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="testDoubleElement" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="testDateElement" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="testShortElement" type="{http://www.w3.org/2001/XMLSchema}short" minOccurs="0"/>
 *         &lt;element name="testByteElement" type="{http://www.w3.org/2001/XMLSchema}byte" minOccurs="0"/>
 *         &lt;element name="testDecimalElement" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="testIntegerElement" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "testElement", propOrder = {
    "testIntElement",
    "testLongElement",
    "testBooleanElement",
    "testStringElement",
    "testFloatElement",
    "testDoubleElement",
    "testDateElement",
    "testShortElement",
    "testByteElement",
    "testDecimalElement",
    "testIntegerElement"
})
public class TestElement {

    protected Integer testIntElement;
    protected Long testLongElement;
    protected Boolean testBooleanElement;
    protected String testStringElement;
    protected Float testFloatElement;
    protected Double testDoubleElement;
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar testDateElement;
    protected Short testShortElement;
    protected Byte testByteElement;
    protected BigDecimal testDecimalElement;
    protected BigInteger testIntegerElement;

    /**
     * Gets the value of the testIntElement property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getTestIntElement() {
        return testIntElement;
    }

    /**
     * Sets the value of the testIntElement property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTestIntElement(Integer value) {
        this.testIntElement = value;
    }

    /**
     * Gets the value of the testLongElement property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getTestLongElement() {
        return testLongElement;
    }

    /**
     * Sets the value of the testLongElement property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setTestLongElement(Long value) {
        this.testLongElement = value;
    }

    /**
     * Gets the value of the testBooleanElement property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isTestBooleanElement() {
        return testBooleanElement;
    }

    /**
     * Sets the value of the testBooleanElement property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTestBooleanElement(Boolean value) {
        this.testBooleanElement = value;
    }

    /**
     * Gets the value of the testStringElement property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTestStringElement() {
        return testStringElement;
    }

    /**
     * Sets the value of the testStringElement property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTestStringElement(String value) {
        this.testStringElement = value;
    }

    /**
     * Gets the value of the testFloatElement property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getTestFloatElement() {
        return testFloatElement;
    }

    /**
     * Sets the value of the testFloatElement property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setTestFloatElement(Float value) {
        this.testFloatElement = value;
    }

    /**
     * Gets the value of the testDoubleElement property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getTestDoubleElement() {
        return testDoubleElement;
    }

    /**
     * Sets the value of the testDoubleElement property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setTestDoubleElement(Double value) {
        this.testDoubleElement = value;
    }

    /**
     * Gets the value of the testDateElement property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTestDateElement() {
        return testDateElement;
    }

    /**
     * Sets the value of the testDateElement property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTestDateElement(XMLGregorianCalendar value) {
        this.testDateElement = value;
    }

    /**
     * Gets the value of the testShortElement property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getTestShortElement() {
        return testShortElement;
    }

    /**
     * Sets the value of the testShortElement property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setTestShortElement(Short value) {
        this.testShortElement = value;
    }

    /**
     * Gets the value of the testByteElement property.
     * 
     * @return
     *     possible object is
     *     {@link Byte }
     *     
     */
    public Byte getTestByteElement() {
        return testByteElement;
    }

    /**
     * Sets the value of the testByteElement property.
     * 
     * @param value
     *     allowed object is
     *     {@link Byte }
     *     
     */
    public void setTestByteElement(Byte value) {
        this.testByteElement = value;
    }

    /**
     * Gets the value of the testDecimalElement property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTestDecimalElement() {
        return testDecimalElement;
    }

    /**
     * Sets the value of the testDecimalElement property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTestDecimalElement(BigDecimal value) {
        this.testDecimalElement = value;
    }

    /**
     * Gets the value of the testIntegerElement property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTestIntegerElement() {
        return testIntegerElement;
    }

    /**
     * Sets the value of the testIntegerElement property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTestIntegerElement(BigInteger value) {
        this.testIntegerElement = value;
    }

}
