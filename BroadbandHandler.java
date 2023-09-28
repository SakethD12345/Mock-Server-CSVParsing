import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import okio.Buffer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import javax.xml.crypto.Data;

public class BroadbandHandler implements Route {
    private static HashMap<String, String> stateToCode;
    private Cache<String, HashMap<String, String>> countyCache;
    public BroadbandHandler(Cache cache) {
        this.setUp();
    }
    public BroadbandHandler() {
        this.setUp();

    }
    public void setUp() {
        this.countyCache = CacheBuilder.newBuilder()
                .maximumSize(10)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();

        try {
            generateStateCodes();

        }
        catch (DatasourceException e) {}
    }

    public Object handle(Request request, Response response) {
        String state = request.queryParams("state");
        String county = request.queryParams("county");
        String stateCode = stateToCode.get(state);
        String countyCode;

        Moshi moshi = new Moshi.Builder().build();
        Type listObject = Types.newParameterizedType(List.class, List.class, String.class);
        Type mapObject = Types.newParameterizedType(Map.class, String.class, Object.class);
        JsonAdapter<List<List<String>>> listAdapter = moshi.adapter(listObject);
        JsonAdapter<Map<String, Object>> mapAdapter = moshi.adapter(mapObject);
        Map<String, Object> responseMap = new HashMap<>();


        if (countyCache.asMap().containsKey(state)) {
            if (countyCache.asMap().get(state).containsKey(county + " County, " + state)) {
                countyCode = countyCache.getIfPresent(state).get(county + " County, " + state);
            }
            else {
                responseMap.put("result", "error_bad_request");
                return mapAdapter.toJson(responseMap);
            }
        }
        else {
            HashMap<String, String> countyMap = this.generateCountyCodes(state);
            countyCode = countyMap.get(county + " County, " + state);
            countyCache.put(state, countyMap);
        }


        if (state == null || county == null || !stateToCode.containsKey(state)) {
            responseMap.put("result", "error_bad_request");
            return mapAdapter.toJson(responseMap);
        }
        try {
            URL requestURL = new URL("https", "api.census.gov",
                    "/data/2021/acs/acs1/subject/variables?get=NAME,S2802_C03_022E&for=county:" + countyCode + "&in=state:" + stateCode);
            HttpURLConnection clientConnection = connect(requestURL);
            List<List<String>> body = listAdapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
            clientConnection.disconnect();
            String broadband = body.get(1).get(1);
            responseMap.put("result", "success");
            responseMap.put("state", state);
            responseMap.put("county", county);
            responseMap.put("broadband", broadband);
            return mapAdapter.toJson(responseMap);
        }
        catch (DatasourceException | IOException e) {
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

    private HashMap<String, String> generateCountyCodes(String state) {
        Moshi moshi = new Moshi.Builder().build();
        Type listObject = Types.newParameterizedType(List.class, List.class, String.class);
        JsonAdapter<List<List<String>>> adapter = moshi.adapter(listObject);
        HashMap<String, String> countyMap = this.getCountyCodes(adapter, state);
        this.countyCache.put(state, countyMap);
        return countyMap;
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
        if(! (urlConnection instanceof HttpURLConnection clientConnection))
            throw new DatasourceException("unexpected: result of connection wasn't HTTP");
        clientConnection.connect(); // GET
        if(clientConnection.getResponseCode() != 200)
            throw new DatasourceException("unexpected: API connection not success status "+clientConnection.getResponseMessage());
        return clientConnection;
    }

    private HashMap<String, String> getCountyCodes(JsonAdapter<List<List<String>>> adapter, String state) {
        HashMap<String, String> map = new HashMap<>();
        try {
            URL requestURL = new URL("https", "api.census.gov", "/data/2010/dec/sf1?get=NAME&for=county:*&in=state:" + stateToCode.get(state));
            HttpURLConnection clientConnection = connect(requestURL);
            List<List<String>> body = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
            clientConnection.disconnect();
            map = listToMap(body);
            return map;
        }
        catch (IOException | DatasourceException e) {
            return map;
        }
    }

}
