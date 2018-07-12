package com.example.amit.photogallery;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FlickerFetch{
     private static final String TAG="flickertag";
     // flicker api key
     private static final String  API_KEY="aca70b2725f4ad0027f65800243ed55b";

     // constants specifying the type of flickr method
     private static final String FETCH_RECENT_METHOD="flickr.photos.getRecent";
     private static final String SEARCH_METHOD="flickr.photos.search";

     // construction of initial uri for request the method will be appended at runtime
    private static final Uri ENDPOINT= Uri.parse("https://api.flickr.com/services/rest")
             .buildUpon()
             .appendQueryParameter("api_key",API_KEY)
             .appendQueryParameter("format","json")
             .appendQueryParameter("nojsoncallback","1")//
             .appendQueryParameter("extras","url_s")
             .build();

    //Method fetches the raw data from given url and returns it as an array of bytes
    public byte[] getUrlBytes(String urlSpec)throws IOException
    {
        URL url=new URL(urlSpec);

        HttpURLConnection connection=(HttpURLConnection) url.openConnection();


        try {

            ByteArrayOutputStream out=new ByteArrayOutputStream();

            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + " with " + urlSpec);
            }

            int bytesRead=0;
            byte buffer[]=new byte[1024];
            while((bytesRead=in.read(buffer))>0){
                out.write(buffer,0,bytesRead);
            }
            out.close();
            return out.toByteArray();
        }
        finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec)throws IOException
    {
        return new String(getUrlBytes(urlSpec));
    }
    //The method makes proper API request and get the url from which we get the json parse the json
    //into java model objects and returns the list of such objects
    private List<GalleryItem> downloadGalleryItems(String url)
    {   List<GalleryItem> items= new ArrayList<>();
        try{
            //Construction of proper url string to make an api request using the Uri.Builder class

            String jsonString=getUrlString(url);
            Log.i(TAG,"fetched json object:-"+jsonString);
            //A  java object is created using the fetched JSON text
            JSONObject jsonBody=new JSONObject(jsonString);
            // parsed into list of java objects
            items=parseItems(jsonBody);
            }
            catch (IOException ioe)
            {
                Log.e(TAG,"Unable to fetch the json");
            }
            catch (JSONException je)
            {
                Log.e(TAG,"Failed to parse json",je);
            }
            return items;
    }
    //This method will pull information for each photo and will create a single GalleryItem object for it and
    // construct a list of gallery items
    /* private void parseItems(List<GalleryItem> items,JSONObject jsonBody)throws IOException,JSONException{
        JSONObject photosJsonObject=jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray=photosJsonObject.getJSONArray("photo");

        for(int i=0;i<photoJsonArray.length();i++){
            JSONObject photoJsonObject=photoJsonArray.getJSONObject(i);

            GalleryItem item=new GalleryItem();
            item.setmId(photoJsonObject.getString("id"));
            item.setmCaption(photoJsonObject.getString("title"));

            if(!photoJsonObject.has("url_s")){
                continue;
            }
            item.setmUrl(photoJsonObject.getString("url_s"));
            items.add(item);

        }

     }*/
    //Challenge:-parsing json with Gson library

    private List<GalleryItem> parseItems(JSONObject jsonBody)throws IOException,JSONException{
        JSONObject photosJsonObject=jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray=photosJsonObject.getJSONArray("photo");
            Gson gson =new Gson();
            Log.i(TAG,photoJsonArray.toString());
            return Arrays.asList(gson.fromJson(photoJsonArray.toString(),GalleryItem[].class));

        }

        // The method builds the proper url based upon the method specified

    private String buildUrl(String method,String query)// query represents the search keyworlds
    {
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                                 .appendQueryParameter("method",method);
        // search query is appended if the method is search
        if(method.equals(SEARCH_METHOD))
        {
            uriBuilder.appendQueryParameter("text",query);
        }
        return uriBuilder.build().toString();
    }

    // proper methods are passed for proper action
    public List<GalleryItem> fetchRecentPhotos() {
        String url = buildUrl(FETCH_RECENT_METHOD, null);
        return downloadGalleryItems(url);
    }
    public List<GalleryItem> searchPhotos(String query) {
        String url = buildUrl(SEARCH_METHOD, query);
        return downloadGalleryItems(url);
    }

}