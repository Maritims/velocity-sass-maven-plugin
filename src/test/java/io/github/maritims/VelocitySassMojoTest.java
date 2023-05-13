package io.github.maritims;

import org.jsoup.nodes.Document;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;

public class VelocitySassMojoTest {
    public static Stream<Arguments> compileScss() {
        return Stream.of(
                Arguments.arguments("nonexistent.vm", false),
                Arguments.arguments("empty.vm", false),
                Arguments.arguments("valid.vm", true),
                Arguments.arguments("validWithAdditionalContent.vm", true)
        );
    }

    @ParameterizedTest
    @MethodSource
    void compileScss(String fileName, boolean expectedResult) {
        // arrange
        VelocitySassMojo sut = spy(new VelocitySassMojo());
        Path path = Paths.get("src", "test", "resources", fileName);
        Document document = sut.getDocument(path.toFile());

        // act
        boolean result = sut.compileScss(document, System.out::println);

        // assert
        assertEquals(expectedResult, result);
    }
}
