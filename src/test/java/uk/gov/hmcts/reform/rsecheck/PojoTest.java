package uk.gov.hmcts.reform.rsecheck;

import com.google.common.testing.EqualsTester;
import com.jparams.verifier.tostring.ToStringVerifier;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.ClassAndFieldPredicatePair;
import pl.pojo.tester.api.assertion.Method;
import pl.pojo.tester.internal.field.DefaultFieldValueChanger;
import pl.pojo.tester.internal.instantiator.ObjectGenerator;
import pl.pojo.tester.internal.utils.ThoroughFieldPermutator;
import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.CreateTaskRequest;
import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.Transition;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsForAll;

@SuppressWarnings({"unchecked", "PMD.JUnitTestsShouldIncludeAssert", "PMD.DataflowAnomalyAnalysis", "PMD.EqualsNull", "PMD.AvoidInstantiatingObjectsInLoops"})
public class PojoTest {

    private final Class[] classesToTest = {
        Transition.class,
        CreateTaskRequest.class
    };
    private final ObjectGenerator objectGenerator = new ObjectGenerator(
        DefaultFieldValueChanger.INSTANCE,
        new ArrayListValuedHashMap<>(),
        new ThoroughFieldPermutator()
    );
    private final EqualsTester equalsTester = new EqualsTester();

    @Test
    public void allPojosAreWellImplemented() {
        assertPojoMethodsForAll(
            classesToTest
        )
            .testing(Method.GETTER)
            .testing(Method.CONSTRUCTOR)
            .areWellImplemented();
    }

    @Test
    public void equalsTest() {
        for (Class classUnderTest : classesToTest) {
            Object newInstance = objectGenerator.createNewInstance(classUnderTest);
            equalsTester.addEqualityGroup(newInstance).testEquals();

            assertThat(
                "Check instance equals another instance that is equal",
                newInstance.equals(objectGenerator.createNewInstance(classUnderTest)),
                is(true)
            );
            List<Object> differentObjects = objectGenerator.generateDifferentObjects(new ClassAndFieldPredicatePair(
                classUnderTest));
            Object differentObject = differentObjects.get(1);
            assertThat(
                "Check instance does not equal another instance that is different \n" + newInstance + "\n" + differentObject,
                newInstance.equals(differentObject),
                is(false)
            );
        }
    }

    @Test
    public void verifyToStringTest() {
        ToStringVerifier.forClasses(classesToTest).verify();
    }
}

