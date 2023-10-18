import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class CipherSuiteChecker {

    public static void main(String[] args) {
        checkCipherSuites("example.com", 443);
    }

    public static void checkCipherSuites(String hostname, int port) {
        try {
            System.setProperty("javax.net.debug", "ssl"); // Enable SSL debugging

            SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) socketFactory.createSocket(hostname, port);

            String[] supportedCipherSuites = socket.getSupportedCipherSuites();
            System.out.println("Supported Cipher Suites:");
            for (String suite : supportedCipherSuites) {
                System.out.println(suite);
            }

            socket.setEnabledCipherSuites(supportedCipherSuites);

            // Connect to the server
            socket.startHandshake();

            String[] enabledCipherSuites = socket.getEnabledCipherSuites();
            System.out.println("\nEnabled Cipher Suites:");
            for (String suite : enabledCipherSuites) {
                System.out.println(suite);
            }

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
