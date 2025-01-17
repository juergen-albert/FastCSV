package blackbox.reader;

import static blackbox.reader.CharacterConv.parse;
import static blackbox.reader.CharacterConv.print;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import de.siegmar.fastcsv.reader.CommentStrategy;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

class GenericDataTest {

    @ParameterizedTest
    @MethodSource("dataProvider")
    void dataTest(final DataProvider.TestData data) {
        final String expected = print(data.getExpected());
        final CommentStrategy commentStrategy = data.isReadComments()
            ? CommentStrategy.READ
            : data.isSkipComments() ? CommentStrategy.SKIP : CommentStrategy.NONE;
        final String actual = print(readAll(parse(data.getInput()), data.isSkipEmptyLines(),
            commentStrategy));
        assertEquals(expected, actual, () -> String.format("Error in line: '%s'", data));
    }

    static List<DataProvider.TestData> dataProvider() throws IOException {
        return DataProvider.loadTestData("/test.txt");
    }

    public static List<List<String>> readAll(final String data, final boolean skipEmptyLines,
                                             final CommentStrategy commentStrategy) {
        return CsvReader.builder()
            .skipEmptyRows(skipEmptyLines)
            .commentCharacter(';')
            .commentStrategy(commentStrategy)
            .build(data)
            .stream()
            .map(CsvRow::getFields)
            .collect(Collectors.toList());
    }

}
