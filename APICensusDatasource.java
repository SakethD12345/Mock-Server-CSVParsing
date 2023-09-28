import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import okio.Buffer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class APICensusDatasource implements CensusDatasource {
    private static HashMap<String, String> stateCodes;
    private Cache<String, HashMap<String, String>> countyCache;
    private Boolean stateCodesGenerated;

    public APICensusDatasource() {
        this.countyCache = CacheBuilder.newBuilder()
                .maximumSize(10)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
        this.stateCodesGenerated = Boolean.FALSE;
    }

    public String getStateCode(String state) throws DatasourceException, IOException {
        if (!this.stateCodesGenerated) {
            generateStateCodes();
            this.stateCodesGenerated = Boolean.TRUE;
        }
        if (stateCodes.containsKey(state)) {
            return stateCodes.get(state);
        } else {
            throw new DatasourceException("Invalid state.");
        }

    }

    public String getCountyCode(String county, String state) throws DatasourceException, IOException {
        String countyCode;

        if (countyCache.asMap().containsKey(state)) {
            if (countyCache.asMap().get(state).containsKey(county + " County, " + state)) {
                countyCode = countyCache.getIfPresent(state).get(county + " County, " + state);
            } else {
                throw new DatasourceException("Invalid county.");
            }
        } else {
            HashMap<String, String> countyMap = this.generateCountyCodes(state);
            countyCode = countyMap.get(county + " County, " + state);
        }
        return countyCode;
    }

    private static void generateStateCodes() throws DatasourceException, IOException {
        URL requestURL = new URL("https", "api.census.gov", "/data/2010/dec/sf1?get=NAME&for=state:*");
        HttpURLConnection clientConnection = connect(requestURL);
        Moshi moshi = new Moshi.Builder().build();
        Type listObject = Types.newParameterizedType(List.class, List.class, String.class);
        JsonAdapter<List<List<String>>> adapter = moshi.adapter(listObject);
        List<List<String>> body = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
        clientConnection.disconnect();
        stateCodes = listToMap(body);
    }

    private static HashMap<String, String> listToMap(List<List<String>> originalList) {
        HashMap<String, String> finalMap = new HashMap<>();
        for (List<String> row : originalList) {
            finalMap.put(row.get(0), row.get(row.size() - 1));
        }
        finalMap.remove("NAME");
        return finalMap;
    }


    public static HttpURLConnection connect(URL requestURL) throws DatasourceException, IOException {
        URLConnection urlConnection = requestURL.openConnection();
        if (!(urlConnection instanceof HttpURLConnection clientConnection))
            throw new DatasourceException("unexpected: result of connection wasn't HTTP");
        clientConnection.connect(); // GET
        if (clientConnection.getResponseCode() != 200)
            throw new DatasourceException("unexpected: API connection not success status " + clientConnection.getResponseMessage());
        return clientConnection;
    }

    private HashMap<String, String> generateCountyCodes(String state) throws DatasourceException, IOException {
        Moshi moshi = new Moshi.Builder().build();
        Type listObject = Types.newParameterizedType(List.class, List.class, String.class);
        JsonAdapter<List<List<String>>> adapter = moshi.adapter(listObject);
        HashMap<String, String> countyMap;
        URL requestURL = new URL("https", "api.census.gov", "/data/2010/dec/sf1?get=NAME&for=county:*&in=state:" + this.getStateCode(state));
        HttpURLConnection clientConnection = connect(requestURL);
        List<List<String>> body = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
        clientConnection.disconnect();
        countyMap = listToMap(body);

        this.countyCache.put(state, countyMap);
        return countyMap;
    }

    public String[] getBroadband(String county, String state) throws DatasourceException, IOException {
        String stateCode = this.getStateCode(state);
        String countyCode = this.getCountyCode(county, state);
        return this.getCountyAPIResponse(countyCode, stateCode);

    }

    public String[] getBroadband(Cache<String, String[]> cache, String county, String state) throws DatasourceException, IOException {
        String[] data;
        String stateCode = this.getStateCode(state);
        String countyCode = this.getCountyCode(county, state);

        if (cache.asMap().containsKey(county + " County, " + state)) {
            String broadband = cache.asMap().get(county + " County, " + state)[0];
            String dateAndTime = cache.asMap().get(county + " County, " + state)[1];
            data = new String[]{broadband, dateAndTime};
        } else {
            data = this.getCountyAPIResponse(countyCode, stateCode);
            cache.put(county + " County, " + state, data);
        }
        return data;
    }

    public String[] getCountyAPIResponse(String countyCode, String stateCode) throws IOException, DatasourceException {
        Moshi moshi = new Moshi.Builder().build();
        Type listObject = Types.newParameterizedType(List.class, List.class, String.class);
        JsonAdapter<List<List<String>>> listAdapter = moshi.adapter(listObject);

        URL requestURL = new URL("https", "api.census.gov",
                "/data/2021/acs/acs1/subject/variables?get=NAME,S2802_C03_022E&for=county:" + countyCode + "&in=state:" + stateCode);
        HttpURLConnection clientConnection = connect(requestURL);
        List<List<String>> body = listAdapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
        clientConnection.disconnect();
        String broadband = body.get(1).get(1);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String dateAndTime = dtf.format(now);
        return new String[] {broadband, dateAndTime};
    }
}



