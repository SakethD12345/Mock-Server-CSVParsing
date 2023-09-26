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

    public static void main(String[] args) {
        // At time of creation, we decide on a specific datasource class:
        Server server = new Server();
        // Notice that this runs, but the program continues executing. Why
        // do you think that is? (We'll address this in a couple of weeks.)
        System.out.println("Server started; exiting main...");
    }
    public Server() {
        loadedCSV = new ArrayList<>();
        header = new ArrayList<>();

        int port = 3232;

        Spark.port(port);
        after((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "*");
        });

        // Setting up the handler for the GET /order and /mock endpoints
        //Spark.get("load", new LoadHandler(this));
        LoadHandler loadHandler = new LoadHandler();
        //System.out.println(getLoadedCSV());
        Spark.get("load", loadHandler);
        ViewHandler viewHandler = new ViewHandler();
        Spark.get("view", new ViewHandler());
        Spark.init();
        Spark.awaitInitialization();



        // Notice this link alone leads to a 404... Why is that?
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
