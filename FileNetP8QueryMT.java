import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.UserContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

import javax.security.auth.Subject;

public class FileNetP8QueryMT {
	public static void main(String[] args) {
		String connURL = " ";
		String connUName = " ";
		String connPwd = " ";
		String objectStoreName = " ";

		// Prepare the queries
		String[] queries = {
				"SELECT This, Id FROM IS_DC_  WHERE DateCreated >= 20100301T183000Z AND DateCreated <= 20230531T181500Z",
				// Add more queries here
				"SELECT This, Id FROM IS_DC_  WHERE DateCreated >= 20100301T183000Z AND DateCreated <= 20230531T181500Z",
				"SELECT This, Id FROM P8_  WHERE DateCreated >= 20100301T183000Z AND DateCreated <= 20230511T181500Z", 
				"SELECT This, Id FROM P8_  WHERE DateCreated >= 20100301T183000Z AND DateCreated <= 20230511T181500Z",
				"SELECT This, Id FROM IS_  WHERE DateCreated >= 20100301T183000Z AND DateCreated <= 20230511T181500Z"
				};

		Connection connection = Factory.Connection.getConnection(connURL);
		Subject subject = UserContext.createSubject(connection, connUName, connPwd, null);
		UserContext.get().pushSubject(subject);

		try {
			Domain domain = Factory.Domain.fetchInstance(connection, null, null);
			ObjectStore objectStore = Factory.ObjectStore.fetchInstance(domain, objectStoreName, null);

			int threadPoolSize = 5; // Number of threads to use
			ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
			List<Future<Long>> futures = new ArrayList<>();
            Boolean continuable = new Boolean(true);
            Integer pageSize = new Integer(1000);
            
			for (int i = 0; i < queries.length; i++) {
				String query = queries[i];
				final int queryIndex = i; // Create a final copy of 'i'

				Callable<Long> callable = () -> {

					UserContext.get().pushSubject(subject);
					try {

						SearchSQL searchSQL = new SearchSQL(query);
						SearchScope searchScope = new SearchScope(objectStore);
						
						// Fetch the document objects using the search
                        long startTime = System.currentTimeMillis();
                        RepositoryRowSet rowSet = searchScope.fetchRows(searchSQL, pageSize, null, continuable);
                        

						// Fetch the document objects using the search
						//RepositoryRowSet rowSet = searchScope.fetchRows(searchSQL, pageSize, null, continuable);

						long count = 0;
						Iterator<?> iterator = rowSet.iterator();
						while (iterator.hasNext()) {
							iterator.next();
							count++;
						}
						long endTime = System.currentTimeMillis();
                        System.out.println("Count of IDs for Query " + (queryIndex  + 1) + ": " + count);
                        System.out.println("Time taken for Query " + (queryIndex + 1) + ": " + (endTime - startTime)/ (1000*60) + " minutes");

						return count;
					} finally {
						// Pop user context after the thread execution
						UserContext.get().popSubject();
					}
				};

				Future<Long> future = executorService.submit(callable);
				futures.add(future);
			}

			// Shutdown the executor service and wait for all tasks to complete
			executorService.shutdown();
			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

			// Get the results from the futures and print them
			for (int i = 0; i < futures.size(); i++) {
				long count = futures.get(i).get();
				System.out.println("Result of Query " + (i + 1) + ": " + count);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			UserContext.get().popSubject();
		}
	}
}
