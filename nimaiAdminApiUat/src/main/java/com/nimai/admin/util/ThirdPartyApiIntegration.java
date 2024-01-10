package com.nimai.admin.util;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import javax.json.Json;
import javax.json.JsonReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimai.admin.model.NimaiMCustomer;
import com.nimai.admin.model.PhaseTwoTransferFail;
import com.nimai.admin.payload.MigrationResponse;
import com.nimai.admin.repository.PhaseTwoTransferFailRepository;


@Component
public class ThirdPartyApiIntegration {

	@Autowired
	PhaseTwoTransferFailRepository phaseRepo;
	
    public String postData(Object customerdata,String Url) throws Exception {
        System.out.println("URL details 1"+Url);
        try {
            String uri = Url;
            ObjectMapper objectMapper = new ObjectMapper();
            String data = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(customerdata);
            System.out.println("===============++++++++++json String 1"+customerdata.toString());
            System.out.println("===============++++++++++json String 2"+data);
            URL url=new URL(uri);
            HttpURLConnection httpUrlConnection=(HttpURLConnection) url.openConnection();
            httpUrlConnection.setRequestMethod("POST");
            httpUrlConnection.setRequestProperty("Content-Type", "application/json");
            httpUrlConnection.setUseCaches(false);
            httpUrlConnection.setDoOutput(true);
            OutputStream os = httpUrlConnection.getOutputStream();
            os.write(data.getBytes());
            os.flush();
            int responseCode=httpUrlConnection.getResponseCode();
            System.out.println(responseCode);
            BufferedReader in=new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream()));
            String inputline;

            StringBuffer response=new StringBuffer();
            while((inputline =in.readLine())!=null) {
                response.append(inputline);
                System.out.println();
            }in.close();

            try {
                JSONObject obj=new JSONObject(response.toString());
                if(obj.length()==0) {
                    System.out.println("===============++++++++++json String 3"+data);
                    System.out.println("Inside if condition"+obj.length());
                    return AppConstants.SUCCESSMSG;
                }else {
                    System.out.println("===========************ 4"+response.toString());
                    System.out.println(customerdata);
                    System.out.println(Url);
                    System.out.println(responseCode);
                    System.out.println(response.toString());
                }
            }catch(JSONException je) {
                System.out.println("Inside if condition"+je.getMessage());
                return AppConstants.SUCCESSMSG;
            }



            return AppConstants.SUCCESSMSG;
        }catch(Exception e) {
            System.out.println("===============++++++++++json String 5"+e.getMessage());
            System.out.println("AppConstants.FAILMSG URL details"+Url);
            e.printStackTrace();
            
            return AppConstants.FAILMSG;
           
        }

    }

	

    public String postDataWithToken(Object customerdata,String Url,String token) throws Exception {
        System.out.println("URL details"+Url);
        HttpURLConnection httpUrlConnection = null;
        try {
            String uri = Url;
            System.out.println("===============++++++++++json String postDataWithToken"+uri);
            ObjectMapper objectMapper = new ObjectMapper();
            String data = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(customerdata);

            URL url=new URL(uri);
            httpUrlConnection=(HttpURLConnection) url.openConnection();
            httpUrlConnection.setRequestMethod("POST");
            httpUrlConnection.setRequestProperty("Content-Type", "application/json");
            httpUrlConnection.setRequestProperty("Authorization", "Bearer"+" "+token);
            httpUrlConnection.setUseCaches(false);
            httpUrlConnection.setDoOutput(true);

            OutputStream os = httpUrlConnection.getOutputStream();
            os.write(data.getBytes());
            os.flush();
            int responseCode=httpUrlConnection.getResponseCode();
            System.out.println(responseCode);
            BufferedReader in=new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream()));
            String inputline;

            StringBuffer response=new StringBuffer();
            while((inputline =in.readLine())!=null) {
                response.append(inputline);
                System.out.println();
            }in.close();

            JSONObject obj=new JSONObject(response.toString());
            if(obj.length()==0) {
                System.out.println("Inside if condition"+obj.length());
                return AppConstants.SUCCESSMSG;
            }else {
                System.out.println("===========************"+response.toString());
                System.out.println(customerdata);
                System.out.println(Url);
                System.out.println(responseCode);
                System.out.println(response.toString());
            }


            return AppConstants.SUCCESSMSG;
        }catch(Exception e) {
            System.out.println("AppConstants.FAILMSG URL details"+Url);
            BufferedReader in=new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream()));
            String inputline;
            e.printStackTrace();
            StringBuffer response=new StringBuffer();
            while((inputline =in.readLine())!=null) {
                response.append(inputline);
                System.out.println();
            }in.close();
            int responseCode=httpUrlConnection.getResponseCode();
            JSONObject obj=new JSONObject(response.toString());
            System.out.println("===========************"+response.toString());
            System.out.println(customerdata);
            System.out.println(Url);
            System.out.println(responseCode);
            System.out.println(response.toString());

            return AppConstants.FAILMSG;
        }

    }


    public String updatepostData(Object customerdata,String Url) throws Exception {
        System.out.println("URL details"+Url);
        try {
            String uri = Url;
            ObjectMapper objectMapper = new ObjectMapper();
            String data = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(customerdata);

            System.out.println("===============++++++++++json String"+data);

            URL url=new URL(uri);
            HttpURLConnection httpUrlConnection=(HttpURLConnection) url.openConnection();
            httpUrlConnection.setRequestMethod("PUT");
            httpUrlConnection.setRequestProperty("Content-Type", "application/json");
            httpUrlConnection.setUseCaches(false);
            httpUrlConnection.setDoOutput(true);
            OutputStream os = httpUrlConnection.getOutputStream();
            os.write(data.getBytes());
            os.flush();
            int responseCode=httpUrlConnection.getResponseCode();
            System.out.println(responseCode);
            BufferedReader in=new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream()));
            String inputline;

            StringBuffer response=new StringBuffer();
            while((inputline =in.readLine())!=null) {
                response.append(inputline);
                System.out.println();
            }in.close();

            JSONObject obj=new JSONObject(response.toString());
            System.out.println("===========************"+response.toString());
            System.out.println(customerdata);
            System.out.println(Url);
            System.out.println(responseCode);
            System.out.println(response.toString());
            return AppConstants.SUCCESSMSG;
        }catch(Exception e) {
            System.out.println("AppConstants.FAILMSG URL details"+Url);
            e.printStackTrace();
            return AppConstants.FAILMSG;
        }

    }

    public MigrationResponse  sendHttpGetRequest(Object customerdata,String Url) throws IOException, JSONException{
        //String AppId="6d22174cd4d740a1a602c55c4a78d44b";
        //String url=https://openexchangerates.org/api/latest.json?app_id=
        String GET_URL=Url;
        System.out.println("===========************ Inside ok"+Url);
        MigrationResponse res=new MigrationResponse();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String data = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(customerdata);
            System.out.println("===========************ Inside ok"+customerdata);
            URL url=new URL(GET_URL);
            HttpURLConnection httpUrlConnection=(HttpURLConnection) url.openConnection();
            httpUrlConnection.setRequestMethod("POST");
            httpUrlConnection.setRequestProperty("Content-Type", "application/json");
            httpUrlConnection.setUseCaches(false);
            httpUrlConnection.setDoOutput(true);
            OutputStream os = httpUrlConnection.getOutputStream();
            os.write(data.getBytes());
            os.flush();
            int responseCode=httpUrlConnection.getResponseCode();

            System.out.println(responseCode);
            Double exchangerates=null;
            if(responseCode==HttpURLConnection.HTTP_OK) {
                System.out.println("===========************ Inside ok"+responseCode);
                BufferedReader in=new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream()));
                String inputline;

                StringBuffer response=new StringBuffer();
                while((inputline =in.readLine())!=null) {
                    response.append(inputline);
                    System.out.println();
                }in.close();

                JSONObject obj=new JSONObject(response.toString());
                System.out.println("===========************"+response.toString());

                String dataList=obj.getString("data");
                String token="";
                JSONArray array = new JSONArray(dataList);
                for(int i=0; i < array.length(); i++)
                {
                    JSONObject object = array.getJSONObject(i);
                    System.out.println(object.getString("access_token"));
                    res.setAccessToken(object.getString("access_token"));
                    res.setResponse(AppConstants.SUCCESSMSG);
                    token=object.getString("access_token");
                }
                return res;
            }
        }catch(Exception e) {
            res.setAccessToken(null);
            res.setResponse(AppConstants.FAILMSG);
            return res;
        }
        return res;

    }


    public MigrationResponse  sendHttpGetRequestTokenised(Object customerdata,String Url,String token) throws IOException, JSONException{
        String GET_URL=Url;
        System.out.println("===========************ Inside ok"+Url);
        MigrationResponse res=new MigrationResponse();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String data = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(customerdata);


            System.out.println("===========************ Inside ok"+data);


            URL url=new URL(GET_URL);
            HttpURLConnection httpUrlConnection=(HttpURLConnection) url.openConnection();
            httpUrlConnection.setRequestMethod("POST");
            httpUrlConnection.setRequestProperty("Content-Type", "application/json");
            httpUrlConnection.setRequestProperty("Authorization", "Bearer"+" "+token);
            httpUrlConnection.setUseCaches(false);
            httpUrlConnection.setDoOutput(true);
            OutputStream os = httpUrlConnection.getOutputStream();
            os.write(data.getBytes());
            os.flush();
            int responseCode=httpUrlConnection.getResponseCode();

            System.out.println(responseCode);
            Double exchangerates=null;

            System.out.println("===========************ Inside ok"+responseCode);
            BufferedReader in=new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream()));
            String inputline;

            StringBuffer response=new StringBuffer();
            while((inputline =in.readLine())!=null) {
                response.append(inputline);
                System.out.println();
            }in.close();

            JSONObject obj=new JSONObject(response.toString());
            System.out.println("===========************===============Response"+response.toString());

            String dataList=obj.getString("data");

            return res;

        }catch(Exception e) {
            res.setAccessToken(null);
            res.setResponse(AppConstants.FAILMSG);
            return res;
        }


    }



    public String java11Method(Object customerdata,String Url) throws Exception {
        System.out.println("URL details"+Url);
        try {
            String uri = Url;
            ObjectMapper objectMapper = new ObjectMapper();
            String data = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(customerdata);
            System.out.println("===============++++++++++json String"+data);
//
//			    HttpClient client = HttpClient.newBuilder().build();
//			    HttpRequest request = HttpRequest.newBuilder()
//			        .headers("Content-Type", "application/json")
//			        .uri(URI.create(uri))
//			        .POST(HttpRequest.BodyPublishers.ofString(data))
//
//			        .build();
//
//			    HttpResponse<?> response = client.send(request, HttpResponse.BodyHandlers.discarding());
//

            return AppConstants.SUCCESSMSG;
        }catch(Exception e) {
            System.out.println("AppConstants.FAILMSG URL details"+Url);
            e.printStackTrace();
            return AppConstants.FAILMSG;
        }

    }

}
