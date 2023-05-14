package io.github.maritims;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
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

    public static Stream<Arguments> onlyRecompileOnActualChange() {
        return Stream.of(
                Arguments.arguments("<style>.foo { background: black; }</style>", "<style>.foo { background: black; }</style>", false, 0),
                Arguments.arguments("<style>.foo { background: black; }</style>", "<style>.foo { background: white; }</style>", true, 1)
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
        WatchService watcher = sut.watch(dir, false);
        if(isSuccessExpected) {
            Files.write(file, ("<style>" +
                    ".foo { background: #000; }" +
                    ".bar { @extend .foo; color: #FFF; }" +
                    "</style>").getBytes(StandardCharsets.UTF_8));
        }

        // act
        boolean success = sut.compileScss(watcher, false);

        // assert
        assertEquals(isSuccessExpected, success);
        verify(sut, times(expectedCompilations).description("Foo bar")).compileScss(any(File.class));
    }

    @ParameterizedTest(name = "When old content is {0} and new content is {1} we expect success to be {2} and the number of compilations to be {3}")
    @MethodSource
    void onlyRecompileOnActualChange(String oldContent, String newContent, boolean expectedResult, int expectedCompilations) throws IOException {
        // arrange
        Path dir = Paths.get("target", "test-classes");
        Path file = dir.resolve("foo.vm");
        Files.deleteIfExists(file);
        Files.write(file, oldContent.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);
        WatchStylesheetsMojo sut = spy(new WatchStylesheetsMojo());
        WatchService watcher = sut.watch(dir, true);

        // act
        Files.write(file, newContent.getBytes(StandardCharsets.UTF_8));
        boolean success = sut.compileScss(watcher, true);

        // assert
        assertEquals(expectedResult, success);
        verify(sut, times(expectedCompilations).description("Foo bar")).compileScss(any(File.class));
    }
}
