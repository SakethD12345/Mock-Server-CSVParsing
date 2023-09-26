import java.util.ArrayList;
import java.util.List;

/*
This class is used to search for certain terms in the parsed files and also has the option for
specifying the column that you want to search the term in
 */
public class CSVSearch {
    public String target;
    public List<List<String>> parsedRows;
    public List<String> header;
    public Boolean hasHeader;

    /**
     * This is the constructor for the search class which instantiates all the instance variables
     * @param target is the user's target value that they are searching for
     * @param parsedRows is the parsedRows that are parsed from the original user's csv
     * @param header is the parsed header of the csv if it exists otherwise it's an empty array
     * @param hasHeader is a boolean telling whether the file has a header
     */
    public CSVSearch(String target, List<List<String>> parsedRows, List<String> header, Boolean hasHeader) {
        this.target = target;
        this.parsedRows = parsedRows;
        this.header = header;
        this.hasHeader = hasHeader;
    }

    /**
     * This is the method for searching through the parsed file for a target word given a target column
     * @param targetColumn is the column to search in
     * @return the rows that contain the target value in the target column
     */
    public ArrayList<List<String>> search(String targetColumn) {
        ArrayList<List<String>> targetRows = new ArrayList<>();
        int searchColumn = searchHelper(targetColumn);
        // Uses the searchHelper for cases where the column identifier wasn't found in the header or
        // the header is not existent
        if (searchColumn == Integer.MAX_VALUE) {
            for (List<String> row : this.parsedRows) {
                for (String val : row) {
                    if (val.equalsIgnoreCase(this.target)) {
                        targetRows.add(row);
                    }
                }
            }
        } else {
            for (List<String> row : this.parsedRows) {
                // If there is a defined target then it only checks the value in that column for each row
                if (row.get(searchColumn).equalsIgnoreCase(this.target)) {
                    targetRows.add(row);
                }
            }
        }
        // Calls the print helper to print the final rows that contain the target value
//        printTargetRows(targetRows);
        return targetRows;
    }

    /**
     * Uses search without the targetColumn parameter, so it just searches the entire parsed file
     * @return The rows that contain the target value
     */
    public ArrayList<List<String>> search() {
        return this.search("-1");
    }
    /**
     * Helper for the search method that takes in the targetColumn and returns the number that
     * correlates with that column using the headers or just the number. Also, potentially returns the
     * max integer value in java if none of the column headers match with the string or if an invalid
     * number is provided
     * @param targetColumn is the user's input for what column they want to narrow the search to
     * @return the index of the target column
     */
    public int searchHelper(String targetColumn) {
        int col = Integer.MAX_VALUE;
        try {
            // Converts the string to an int if it is possible
            col = Integer.parseInt(targetColumn);
            // Checks if the number is within the potential range of the parsed columns
            if (col < 0 || col >= this.parsedRows.get(0).size()) {
                col = Integer.MAX_VALUE;
            }
        } catch (NumberFormatException e) {
            if (this.hasHeader) {
                col = 0;
                for (String item : this.header) {
                    if (item.equalsIgnoreCase(targetColumn)) {
                        return col;
                    } else {
                        col++;
                    }
                }
                System.out.println("Couldn't narrow search because the given column name wasn't found");
            } else {
                System.out.println("Couldn't narrow search because file doesn't contain column headers");
            }
        }
        return col;
    }

//    /**
//     * This method prints out all the final Rows that have the target value in them
//     * @param targetRows is the given rows that contain the target value (in the target column if that
//     *                   is specified)
//     */
//    public void printTargetRows(ArrayList<List<String>> targetRows) {
//        for (List<String> row : targetRows) {
//
////            for (String val : row) {
////                if (row[row.() - 1].equals(val)) {
////                    System.out.print(val);
////                } else {
////                    System.out.print(val + ", ");
////                }
////            }
//            System.out.println();
//        }
//    }
}
