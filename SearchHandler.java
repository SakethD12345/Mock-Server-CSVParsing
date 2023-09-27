import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import spark.Request;
import spark.Response;
import spark.Route;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchHandler implements Route {

    public SearchHandler() {

    }
    public Object handle(Request request, Response response) {
        String target = request.queryParams("search");
        String colIdentifier = request.queryParams("identifier");
        String header = request.queryParams("header");
        Boolean hasHeader = Boolean.FALSE;
        if (header.equalsIgnoreCase("true")) {
            hasHeader = Boolean.TRUE;
        }
        ArrayList<List<String>> parsedCSV = Server.getLoadedCSV();
        CSVSearch searcher = new CSVSearch(target, Server.getLoadedCSV(), Server.getCSVHeader(), hasHeader);
        ArrayList<List<String>> searchedRows = searcher.search(colIdentifier);

        Moshi moshi = new Moshi.Builder().build();
        Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("view", "successful");
        System.out.println("view: ");
        responseMap.put("data", searchedRows);
        return adapter.toJson(responseMap);
    }

}
