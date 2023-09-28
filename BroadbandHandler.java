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

import okio.Buffer;

import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.cache.Cache;

public class BroadbandHandler implements Route {
    private Cache<String, String[]> cache;
    private CensusDatasource datasource;
    private Boolean caching;
    public BroadbandHandler(Cache<String, String[]> cache, CensusDatasource datasource) {
        this.datasource = datasource;

        this.caching = Boolean.TRUE;
        this.cache = cache;

    }
    public BroadbandHandler(CensusDatasource datasource) {
        this.datasource = datasource;
        this.caching = Boolean.FALSE;

    }

    public Object handle(Request request, Response response) {
        Moshi moshi = new Moshi.Builder().build();
        Type listObject = Types.newParameterizedType(List.class, List.class, String.class);
        Type mapObject = Types.newParameterizedType(Map.class, String.class, Object.class);
        JsonAdapter<List<List<String>>> listAdapter = moshi.adapter(listObject);
        JsonAdapter<Map<String, Object>> mapAdapter = moshi.adapter(mapObject);
        Map<String, Object> responseMap = new HashMap<>();

        String state = request.queryParams("state");
        String county = request.queryParams("county");
        String stateCode;
        String countyCode;
        try {
            stateCode = this.datasource.getStateCode(state);
            countyCode = this.datasource.getCountyCode(county, state);
        }
        catch (DatasourceException e) {

            responseMap.put("result", "error_bad_request");
            responseMap.put("error", e.getMessage());
            return mapAdapter.toJson(responseMap);
        }

        try {
            String broadband;
            if(this.caching && this.cache.asMap().containsKey(county + " County, " + state)) {
                broadband = this.cache.asMap().get(county + " County, " + state)[0];
                responseMap.put("date and time", this.cache.asMap().get(county + " County, " + state)[1]);
            }
            else {
                URL requestURL = new URL("https", "api.census.gov",
                        "/data/2021/acs/acs1/subject/variables?get=NAME,S2802_C03_022E&for=county:" + countyCode + "&in=state:" + stateCode);
                HttpURLConnection clientConnection = connect(requestURL);
                List<List<String>> body = listAdapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
                clientConnection.disconnect();
                broadband = body.get(1).get(1);
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();

                if (this.caching) {
                    String[] data = {broadband, dtf.format(now)};
                    this.cache.put(county + " County, " + state, data);

                }
                responseMap.put("date and time", dtf.format(now));
            }
            responseMap.put("result", "success");
            responseMap.put("state", state);
            responseMap.put("county", county);
            responseMap.put("broadband", broadband);
            return mapAdapter.toJson(responseMap);
        }
        catch (DatasourceException | IOException e) {
            responseMap.put("result", "error_bad_json");
            responseMap.put("error", e.getMessage());
            return mapAdapter.toJson(responseMap);
        }

    }
    public static HttpURLConnection connect(URL requestURL) throws DatasourceException, IOException {
        URLConnection urlConnection = requestURL.openConnection();
        if(! (urlConnection instanceof HttpURLConnection clientConnection))
            throw new DatasourceException("unexpected: result of connection wasn't HTTP");
        clientConnection.connect(); // GET
        if(clientConnection.getResponseCode() != 200)
            throw new DatasourceException("unexpected: API connection not success status "+clientConnection.getResponseMessage());
        return clientConnection;
    }









}
