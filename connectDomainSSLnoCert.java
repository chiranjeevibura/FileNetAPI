import com.filenet.api.core.*;
import com.filenet.api.util.UserContext;
import javax.security.auth.Subject;

public class connectDomainSSLnoCert {

	public static void main(String[] args) {

		String username = " ";
		String password = " ";
		String url = " ";
		String objectStoreName = " ";

		try {
			// Connect to the source domain
			Connection sourceConnection = Factory.Connection.getConnection(url);
			Subject subject = UserContext.createSubject(sourceConnection, username, password, null);
			UserContext.get().pushSubject(subject);

			// Get the source domain
			Domain sourceDomain = Factory.Domain.fetchInstance(sourceConnection, null, null);

			// Print the source domain connection status
			System.out.println("Source Domain connected: True");
			System.out.println("Source Domain name: " + sourceDomain.get_Name());

			// Get the source object store
			ObjectStore objectStore = Factory.ObjectStore.fetchInstance(sourceDomain, objectStoreName, null);
			System.out.println("Source Object Store: " + objectStore.get_DisplayName());

		} finally {
			UserContext.get().popSubject();

		}
	}
}
