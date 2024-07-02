package org.smvisualiser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class NewsClient {

  private final String API_KEY;

  public NewsClient() throws IOException {
    this.API_KEY = retrieveKey();
  }


  public String newsRetriever(String symbol) {

    try {
      URL url = new URL("https://newsapi.org/v2/everything?q=" + symbol + "&apiKey=" + API_KEY);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");

      BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      String line;
      StringBuilder response = new StringBuilder();

      while ((line = reader.readLine()) != null) {
        response.append(line);
      }
      reader.close();

      // Parse JSON response
      String jsonResponse = response.toString();
      System.out.println("Response:\n" + jsonResponse);

      // Handle JSON parsing according to your API response structure
      // Example: parse and display news articles
      // JSONObject jsonObject = new JSONObject(jsonResponse);
      // JSONArray articles = jsonObject.getJSONArray("articles");
      // for (int i = 0; i < articles.length(); i++) {
      //     JSONObject article = articles.getJSONObject(i);
      //     System.out.println(article.getString("title"));
      //     System.out.println(article.getString("url"));
      //     System.out.println("---------");
      // }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return "poop";
  }

  private static String retrieveKey() throws IOException {
    return Files.readString(Paths.get("news_api_key")).trim();
  }
}
