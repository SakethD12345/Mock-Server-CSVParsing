import spark.Request;
import spark.Response;
import spark.Route;

import java.util.ArrayList;
import java.util.List;

public class SearchHandler implements Route {
    public Object handle(Request request, Response response) {
        ArrayList<List<String>> parsedCSV = Server.getLoadedCSV();
        CSVSearch searcher = new CSVSearch();
    }

}
