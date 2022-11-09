package net.foulest.oust.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MiscUtils {

    /**
     * Outputs a website's HTML (or JSON/raw text) source code.
     *
     * @param website The website to grab source code from.
     */
    public static String getWebsiteSrc(String website) {
        try {
            URL url = new URL(website);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36");
            con.setRequestProperty("content-type", "application/json; utf-8");
            con.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
            con.setRequestProperty("accept-language", "en-US,en;q=0.9");
            con.setRequestProperty("upgrade-insecure-requests", "1");
            con.setRequestProperty("dnt", "1");
            con.setRequestProperty("cache-control", "max-age=0");
            con.setRequestProperty("sec-ch-ua", "\"Chromium\";v=\"106\", \"Google Chrome\";v=\"106\", \"Not;A=Brand\";v=\"99\"");
            con.setRequestProperty("sec-ch-ua-mobile", "?0");
            con.setRequestProperty("sec-ch-ua-platform", "\"Windows\"");
            con.setRequestProperty("sec-gpc", "1");
            con.setConnectTimeout(2000);
            con.setReadTimeout(2000);

            // Tests the connection before grabbing the source code.
            // Return if our connection is blocked by the website, or any other errors occur.
            try {
                con.getInputStream();
            } catch (IOException ignored) {
                return "Blocked";
            }

            // Grabs the website's source code and returns it in String form.
            InputStream input = con.getInputStream();
            String websiteSrc = getStringFromStream(input);
            con.disconnect();
            return websiteSrc;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return "Not found";
    }

    /**
     * Converts an InputStream into a String.
     *
     * @param is The InputStream to convert.
     */
    public static String getStringFromStream(InputStream is) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;

        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException ex) {
            ex.printStackTrace();

        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return sb.toString();
    }
}
