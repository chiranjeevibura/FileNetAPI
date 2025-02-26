# MongoDB Configuration
mongo.uri=mongodb://your_mongo_connection
mongo.database=your_target_db
mongo.collection=documents

# Oracle Database Configuration
oracle.dsn=your_oracle_dsn
oracle.user=your_db_user
oracle.password=your_db_password

# SQL Query to Fetch Parent-Redacted Mapping
sql.query=SELECT p.PARENT_GUID, r.RED_GUID, r.CREATED_DATE, r.TUPLE_ID \
          FROM REDACTED_DOCS r \
          JOIN PARENT_DOCS p ON r.PARENT_GUID = p.PARENT_GUID


pip install pymongo cx_Oracle configparser


import uuid
import cx_Oracle
import configparser
from pymongo import MongoClient

### **1️⃣ Load Configurations from Properties File** ###
config = configparser.ConfigParser()
config.read("config.properties")

# MongoDB Configuration
MONGO_URI = config.get("DEFAULT", "mongo.uri")
MONGO_DB = config.get("DEFAULT", "mongo.database")
MONGO_COLLECTION = config.get("DEFAULT", "mongo.collection")

# Oracle Database Configuration
ORACLE_DSN = config.get("DEFAULT", "oracle.dsn")
ORACLE_USER = config.get("DEFAULT", "oracle.user")
ORACLE_PASSWORD = config.get("DEFAULT", "oracle.password")

# SQL Query
SQL_QUERY = config.get("DEFAULT", "sql.query")

### **2️⃣ Fetch Metadata from SQL Database** ###
# Create a Connection to Oracle
connection = cx_Oracle.connect(ORACLE_USER, ORACLE_PASSWORD, ORACLE_DSN)
cursor = connection.cursor()

cursor.execute(SQL_QUERY)

# Store Metadata in a Dictionary {parent_guid: [list of redacted docs]}
parent_redacted_map = {}

for parent_guid, red_guid, created_date, tuple_id in cursor.fetchall():
    parent_guid = parent_guid.lower().strip("{}")  # Convert to lowercase, remove `{}` for consistency
    red_guid = red_guid.lower().strip("{}")

    # Create linked document structure
    linked_doc = {
        "linkId": red_guid,
        "linkName": "Redaction-Origin",
        "linkCreatedDateTime": str(created_date),  # Convert Date to String
        "linkTupleId": tuple_id
    }

    # Append to Parent's Linked Documents List
    if parent_guid not in parent_redacted_map:
        parent_redacted_map[parent_guid] = []
    
    parent_redacted_map[parent_guid].append(linked_doc)

# Close Oracle Connection
cursor.close()
connection.close()

### **3️⃣ Connect to MongoDB & Update Parent Documents** ###
mongo_client = MongoClient(MONGO_URI)
db = mongo_client[MONGO_DB]
collection = db[MONGO_COLLECTION]

# Loop Through Parent Documents and Update LinkedDocuments if Empty
for parent_guid, linked_docs in parent_redacted_map.items():
    parent_uuid = uuid.UUID(parent_guid)  # Convert to UUID for MongoDB Query

    # Check if Parent Exists and has Empty `linkedDocuments`
    parent_doc = collection.find_one({"_id": parent_uuid})
    if parent_doc and ("linkedDocuments" not in parent_doc or not parent_doc["linkedDocuments"]):
        # Perform the Update (Using `$set` to Update Only `linkedDocuments`)
        collection.update_one(
            {"_id": parent_uuid},
            {"$set": {"linkedDocuments": linked_docs}}
        )

# Close MongoDB Connection
mongo_client.close()

print("✅ MongoDB linkedDocuments field updated successfully!")
