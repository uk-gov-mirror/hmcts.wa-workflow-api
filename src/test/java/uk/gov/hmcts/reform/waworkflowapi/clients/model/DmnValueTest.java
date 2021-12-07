package uk.gov.hmcts.reform.waworkflowapi.clients.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DmnValueTest {

    @Test
    void should_set_properties() {

        DmnValue<String> testObject = new DmnValue<>("0000000", "String");

        Assertions.assertEquals("String", testObject.getType());
        Assertions.assertEquals("0000000", testObject.getValue());
    }

    @Test
    void should_return_dmn_value_from_string() {


        DmnValue<String> testObject = DmnValue.dmnStringValue("someString");

        Assertions.assertEquals("String", testObject.getType());
        Assertions.assertEquals("someString", testObject.getValue());
    }

    @Test
    void should_return_dmn_value_from_integer() {

        DmnValue<Integer> testObject = DmnValue.dmnIntegerValue(100);

        Assertions.assertEquals("Integer", testObject.getType());
        Assertions.assertEquals(100, testObject.getValue());
    }

    @Test
    void should_return_dmn_value_from_json_string() {

        DmnValue<String> testObject = DmnValue.jsonValue("someJson");

        Assertions.assertEquals("json", testObject.getType());
        Assertions.assertEquals("someJson", testObject.getValue());
    }

    @Test
    void should_return_true_when_objects_are_the_same() {
        DmnValue<String> targetObj = DmnValue.jsonValue("someJson");

        Assertions.assertEquals(DmnValue.jsonValue("someJson"), targetObj);
    }

    @Test
    void should_return_false_when_object_is_null() {
        DmnValue<String> thisObj = DmnValue.jsonValue("someJson");

        Assertions.assertNotEquals(null, thisObj);
    }

    @Test
    void hashCode_should_not_be_the_same() {

        DmnValue<String> testObject1 = DmnValue.jsonValue("someJson");
        DmnValue<String> testObject2 = DmnValue.jsonValue("someOtherValues");
        Assertions.assertNotEquals(testObject1, testObject2);
        Assertions.assertNotEquals(testObject2.hashCode(), testObject1.hashCode());
    }

    @Test
    void hashCode_should_be_equal() {

        DmnValue<String> testObject1 = DmnValue.jsonValue("someJson");
        DmnValue<String> testObject2 = DmnValue.jsonValue("someJson");
        Assertions.assertEquals(testObject1, testObject2);
        Assertions.assertEquals(testObject2.hashCode(), testObject1.hashCode());
    }

    @Test
    void should_convert_value_to_string() {
        DmnValue<String> testObject1 = DmnValue.jsonValue("someJson");
        Assertions.assertEquals("DmnValue(value=someJson, type=json)", testObject1.toString());
    }
}
