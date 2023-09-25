import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The CSVParser class of my project, which takes in a reader, some information about the file, and
 * how to form rows, and produces a List of these rows that can then be used for Search.
 *
 * @param <T> the row Object that file rows will be converted into
 */
public class CSVParser<T> {
    private Boolean hasHeader;
    private CreatorFromRow<T> rowCreator;
    private BufferedReader bufferedReader;
    private T header;
    static final Pattern regex = Pattern.compile(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*(?![^\\\"]*\\\"))");

    /**
     * The constructor for Search, where the header of the file, if there is one, is separated from
     * the rest of the file data and any errors that might arise from loading and reading the file are
     * dealt with.
     *
     * @param reader the Reader object that provides the file given
     * @param hasHeader whether the file has a header
     * @param rowCreator the row creator that will create rows of type T
     */
    public CSVParser(Reader reader, Boolean hasHeader, CreatorFromRow<T> rowCreator) {
        this.hasHeader = hasHeader;
        this.rowCreator = rowCreator;
        this.bufferedReader = new BufferedReader(reader);
        if (hasHeader) {
            List<String> header = new ArrayList<>();
            try {
                String h = this.bufferedReader.readLine();
                String[] line = regex.split(h);
                header = Arrays.asList(line);

            } catch (IOException e) {
                System.err.println("Sorry, an error has occurred when reading the file.");
            }

            this.header = this.rowCreator.create(header);

        }
    }

    /**
     * The method to read each line of the CSV file and convert it into rows of type T, as well as
     * deal with any errors that may arise in this process.
     *
     * @return an ArrayList of all the rows of the file as Objects of type T
     */
    public ArrayList<T> parseCSVFile() {
        ArrayList<T> parsed = new ArrayList<>();

        try {
            for (String line = this.bufferedReader.readLine();
                 line != null;
                 line = this.bufferedReader.readLine()) {
                List<String> items = Arrays.asList(regex.split(line));

                T row = this.rowCreator.create(items);
                parsed.add(row);
            }
        } catch (IOException e) {
            System.err.println("Sorry, an error has occurred when reading the file.");
        }
        return parsed;
    }

    /**
     * A method to obtain the header as a row Object of type T, for use outside this class.
     *
     * @return the header data converted into a row Object of type T
     * @throws IOException if method is called for a file that has no header
     */
    public T getHeader() throws IOException {
        if (this.hasHeader) {
            return this.header;
        } else {
            throw new IOException();
        }
    }

    /**
     * A method to obtain a Boolean representing whether the file it is called for has a header.
     *
     * @return a Boolean representing whether the file has a header
     */
    public Boolean getHasHeader() {
        return this.hasHeader;
    }
}
