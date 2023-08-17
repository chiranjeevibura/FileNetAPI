import com.filenet.api.core.Domain;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.security.User;
import com.filenet.api.util.UserContext;

// Initialize FileNet connection
Domain domain = Factory.Domain.fetchInstance(connection, null, null, null);
ObjectStore objectStore = Factory.ObjectStore.fetchInstance(domain, "YourObjectStoreName", null);

// Retrieve a user by ID (SID)
User user = Factory.User.fetchInstance(objectStore, "your_SID_here", null);

System.out.println("User Display Name: " + user.get_DisplayName());
System.out.println("User Name: " + user.get_Name());
