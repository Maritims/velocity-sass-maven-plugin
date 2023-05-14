package io.github.maritims;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchService;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class WatchStylesheetsMojoTest {
    public static Stream<Arguments> compileScss() {
        return Stream.of(
                Arguments.arguments(Paths.get("target", "test-classes"), true, 1),
                Arguments.arguments(Paths.get("foo", "bar"), false, 0)
        );
    }

    @ParameterizedTest(name = "When the path is {0} we expect success to be {1} and the number of compilations to be {2}")
    @MethodSource
    void compileScss(Path dir, boolean isSuccessExpected, int expectedCompilations) throws IOException {
        // arrange
        Path file = dir.resolve(Paths.get("foo.vm"));
        if(isSuccessExpected) {
            Files.deleteIfExists(file);
            Files.createFile(file);
        }

        WatchStylesheetsMojo sut = spy(new WatchStylesheetsMojo());
        WatchService watcher = sut.watch(dir);
        if(isSuccessExpected) {
            Files.write(file, ("<style>" +
                    ".foo { background: #000; }" +
                    ".bar { @extend .foo; color: #FFF; }" +
                    "</style>").getBytes(StandardCharsets.UTF_8));
        }

        // act
        boolean success = sut.compileScss(watcher);

        // assert
        assertEquals(isSuccessExpected, success);
        verify(sut, times(expectedCompilations).description("Foo bar")).compileScss(any(File.class));
    }
}
