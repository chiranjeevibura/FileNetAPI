import com.filenet.api.core.*;
import com.filenet.api.util.UserContext;
import com.filenet.api.exception.EngineRuntimeException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.security.auth.Subject;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class connectDomainSSLwithCert {
	public static void main(String[] args) {

		String username = " ";
		String password = " ";
		String url = " ";
		String OSName = " ";
		String certificatePath = "C:\\Work\\fnc.crt";

		// Create a connection to the FileNet domain
		try {
			// Load the SSL certificate
			CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
			FileInputStream fileInputStream = new FileInputStream(certificatePath);
			X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(fileInputStream);
			fileInputStream.close();

			// Create a KeyStore and add the certificate
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(null, null);
			keyStore.setCertificateEntry("fileNetCertificate", certificate);

			// Create a TrustManager that trusts the certificate
			TrustManagerFactory trustManagerFactory = TrustManagerFactory
					.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init(keyStore);

			// Create an SSL context with the TrustManager
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

			Connection connection = Factory.Connection.getConnection(url);
			try {
				// Establish the user context
				Subject subject = UserContext.createSubject(connection, username, password, null);
				UserContext.get().pushSubject(subject);

				// Get the default domain
				Domain domain = Factory.Domain.fetchInstance(connection, null, null);

				// Print the domain connection status
				System.out.println("Domain connected: true");
				System.out.println("Domain name: " + domain.get_Name());

				// Get the object store
				ObjectStore objectStore = Factory.ObjectStore.fetchInstance(domain, OSName, null);
				System.out.println("Source Object Store: " + objectStore.get_DisplayName());

				UserContext.get().popSubject();
			} catch (EngineRuntimeException e) {
				System.out.println("Domain connected: false");
				e.printStackTrace();
			}
		} catch (Exception e) {
			System.out.println("Domain connected: false");
			e.printStackTrace();
		}
	}

}
