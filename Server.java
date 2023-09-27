import spark.Spark;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.after;

public class Server {
    private static ArrayList<List<String>> loadedCSV;
    private static List<String> header;


    public Server() {
        loadedCSV = new ArrayList<>();
        header = new ArrayList<>();

        int port = 4567;

        Spark.port(port);
        after((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "*");
        });

        LoadHandler loadHandler = new LoadHandler();
        Spark.get("load", loadHandler);
        ViewHandler viewHandler = new ViewHandler();
        Spark.get("view", viewHandler);
        SearchHandler searchHandler = new SearchHandler();
        Spark.get("search", searchHandler);
        CensusHandler censusHandler = new CensusHandler();
        Spark.get("broadband", censusHandler);
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


}
