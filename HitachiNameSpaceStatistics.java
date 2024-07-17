import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.Properties;

public class HitachiNameSpaceStatistics {
    public static void main(String[] args) throws Exception {
        try {
            getNameSpaceStatistics();
            String guid = "your-guid-here";
            searchAndDownloadObject(guid);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void getNameSpaceStatistics() throws IOException {
        InputStream propsInstream = null;
        Properties props = null;

        try {
            propsInstream = HitachiNameSpaceStatistics.class.getClassLoader().getResourceAsStream("config.properties");
            props = new Properties();
            props.load(propsInstream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int numberOfServers = Integer.parseInt(props.getProperty("numberOfServers"));
        for (int i = 1; i <= numberOfServers; i++) {
            String url = props.getProperty("hitachiServerUrl_" + i);
            String environment = props.getProperty("environment");

            CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();

            HttpGet request = new HttpGet(url);
            request.addHeader("Authorization", props.getProperty("authenticationCode_" + i));

            try {
                HttpResponse response = httpClient.execute(request);

                if (response.getStatusLine().getStatusCode() == 200) {
                    BufferedReader rd = new BufferedReader(
                            new InputStreamReader(response.getEntity().getContent()));
                    StringBuffer result = new StringBuffer();
                    String line;

                    while ((line = rd.readLine()) != null) {
                        result.append(line);
                    }

                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = dbFactory.newDocumentBuilder();
                    byte[] arr = result.toString().getBytes();
                    InputStream in = new ByteArrayInputStream(arr);
                    Document doc = builder.parse(new InputSource(in));
                    doc.getDocumentElement().normalize();

                    System.out.println("Environment = " + environment);
                    System.out.println("Namespace= " + props.getProperty("nameSpace_" + i));
                    String objectCount = doc.getDocumentElement().getAttribute("objectCount");
                    String totalCapacity = doc.getDocumentElement().getAttribute("totalCapacityBytes");
                    String temp = totalCapacity;
                    totalCapacity = formatFileSize(Long.valueOf(totalCapacity).longValue());
                    System.out.println("Pool Capacity = " + totalCapacity);
                    String usedCapacityBytes = doc.getDocumentElement().getAttribute("usedCapacityBytes");
                    long usedCapacityBytesL = Long.valueOf(temp).longValue() - Long.valueOf(usedCapacityBytes).longValue();
                    usedCapacityBytes = formatFileSize(usedCapacityBytesL);
                    System.out.println("Free Space = " + usedCapacityBytes);
                    System.out.println("ObjectCount = " + objectCount);
                } else {
                    System.out.println(props.getProperty("nameSpace_" + i) + " Response Code : " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String formatFileSize(long size) {
        String hrsize = null;
        double b = size;
        hrsize = String.format("%.2f", b) + " Bytes";
        return hrsize;
    }

    public static void searchAndDownloadObject(String guid) {
        InputStream propsInstream = null;
        Properties props = null;

        try {
            propsInstream = HitachiNameSpaceStatistics.class.getClassLoader().getResourceAsStream("config.properties");
            props = new Properties();
            props.load(propsInstream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int numberOfServers = Integer.parseInt(props.getProperty("numberOfServers"));
        for (int i = 1; i <= numberOfServers; i++) {
            String url = props.getProperty("hitachiServerUrl_" + i) + "/rest/objects/" + guid;
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();

            HttpGet request = new HttpGet(url);
            request.addHeader("Authorization", props.getProperty("authenticationCode_" + i));

            try {
                HttpResponse response = httpClient.execute(request);

                if (response.getStatusLine().getStatusCode() == 200) {
                    InputStream inputStream = response.getEntity().getContent();
                    File targetFile = new File("downloaded_object_" + guid + ".dat");
                    OutputStream outputStream = new FileOutputStream(targetFile);

                    byte[] buffer = new byte[8 * 1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    outputStream.close();
                    inputStream.close();

                    System.out.println("Object " + guid + " downloaded successfully.");
                } else {
                    System.out.println("Object " + guid + " not found. Response Code : " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
