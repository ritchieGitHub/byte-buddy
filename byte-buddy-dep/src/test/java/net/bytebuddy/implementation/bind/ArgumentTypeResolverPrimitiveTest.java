package net.bytebuddy.implementation.bind;

import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
public class ArgumentTypeResolverPrimitiveTest extends AbstractArgumentTypeResolverTest {

    private final Class<?> firstType;

    private final Class<?> secondType;

    @Mock
    private TypeDescription firstPrimitive;

    @Mock
    private TypeDescription secondPrimitive;

    public ArgumentTypeResolverPrimitiveTest(Class<?> firstType, Class<?> secondType) {
        this.firstType = firstType;
        this.secondType = secondType;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {boolean.class, byte.class},
                {boolean.class, short.class},
                {boolean.class, char.class},
                {boolean.class, int.class},
                {boolean.class, long.class},
                {boolean.class, float.class},
                {boolean.class, double.class},

                {byte.class, short.class},
                {byte.class, char.class},
                {byte.class, int.class},
                {byte.class, long.class},
                {byte.class, float.class},
                {byte.class, double.class},

                {short.class, char.class},
                {short.class, int.class},
                {short.class, long.class},
                {short.class, float.class},
                {short.class, double.class},

                {char.class, long.class},
                {char.class, float.class},
                {char.class, double.class},

                {int.class, char.class},
                {int.class, long.class},
                {int.class, float.class},
                {int.class, double.class},

                {long.class, float.class},
                {long.class, double.class},

                {float.class, double.class},
        });
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(sourceType.isPrimitive()).thenReturn(true);
        when(firstPrimitive.isPrimitive()).thenReturn(true);
        when(firstPrimitive.represents(firstType)).thenReturn(true);
        when(secondPrimitive.isPrimitive()).thenReturn(true);
        when(secondPrimitive.represents(secondType)).thenReturn(true);
    }

    @Test
    public void testLeftDominance() throws Exception {
        testDominance(firstPrimitive, secondPrimitive, MethodDelegationBinder.AmbiguityResolver.Resolution.LEFT);
    }

    @Test
    public void testRightDominance() throws Exception {
        testDominance(secondPrimitive, firstPrimitive, MethodDelegationBinder.AmbiguityResolver.Resolution.RIGHT);
    }

    @Test
    public void testLeftNonDominance() throws Exception {
        testDominance(secondPrimitive, firstPrimitive, MethodDelegationBinder.AmbiguityResolver.Resolution.RIGHT);
    }

    @Test
    public void testRightNonDominance() throws Exception {
        testDominance(firstPrimitive, secondPrimitive, MethodDelegationBinder.AmbiguityResolver.Resolution.LEFT);
    }

    @Test
    public void testNonDominance() throws Exception {
        testDominance(firstPrimitive, firstPrimitive, MethodDelegationBinder.AmbiguityResolver.Resolution.AMBIGUOUS);
    }

    private void testDominance(TypeDescription leftPrimitive,
                               TypeDescription rightPrimitive,
                               MethodDelegationBinder.AmbiguityResolver.Resolution expected) throws Exception {
        when(sourceParameterList.size()).thenReturn(2);
        when(sourceType.isPrimitive()).thenReturn(true);
        ParameterDescription leftParameter = mock(ParameterDescription.class);
        when(leftParameter.getTypeDescription()).thenReturn(leftPrimitive);
        when(leftParameterList.get(0)).thenReturn(leftParameter);
        when(left.getTargetParameterIndex(any(ArgumentTypeResolver.ParameterIndexToken.class)))
                .thenAnswer(new TokenAnswer(new int[][]{{0, 0}}));
        ParameterDescription rightParameter = mock(ParameterDescription.class);
        when(rightParameter.getTypeDescription()).thenReturn(rightPrimitive);
        when(rightParameterList.get(0)).thenReturn(rightParameter);
        when(right.getTargetParameterIndex(any(ArgumentTypeResolver.ParameterIndexToken.class)))
                .thenAnswer(new TokenAnswer(new int[][]{{0, 0}}));
        MethodDelegationBinder.AmbiguityResolver.Resolution resolution =
                ArgumentTypeResolver.INSTANCE.resolve(source, left, right);
        assertThat(resolution, is(expected));
        verify(source, atLeast(1)).getParameters();
        verify(leftMethod, atLeast(1)).getParameters();
        verify(rightMethod, atLeast(1)).getParameters();
        verify(left, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(0)));
        verify(left, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(1)));
        verify(left, never()).getTargetParameterIndex(argThat(not(describesArgument(0, 1))));
        verify(right, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(0)));
        verify(right, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(1)));
        verify(right, never()).getTargetParameterIndex(argThat(not(describesArgument(0, 1))));
    }
}
