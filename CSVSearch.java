import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The Search class of my project. Here, I use a given CSVParser to search through available data
 * for the given search term. There is also the option to provide a column identifier to limit the
 * search to one column of the data.
 */
public class CSVSearch {
    private Boolean hasHeader;
    private String search;
    private String nameIdentifier;
    private Integer indexIdentifier;
    private Boolean hasIdentifier;
    private Boolean isIndex;
    private CSVParser<List<String>> parser;

    /**
     * One of 3 Search constructors- used when the column identifier is given and is a name (not an
     * index).
     *
     * @param parser The parser of the given file to use when searching
     * @param search The search term itself, inputted by the user
     * @param identifier The column name identifier to limit the search
     */
    public CSVSearch(CSVParser<List<String>> parser, String search, String identifier) {
        this.parser = parser;
        this.hasHeader = parser.getHasHeader();
        this.search = search;
        this.nameIdentifier = identifier;
        this.isIndex = Boolean.FALSE;
        this.hasIdentifier = Boolean.TRUE;
    }

    /**
     * One of 3 Search constructors- used when the column identifier is given and is an index (not a
     * name).
     *
     * @param parser The parser of the given file to use when searching
     * @param search The search term itself, inputted by the user
     * @param identifier The column index identifier to limit the search
     */
    public CSVSearch(CSVParser<List<String>> parser, String search, Integer identifier) {
        this.parser = parser;
        this.hasHeader = parser.getHasHeader();
        this.search = search;
        this.indexIdentifier = identifier;
        this.isIndex = Boolean.TRUE;
        this.hasIdentifier = Boolean.TRUE;
    }

    /**
     * One of 3 Search constructors- used when no column identifier is given.
     *
     * @param parser The parser of the given file to use when searching
     * @param search The search term itself, inputted by the user
     */
    public CSVSearch(CSVParser<List<String>> parser, String search) {
        this.parser = parser;
        this.hasHeader = parser.getHasHeader();
        this.search = search;
        this.hasIdentifier = Boolean.FALSE;
    }

    /**
     * The main searching method, which goes through each row object produced by the CSVParser and
     * returns a list of the rows that contain the search term somewhere in the row (if there is no
     * column identifier), or the rows that contain the search term in the given column (if there is a
     * column identifier).
     *
     * @return an ArrayList of the rows containing the search term in the correct column (if
     *     applicable)
     */
    public ArrayList<List<String>> searchRows() {
        ArrayList<List<String>> searchedRows = new ArrayList<>();
        ArrayList<List<String>> parsedFile = this.parser.parseCSVFile();
        for (List<String> row : parsedFile) {
            for (String item : row) {
                if (this.search.equalsIgnoreCase(item)) {
                    if (!searchedRows.contains(row)) {
                        searchedRows.add(row);
                    }
                }
            }
        }
        if (this.hasHeader && this.hasIdentifier) {
            try {
                List<String> header = this.parser.getHeader();
                int index = -1;
                int i = 0;
                if (!this.isIndex) {
                    for (String item: header) {
                        if (item.equalsIgnoreCase(this.nameIdentifier)) {
                            index = i;
                        }
                        i++;
                    }
                } else {
                    index = this.indexIdentifier;
                }
                ArrayList<List<String>> columnSearchedRows = new ArrayList<>();
                for (List<String> row : searchedRows) {
                    if (row.get(index).equalsIgnoreCase(this.search)) {
                        columnSearchedRows.add(row);
                    }
                }
                searchedRows = columnSearchedRows;
            } catch (IOException e) {
                System.err.println("This file has no header.");
            }
        }
        return searchedRows;
    }

    /**
     * A method to print out the resulting rows so that the user can easily understand the results of
     * their search.
     *
     * @param searchedRows an ArrayList of the rows to be printed
     */
    public void printRows(ArrayList<String[]> searchedRows) {
        if (searchedRows.isEmpty()) {
            System.out.println("No results found.");
        }
        for (String[] row : searchedRows) {
            String stringRow = Arrays.toString(row);
            System.out.println(stringRow);
        }
    }
}
