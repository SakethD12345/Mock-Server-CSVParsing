import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory;
import handler.SearchHandler;
import handler.LoadHandler;
import handler.ViewHandler;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import spark.Spark;


import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class TestCSVHandler {

    @BeforeAll
    public static void setup_before_everything() {
        Spark.port(0);
        Logger.getLogger("").setLevel(Level.WARNING);
    }

    @BeforeEach
    public void setup() {
        Spark.get("/loadcsv", new LoadHandler());
        Spark.get("/searchcsv", new SearchHandler());
        Spark.get("/viewcsv", new ViewHandler());
        Spark.init();
        Spark.awaitInitialization();
    }

    @AfterEach
    public void teardown() {
        Spark.unmap("/loadcsv");
        Spark.awaitStop();
    }

    static private HttpURLConnection tryRequest(String apiCall, String query1, String query2) throws IOException {
        URL requestURL = new URL("http://localhost:" + Spark.port());

        if (apiCall.equals("loadcsv")) {
            requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall + "?filename=" + query1 + "&header=" + query2);
        }
        else if (apiCall.equals("searchcsv")) {
            requestURL = new URL("http://localhost:"+Spark.port()+"/"+apiCall + "?target=" + query1 + "&identifier=" + query2);
        }
        System.out.println(requestURL);
        HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();
        clientConnection.connect();
        return clientConnection;
    }

    static private HttpURLConnection tryRequest(String apiCall, String target) throws IOException {
        URL requestURL = new URL("http://localhost:"+Spark.port()+"/"+apiCall + "?target=" + target);
        HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();
        clientConnection.connect();
        return clientConnection;
    }

    static private HttpURLConnection tryRequest(String apiCall) throws IOException {
        URL requestURL = new URL("http://localhost:"+Spark.port()+"/"+apiCall);
        HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();
        clientConnection.connect();
        return clientConnection;
    }

    @Test
    public void testSuccessfulLoadCSV() {
        Boolean exceptionThrown = Boolean.FALSE;
        try {
            String filename = "/Users/lanayang-maccini/Desktop/CSCI0320/server-ssdhulip-lyangmaccini/data/data1.csv";
            HttpURLConnection clientConnection = tryRequest("loadcsv", filename, "true");
            assertEquals(200, clientConnection.getResponseCode());

            Moshi moshi = new Moshi.Builder().build();
            Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
            JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);

            Map<String, Object> response = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
            clientConnection.disconnect();
            assertEquals(response.get("filepath"), filename);
        }
        catch (IOException e) {
            System.out.println("error");
            exceptionThrown = Boolean.TRUE;
        }
        assertFalse(exceptionThrown);
    }

    @Test
    public void testFailureLoadCSV() {
        Boolean exceptionThrown = Boolean.FALSE;
        try {
            String filename = "hello";
            HttpURLConnection clientConnection = tryRequest("loadcsv", filename, "true");
            assertEquals(200, clientConnection.getResponseCode());

            Moshi moshi = new Moshi.Builder().build();
            Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
            JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);

            Map<String, Object> response = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
            clientConnection.disconnect();
            assertEquals(response.get("result"), "error_datasource");
        }
        catch (IOException e) {
            exceptionThrown = Boolean.TRUE; // the actual API connection was successful, but the overall query was a failure because the file doesn't exist, which is the parser's job to throw an erorr for.
        }
        assertFalse(exceptionThrown);
    }

    @Test
    public void testSearchCSV() {
        Boolean exceptionThrown = Boolean.FALSE;
        try {
            tryRequest("loadcsv", "/Users/lanayang-maccini/Desktop/CSCI0320/server-ssdhulip-lyangmaccini/data/data1.csv", "true");
            String target = "cranston";
            HttpURLConnection clientConnection = tryRequest("searchcsv", target, "0");
            assertEquals(200, clientConnection.getResponseCode());

            Moshi moshi = new Moshi.Builder().build();
            Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
            JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);

            Map<String, Object> response = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
            clientConnection.disconnect();
            assertEquals(response.get("target"), target);
            assertEquals(response.get("data"), "Cranston");
        }
        catch (IOException e) {
            exceptionThrown = Boolean.TRUE;
        }
        assertFalse(exceptionThrown);
    }




    public void testViewCSV() {

    }




}
