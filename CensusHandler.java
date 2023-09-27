import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import okio.Buffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CensusHandler implements Route {
    private static HashMap<String, String> stateToCode;
    private static HashMap<String, HashMap<String, String>> countyToCode;
    public CensusHandler() {
        try {
            generateStateCodes();
            generateCountyCodes();
        }
        catch (DatasourceException e) {
        }

    }
    public Object handle(Request request, Response response) {
        String state = request.queryParams("state");
        String county = request.queryParams("county");
        String stateCode = stateToCode.get(state);
        String countyCode = countyToCode.get(state).get(county);
        try {
            Moshi moshi = new Moshi.Builder().build();
            Type listObject = Types.newParameterizedType(List.class, List.class, String.class);
            URL requestURL = new URL("https", "api.census.gov",
                    "/data/2010/dec/sf1?get=NAME&for=county:" + countyCode + "&in=state:" + stateCode);
            HttpURLConnection clientConnection = connect(requestURL);
            Type mapObject = Types.newParameterizedType(Map.class, String.class, Object.class);
            JsonAdapter<List<List<String>>> listAdapter = moshi.adapter(listObject);
            JsonAdapter<Map<String, Object>> mapAdapter = moshi.adapter(mapObject);
            List<List<String>> body = listAdapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
            clientConnection.disconnect();
            Map<String, Object> responseMap = new HashMap<>();
            String broadband = body.get(1).get(1);
            responseMap.put("broadband", broadband);
            return mapAdapter.toJson(responseMap);
        }
        catch (IOException | DatasourceException e) {
            return 0;
        }

    }

    private static void generateStateCodes() throws DatasourceException {
        try {
            URL requestURL = new URL("https", "api.census.gov", "/data/2010/dec/sf1?get=NAME&for=state:*");
            HttpURLConnection clientConnection = connect(requestURL);
            Moshi moshi = new Moshi.Builder().build();
            Type listObject = Types.newParameterizedType(List.class, List.class, String.class);
            JsonAdapter<List<List<String>>> adapter = moshi.adapter(listObject);
            List<List<String>> body = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
            clientConnection.disconnect();
            stateToCode = listToMap(body);
        }
        catch (IOException | DatasourceException e) {
            throw new DatasourceException(e.getMessage());
        }
    }

    private static void generateCountyCodes() throws DatasourceException {
        try {
            Moshi moshi = new Moshi.Builder().build();
            Type listObject = Types.newParameterizedType(List.class, List.class, String.class);
            JsonAdapter<List<List<String>>> adapter = moshi.adapter(listObject);
            countyToCode = new HashMap<>();
            for (String state: stateToCode.keySet()) {
                URL requestURL = new URL("https", "api.census.gov", "/data/2010/dec/sf1?get=NAME&for=county:*&in=state:" + stateToCode.get(state));
                HttpURLConnection clientConnection = connect(requestURL);
                List<List<String>> body = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
                HashMap<String, String> countyToCodeForState = listToMap(body);
                countyToCode.put(state, countyToCodeForState);
                clientConnection.disconnect();
            }
        }
        catch (IOException | DatasourceException e) {
            throw new DatasourceException(e.getMessage());
        }

    }

    private static HashMap<String, String> listToMap(List<List<String>> originalList) {
        HashMap<String, String> finalMap = new HashMap<>();
        for(List<String> row: originalList) {
            finalMap.put(row.get(0), row.get(row.size()-1));
        }
        finalMap.remove("NAME");
        return finalMap;
    }


    private static HttpURLConnection connect(URL requestURL) throws DatasourceException, IOException {
        URLConnection urlConnection = requestURL.openConnection();
        if(! (urlConnection instanceof HttpURLConnection))
            throw new DatasourceException("unexpected: result of connection wasn't HTTP");
        HttpURLConnection clientConnection = (HttpURLConnection) urlConnection;
        clientConnection.connect(); // GET
        if(clientConnection.getResponseCode() != 200)
            throw new DatasourceException("unexpected: API connection not success status "+clientConnection.getResponseMessage());
        return clientConnection;
    }

}
