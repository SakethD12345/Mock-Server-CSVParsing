import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;
import java.lang.reflect.Type;

import java.util.HashMap;
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
        Type mapObject = Types.newParameterizedType(Map.class, String.class, Object.class);
        JsonAdapter<Map<String, Object>> mapAdapter = moshi.adapter(mapObject);
        Map<String, Object> responseMap = new HashMap<>();

        String state = request.queryParams("state");
        String county = request.queryParams("county");

        try {
            String[] data;
            if (this.caching) {
                data = this.datasource.getBroadband(this.cache, county, state);
            }
            else {
                data = this.datasource.getBroadband(county, state);
            }
            responseMap.put("result", "success");
            responseMap.put("state", state);
            responseMap.put("county", county);
            responseMap.put("broadband", data[0]);
            responseMap.put("date and time", data[1]);
            return mapAdapter.toJson(responseMap);
        }
        catch (DatasourceException e) {
            responseMap.put("result", "error_bad_request");
            responseMap.put("error", e.getMessage());
            return mapAdapter.toJson(responseMap);
        }
        catch (IOException e) {
            responseMap.put("result", "error_bad_json");
            responseMap.put("error", e.getMessage());
            return mapAdapter.toJson(responseMap);
        }

    }


}
