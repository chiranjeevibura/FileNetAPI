import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MongoDBAggregation {

    private static final String CONNECTION_STRING = "mongodb://localhost:27017"; // Update with your MongoDB connection string
    private static final String DATABASE_NAME = "your_database_name";
    private static final String COLLECTION_NAME = "your_collection_name";

    public static void main(String[] args) {
        try (MongoClient mongoClient = MongoClients.create(CONNECTION_STRING)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

            // Example UTC timestamps
            Date startDate = Date.from(Instant.parse("2023-11-01T00:00:00Z"));
            Date endDate = Date.from(Instant.parse("2023-11-30T23:59:59Z"));

            // 1. Run aggregation for filtering and counting
            runAggregation(collection, "status", "active", startDate, endDate);

            // 2. Run aggregation for modifying and counting
            updateAndCount(collection, "status", "inactive", "active", startDate, endDate);
        }
    }

    // 1. Aggregation to filter and count documents
    public static void runAggregation(MongoCollection<Document> collection,
                                       String statusField, String value,
                                       Date startDate, Date endDate) {
        List<Bson> pipeline = Arrays.asList(
            Aggregates.match(Filters.and(
                Filters.eq(statusField, value),
                Filters.gte("timestamp", startDate),
                Filters.lte("timestamp", endDate)
            )),
            Aggregates.group(null,
                Accumulators.sum("count", 1),
                Accumulators.first("status", "$" + statusField),
                Accumulators.first("startDate", startDate),
                Accumulators.first("endDate", endDate)
            )
        );

        AggregateIterable<Document> result = collection.aggregate(pipeline);
        for (Document doc : result) {
            System.out.println(doc.toJson());
        }
    }

    // 2. Aggregation to update and count documents
    public static void updateAndCount(MongoCollection<Document> collection,
                                       String statusField, String oldValue, String newValue,
                                       Date startDate, Date endDate) {
        // Update operation
        UpdateResult updateResult = collection.updateMany(
            Filters.and(
                Filters.eq(statusField, oldValue),
                Filters.gte("timestamp", startDate),
                Filters.lte("timestamp", endDate)
            ),
            Updates.set(statusField, newValue)
        );
        System.out.println("Updated " + updateResult.getModifiedCount() + " documents.");

        // Count operation after update
        List<Bson> pipeline = Arrays.asList(
            Aggregates.match(Filters.and(
                Filters.eq(statusField, newValue),
                Filters.gte("timestamp", startDate),
                Filters.lte("timestamp", endDate)
            )),
            Aggregates.group(null,
                Accumulators.sum("count", 1),
                Accumulators.first("status", "$" + statusField),
                Accumulators.first("startDate", startDate),
                Accumulators.first("endDate", endDate)
            )
        );

        AggregateIterable<Document> result = collection.aggregate(pipeline);
        for (Document doc : result) {
            System.out.println(doc.toJson());
        }
    }
}
