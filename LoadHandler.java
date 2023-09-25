public class LoadHandler implements Route {
    private Server server;
    public LoadHandler(Server server) {
        this.server = server;
    }

    public Object handle(Request request) {
        String filename = request.queryParmams("filename");
        Moshi moshi = new Moshi.Builder().build();
        Type mapStringObject = Type.newParameterizedType(Map.class, String.class, Object.class);
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject).nonNull();
        Map<String, Object> body = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

        server.setLoadedCSV(body);
    }

    public void loadcsv(String filepath, Boolean hasHeader) {
        try {
            FileReader reader = new FileReader(filepath);
            RowCreator rowCreator = new RowCreator();
            CSVParser<List<String>> parser = new CSVParser<>(reader, hasHeader, rowCreator);
            this.loadedCSV = parser.parseCSVFile();
        }
        catch (FileNotFoundException e){
            System.err.println("CSV file not found. Please input a valid file path.");
        }
    }
}
