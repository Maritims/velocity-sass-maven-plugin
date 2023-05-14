package io.github.maritims;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;

public class UpdateStylesheetsMojoTest {
    public static Stream<Arguments> compileScss() {
        return Stream.of(
                Arguments.arguments("nonexistent.vm", false),
                Arguments.arguments("empty.vm", false),
                Arguments.arguments("valid.vm", true),
                Arguments.arguments("validWithAdditionalContent.vm", true)
        );
    }

    @ParameterizedTest(name = "When the filename is {0} we expect success to be {1}")
    @MethodSource
    void compileScss(String fileName, boolean expectedResult) {
        // arrange
        UpdateStylesheetsMojo sut = spy(new UpdateStylesheetsMojo());
        Path path = Paths.get("src", "test", "resources", fileName);

        // act
        boolean result = sut.compileScss(path.toFile());

        // assert
        assertEquals(expectedResult, result);
    }
}
