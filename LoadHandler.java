import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadHandler implements Route {

    public LoadHandler() {}

    public Object handle(Request request, Response response) {
        try {
            String filename = request.queryParams("filename");
            CSVParser<List<String>> parser = new CSVParser<>(new FileReader(filename), Boolean.TRUE, new RowCreator());
            ArrayList<List<String>> parsedCSV = parser.parseCSVFile();
            Moshi moshi = new Moshi.Builder().build();
            Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
            JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("load", "successful");
            responseMap.put("filepath", filename);
            Server.setLoadedCSV(parsedCSV);
            Server.setCSVHeader(parser.getHeader());

            return adapter.toJson(responseMap);
        }
        catch (FileNotFoundException e) {
            System.err.println("File not found :(");
            return 0;
        }
    }
}
