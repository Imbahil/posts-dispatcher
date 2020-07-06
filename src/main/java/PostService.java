import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PostService {
    private static FileWriter file;

    public static void downloadPosts() throws IOException {
        HttpURLConnection connection = makeConnection();
        validateResponseStatus(connection.getResponseCode());
        StringBuilder response = createJson(connection);
        List<Post> postsList = convertJsonToPostsList(response);

        postsList.forEach(post -> {
            try {
                saveFile(post);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }

    private static HttpURLConnection makeConnection() throws IOException {
        URL urlForGetRequest = new URL("https://jsonplaceholder.typicode.com/posts");
        HttpURLConnection connection = (HttpURLConnection) urlForGetRequest.openConnection();
        connection.setRequestMethod("GET");
        return connection;
    }

    private static StringBuilder createJson(HttpURLConnection connection) throws IOException {
        String readLine;
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();

        while (null != (readLine = in.readLine())) {
            response.append(readLine);
        }
        in.close();

        return response;
    }

    private static void validateResponseStatus(final int responseCode) {
        if (responseCode != HttpURLConnection.HTTP_OK) {
            System.out.println("NOT OK :(");
        }
    }

    private static List<Post> convertJsonToPostsList(final StringBuilder response) {
        Gson gson = new Gson();
        Type postsListType = new TypeToken<ArrayList<Post>>() {
        }.getType();
        return gson.fromJson(response.toString(), postsListType);
    }

    private static void saveFile(final Post post) throws JsonProcessingException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(post);
        try {

            file = new FileWriter("posts/" + post.getId() + ".json");
            file.write(json);

        } catch (IOException e) {
            e.printStackTrace();

        } finally {

            try {
                file.flush();
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
