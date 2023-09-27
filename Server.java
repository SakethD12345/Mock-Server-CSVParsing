import spark.Spark;

import java.util.ArrayList;
import java.util.List;

import static spark.Spark.after;

public class Server {
    private static ArrayList<List<String>> loadedCSV;
    private static List<String> header;
    private static Boolean hasHeader;


    public Server() {
        loadedCSV = new ArrayList<>();
        header = new ArrayList<>();
        hasHeader = Boolean.TRUE;

        int port = 4567;

        Spark.port(port);
        after((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "*");
        });

        LoadHandler loadHandler = new LoadHandler();
        Spark.get("loadcsv", loadHandler);
        ViewHandler viewHandler = new ViewHandler();
        Spark.get("viewcsv", viewHandler);
        SearchHandler searchHandler = new SearchHandler();
        Spark.get("searchcsv", searchHandler);
        BroadbandHandler broadbandHandler = new BroadbandHandler();
        Spark.get("broadband", broadbandHandler);
        Spark.init();
        Spark.awaitInitialization();

        System.out.println("Server started at http://localhost:" + port);
    }

    public static ArrayList<List<String>> getLoadedCSV() {
        return loadedCSV;
    }

    public static void setLoadedCSV(ArrayList<List<String>> newLoadedCSV) {
        loadedCSV = newLoadedCSV;
    }

    public static List<String> getCSVHeader() {
        return header;
    }

    public static void setCSVHeader(List<String> newCSVHeader) {
        header = newCSVHeader;
    }
    public static void setHasHeader(Boolean bool) {hasHeader = bool; }
    public static Boolean getHasHeader() { return hasHeader; }


}
