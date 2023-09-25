public class ViewHandler implements Route {
    public Object handle(Request request) {
        String filename = request.queryParmams("filename");
        CSVParser parser = new CSVParser(new FileReader(filename), Boolean.TRUE, new RowCreator());
        ArrayList<List<String>> parsedCSV = parser.parseCSVFile();
        Moshi moshi = new Moshi.Builder().build();
        Type mapStringObject = Type.newParameterizedType(Map.class, String.class, Object.class);
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("view", "succesful");
        responseMap.put("data, ");
        try {
//            double lat_double = Double.parseDouble(lat);
//            double lon_double = Double.parseDouble(lon);
//            Geolocation loc = new Geolocation(lat_double, lon_double);
//            // Low-level NWS API invocation isn't the job of this class!
//            // Neither is caching!
//            WeatherData data = state.getCurrentWeather(loc);
//            // Building responses is the job of this class:
            responseMap.put("view", "successful");

            // Decision point; note the difference here
            responseMap.put("data", adapter.toJson(parsedCSV));

            return adapter.toJson(responseMap);
        } catch (DatasourceException e) {
            responseMap.put("view", "error");
            responseMap.put("error_type", "datasource");
            responseMap.put("details", e.getMessage());
            return adapter.toJson(responseMap);
        }
    }
}
