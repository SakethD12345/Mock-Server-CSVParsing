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

    public SearchHandler() {}

    public Object handle(Request request, Response response) {
        String target = request.queryParams("search");
        String colIdentifier = request.queryParams("identifier");

        Moshi moshi = new Moshi.Builder().build();
        Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
        Map<String, Object> responseMap = new HashMap<>();

//        Boolean hasHeader = Boolean.FALSE;
//        if (header.equalsIgnoreCase("true")) {
//            hasHeader = Boolean.TRUE;
//        }
//        else if (!header.equalsIgnoreCase("false")) {
//            responseMap.put("result", "error_bad_request");
//            return adapter.toJson(responseMap);
//        }

        if (target != null) {
            CSVSearch searcher;
            if (Server.getHasHeader()) {
                searcher = new CSVSearch(target, Server.getLoadedCSV(), Boolean.TRUE, Server.getHeader());
            }
            else {
                searcher = new CSVSearch(target, Server.getLoadedCSV(), Boolean.FALSE);
            }
            ArrayList<List<String>> searchedRows;
            if (colIdentifier != null) {
                responseMap.put("column identifier", colIdentifier);
                try {
                    searchedRows = searcher.search(colIdentifier);
                }
                catch (SearchException e) {
                    if (Server.getHasHeader()) {
                        responseMap.put("available columns", Server.getHeader());
                    }
                    responseMap.put("result", "error_bad_request");
                    responseMap.put("error", e.getMessage());
                    return adapter.toJson(responseMap);
                }
            } else {
                try {
                searchedRows = searcher.search();
                }
                catch (SearchException e) {
                    responseMap.put("result", "error_bad_request");
                    responseMap.put("error", e.getMessage());
                    return adapter.toJson(responseMap);
                }
            }
            responseMap.put("result", "success");
            responseMap.put("target", target);
            responseMap.put("data", searchedRows);
            return adapter.toJson(responseMap);
        }
        else {
            responseMap.put("result", "error_bad_request");
            return adapter.toJson(responseMap);
        }
    }

}
