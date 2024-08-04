import io
import uuid
import logging
from datetime import datetime
from struct import unpack
import pytz
from pyspark.sql import SparkSession
from pyspark.sql.functions import (
    collect_list, sha2, array, col, lit, struct, udf, monotonically_increasing_id,
    array_distinct, concat, split, when
)
from pyspark.sql.types import BinaryType, StringType, StructField, StructType, IntegerType
import configparser
import time

# Configuration
config = configparser.ConfigParser()
config.read('config.ini')

#MongoDB Config
mongo_uri = config['mongo']['uri']
database = config['mongo']['database']
collection = config['mongo']['collection']

#OracleDB Config
oracle_user = config['oracle']['user']
oracle_password = config['oracle']['password']
oracle_driver = config['oracle']['driver']
oracle_url = config['oracle']['url']

#Queries
main_sql_query = config['queries']['main_sql_query']
cust_sql_query = config['queries']['cust_sql_query']
ext_sql_query = config['queries']['ext_sql_query']

# Initialize Spark session
spark = SparkSession.builder \
    .appName("Oracle to MongoDB Migration") \
    .config("spark.mongodb.read.connection.uri", mongo_uri) \
    .config("spark.mongodb.write.connection.uri", mongo_uri) \
    .config('spark.jars.packages', 'org.mongodb.spark:mongo-spark-connector_2.12:10.2.2') \
    .config('spark.mongodb.write.convertJson', "object_Or_Array_Only") \
    .getOrCreate()

# Read data from Oracle database
def read_data_from_oracle():
    oracle_main_df = spark.read.format("jdbc") \
        .option("url", oracle_url) \
        .option("dbtable", f"({sql_main_query})") \
        .option("user", oracle_user) \
        .option("password", oracle_password) \
        .option("driver", oracle_driver) \
        .load()

    oracle_ext_df = spark.read.format("jdbc") \
        .option("url", oracle_url) \
        .option("dbtable", f"({sql_ext_ref_query})") \
        .option("user", oracle_user) \
        .option("password", oracle_password) \
        .option("driver", oracle_driver) \
        .load()

    oracle_cust_df = spark.read.format("jdbc") \
        .option("url", oracle_url) \
        .option("dbtable", f"({sql_cust_ref_query})") \
        .option("user", oracle_user) \
        .option("password", oracle_password) \
        .option("driver", oracle_driver) \
        .load()


# Aggregate externalReferenceKeys in oracle_ext_df
oracle_ext_df = oracle_ext_df.groupBy("object_id").agg(
    F.collect_list(
        F.struct(
            F.col("ext_ref_domain").alias("domain"),
            F.col("externalReferenceKeys_name").alias("name"),
            F.col("externalReferenceKeys_value").alias("value")
        )
    ).alias("externalReferenceKeys")
)

# Aggregate customerReferenceKeys in oracle_cust_df
oracle_cust_df = oracle_cust_df.groupBy("object_id").agg(
    F.collect_list(
        F.struct(
            F.col("CustomerDomain").alias("domain"),
            F.col("customerReferenceKeys_name").alias("name"),
            F.col("customerReferenceKeys_value").alias("value")
        )
    ).alias("customerReferenceKeys")
)

# Join main_df with oracle_ext_df and oracle_cust_df
oracle_df = oracle_main_df.join(oracle_ext_df, on="object_id", how="left") \
                          .join(oracle_cust_df, on="object_id", how="left")

return oracle_df

# Function to decode Oracle Blob
def get_hcp_path(hop_blob_data):
    # ... (existing code)
udf_hcp_path = udf(get_hcp_path)

# Function to replace values
def replace_with_value(column_source_value, column_name):
    # ... (existing code)
udf_replace_with_value = udf(replace_with_value, StringType())

# Function to generate UUID
def generate_uuid_id(object_id):
    # ... (existing code)
generate_uuid_udf = udf(generate_uuid_id, StringType())

# Function to generate batchID
def generate_batch_id():
    return str(uuid.uuid4().hex)[:8]
batch_id = generate_batch_id()

# Function to split document Title into BaseName and Extension
def split_document_title(documentTitle):
    # ... (existing code)

# Define Transformations 
def transform(oracle_df):
    start_time = time.time()

  # Perform transformations
  replace_values = {
      'pageCount': 0,
      'lockCreationDateTime': '1970-01-01T00:00:00.000+00:00',
      'gisClass': "NPPI"
  }
  
  transform_df = oracle_df.fillna(replace_values)
  
  transform_df = transform_df.withColumn("_id", generate_uuid_udf(col('object_id')))
  transform_df = transform_df.withColumn("initialVersionId", transform_df["_id"])
  transform_df = transform_df.withColumn("documentStoreUrl", udf_hcp_path(col('hop_blob_data')))
  transform_df = transform_df.withColumn("schemaVersion", lit("1.19"))
  transform_df = transform_df.withColumn("contentSize", col('contentSize').cast('int'))
  transform_df = transform_df.withColumn("pageCount", col('pageCount').cast('int'))
  
  decimal_columns = ['documentVersion', 'documentVersionLabel']
  for column in decimal_columns:
      transform_df = transform_df.withColumn(column, col(column).cast("string"))
  
  columns_needing_replace_value = ["documentStateList", "documentVersionLabel", "documentStatus"]
  for column in columns_needing_replace_value:
      transform_df = transform_df.withColumn(column, udf_replace_with_value(col(column), lit(column)))
  
  transform_df = transform_df.withColumn("primaryRegion", lit("US"))
  transform_df = transform_df.withColumn("primaryDomain", col('domain'))
  transform_df = transform_df.withColumn("primaryKeyType", lit(None))
  transform_df = transform_df.withColumn("primaryKey", lit(None))
  transform_df = transform_df.withColumn("skHash", lit(None))
  transform_df = transform_df.withColumn("documentHashCode", lit(None))
  transform_df = transform_df.withColumn("language", lit("EN"))
  
  transform_df = transform_df.withColumn("migrateBatchID", lit(batch_id))
  transform_df = transform_df.withColumn("documentStatus", col('documentStatus'))
  transform_df = transform_df.withColumn("createdBy", col('createdBy'))
  transform_df = transform_df.withColumn("modifiedBy", col('modifiedBy'))
  transform_df = transform_df.withColumn("migratedBy", lit(migrated_by))
  transform_df = transform_df.withColumn("documentCreatedDateTime", col('documentCreatedDateTime'))
  transform_df = transform_df.withColumn("systemCreatedDateTime", col('systemCreatedDateTime'))
  transform_df = transform_df.withColumn("systemtModifiedDateTime", col('systemModifiedDateTime'))
  transform_df = transform_df.withColumn("documentStatusDateTime", col('documentStatusDateTime'))
  transform_df = transform_df.withColumn("migrationModifiedDateTime", col('systemModifiedDateTime'))
  transform_df = transform_df.withColumn("lastAccessDateTime", cold('content_last_access_date'))
  transform_df = transform_df.withColumn("lockCreationDateTime", lit(None))
  
  # Extracting base name and extension
  transform_df = transform_df.withColumn("fileInfo", split_document_title_udf(col("documentTitle")))
  
  transform_df = transform_df.withColumn("documentBaseName", col("fileInfo.base_name"))
  transform_df = transform_df.withColumn("documentExtension", col("fileInfo.extension"))
  
  transform_df = transform_df.withColumn("mimeType", col("mimeType"))
  transform_df = transform_df.withColumn("documentStateList", array())
  transform_df = transform_df.withColumn("documentOs", array(struct(col("PsGuid").alias("documentOsGuid"), lit("AIT: ABC: CE: OS9").alias("documentOsName"), lit("1").alias("documentOsSequenceNumber"))))
  transform_df = transform_df.withColumn("documentCatCd", col("documentCatCd").cast(StringType()))
  transform_df = transform_df.withColumn("documentSubtypeCd", col("documentSubtypeCd").cast(StringType()))
  
  # Creating 'tuple' struct
  transform_df = transform_df.withColumn("tuple", struct(
      col("tupleId"), col("documentCatCd"), col("documentCatDesc"),
      col("documentSubtypeCd"), col("documentSubtypeDesc"),
      col("documentTypeCd"), col("documentTypeDesc"),
      col("documentDateFormat"), lit("YYYY-MM-DD").alias("documentDateFormat"),
      lit(None).alias("tupleExpirationDateTime")
  )).drop(*['tupleId', 'documentCatCd', 'documentCatDesc', 'documentSubtypeCd', 'documentSubtypeDesc', 'documentTypeCd', 'documentTypeDesc', 'documentDateFormat'])
  
  transform_df = transform_df.withColumn("versionTags", array(lit(None).alias("lockExceptionUser"), lit(None).alias("lockExceptionDateTime")))
  
  transform_df = transform_df.withColumn("version", struct(
      col("documentVersion"), col("initialVersionId"), col("documentVersionLabel"), col("versionTags")
  )).drop(*['documentVersion', 'initialVersionId', 'documentVersionLabel', 'versionTags'])
  
  transform_df = transform_df.withColumn("users", array(struct(
      lit(None).alias("lockExceptionUser"), lit(None).alias("lockExceptionDateTime")
  )))
  
  # Creating 'lockManagement' struct
  transform_df = transform_df.withColumn("lockManagement", struct(
      col("lockOwner"), col("lockCreationDateTime"), lit(None).alias("lockExpirationDateTime")
  )).drop(*['lockOwner', 'lockCreationDateTime', 'users'])
  
  # Creating 'storage' struct
  transform_df = transform_df.withColumn("storage", array(struct(
      lit("HCF1").alias("documentStoreType"), col("documentStoreUrl")
  ))).drop("documentStoreUrl")
  
  # Creating 'businessMetadata' array of structs
  transform_df = transform_df.withColumn("businessMetadata", array(
      struct(lit("DOC_DAY").alias("name"), lit("Integer").alias("type"), col("doc_day").cast(IntegerType()).alias("value")),
      struct(lit("DOC_MONTH").alias("name"), lit("Integer").alias("type"), col("doc_month").cast(IntegerType()).alias("value")),
      struct(lit("DOC_YEAR").alias("name"), lit("Integer").alias("type"), col("doc_year").cast(IntegerType()).alias("value")),
      struct(lit("DBA_NAME").alias("name"), lit("String").alias("type"), col("dba_name").alias("value")),
      struct(lit("FIREWALL_PRIVACY_BOOKING_ENTITY").alias("name"), lit("String").alias("type"), col("firewall_privacy_booking").alias("value")),
      struct(lit("FIREWALL_PRIVACY_JURISDICTION").alias("name"), lit("String").alias("type"), col("firewall_privacy_jurisdiction").alias("value")),
      struct(lit("LABEL").alias("name"), lit("String").alias("type"), col("label").alias("value")),
      struct(lit("LEGAL_NAME").alias("name"), lit("String").alias("type"), col("legal_name").alias("value")),
      struct(lit("UPDATED_BY").alias("name"), lit("String").alias("type"), col("updated_by").alias("value")),
      struct(lit("MERCHANT_ID").alias("name"), lit("String").alias("type"), col("merchant_id").alias("value")),
      struct(lit("SENT_BY").alias("name"), lit("String").alias("type"), col("sent_by").cast(IntegerType()).alias("value"))
  )).drop(*['doc_day', 'doc_month', 'doc_year', 'dba_name', 'firewall_privacy_booking', 'firewall_privacy_jurisdiction', 'label', 'legal_name', 'updated_by', 'merchant_id', 'sent_by'])
  
  # Creating 'businessReferenceKeys' array of structs
  transform_df = transform_df.withColumn("businessReferenceKeys", array(
      struct(lit("PREV_DOC_SOURCE").alias("name"), lit("String").alias("type"), lit(None).alias("value"))
  )).drop(*['prev_doc_source'])
  
  # Creating 'jurisdiction' array of structs
  transform_df = transform_df.withColumn("jurisdiction", array(
      struct(lit(None).alias("lockExceptionUser"), lit(None).alias("lockExceptionDateTime"))
  ))
  
  # Creating 'lobAitOwner' array of structs
  transform_df = transform_df.withColumn("lobAitOwner", array(
      struct(lit(None).alias("lockExceptionUser"), lit(None).alias("lockExceptionDateTime"))
  ))
  
  
  # Creating 'grmHold' array of structs
  transform_df = transform_df.withColumn("grmHold", col("grmHold").cast("boolean"))  
  
  # Creating 'recordManagement' array of structs 
  transform_df = transform_df.withColumn(
          "recordManagement",
          struct(
              col("recordCode"),
              lit(None).alias("recordStatusChangeDateTime"),
              col("recordTriggerStartDateTime"),
              col("jurisdiction"),
              col("lobAitOwner"),
              col("grmHold"),
              col("gisClass"),
          ),
      )
      .drop(
          [
              "recordCode",
              "recordTriggerStartDateTime",
              "jurisdiction",
              "LobAitOwner",
              "grmHold",
              "gisClass",
          ]
      )
  # Creating 'keyRetention' array of structs
  transform_df = transform_df.withColumn("keyRetention", array())  # keyRetention
   
  # Creating 'securityGroup' array of structs
  transform_df = transform_df.withColumn(
          "securityGroup",
          when(transform_df["securityGroup"] ==  "AKRIT", "AKRIT").otherwise(None)
      )
  
  # Creating 'businessDocumentStatus' array of structs
  transform_df = transform_df.withColumn(
          "businessDocumentStatus",
          when(transform_df["documentStatus"] == "Active", "Active")
          .when(transform_df["documentStatus"] == "WIP", "WIP")
          .otherwise(None)
      )
  
  # Creating 'tenants' array of structs
  transform_df = transform_df.withColumn(
          "tenants",
          array(
              struct(
                  col("domain"),
                  col("division"),
                  col("subdivision"),
                  col("securityGroup"),
                  lit(None).alias("wipExpirationDate"),
                  col("businessDocumentStatus"),
                  col("documentStatusDateTime").alias("businessDocumentStatusDateTime"),
                  col("businessMetadata"),
                  col("businessReferenceKeys"),
                  col("recordManagement"),
                  col("keyRetention"),
              )
          ),
      )
      .drop(
          [
              "domain",
              "division",
              "subdivision",
              "securityGroup",
              "businessMetadata",
              "businessReferenceKeys",
              "recordManagement",
              "keyRetention",
              "businessDocumentStatus",
          ]
      )
  
  # Creating 'bankDocument' struct
  transform_df = transform_df.withColumn("bankDocument", struct(col('tenants'))).drop(*['tenants']),
  
  # Creating 'administrativeMetadata' array of structs
  transform_df = transform_df.withColumn("administrativeMetadata",
          array(
              struct(
                  lit("None").alias("name"),
                  lit("None").alias("type"),
                  lit("None").alias("value"),
              )
          ),
      )
  
  # Creating 'batchCaptureDateTime' array of structs
  transform_df = transform_df.withColumn("batchCaptureDateTime", lit(None).cast("timestamp"))
  
  # Creating 'adminDocument' array of structs
  transform_df = transform_df.withColumn(
          "adminDocument",
          struct(
              lit(None).alias("batchIdentifier"),
              col("batchCaptureDateTime"),
              col("originationSystemBatchUid"),
              col("originationSystemDocId"),
              col("sourceDocDescription"),
              col("originationsystemCaptureTime"),
              col("originationsystemName"),
              col("channelCode"),
              col("administrativeMetadata"),
          ),
      )
      .drop(
          [
              "originationSystemBatchUid",
              "originationSystemDocId",
              "sourceDocDescription",
              "originationsystemCaptureTime",
              "originationsystemName",
              "channelCode",
              "administrativeMetadata",
              "batchCaptureDateTime",
          ]
      )
  
  # Creating 'supportingDocuments' array of structs
  transform_df = transform_df.withColumn("supportingDocuments", array())
  
  # Creating 'arrangements' array of structs
  transform_df = transform_df.withColumn("arrangements", array())
  
  # Creating 'externalReferenceKeys' array of structs
  transform_df = transform_df.withColumn("externalReferenceKeys", F.expr("filter(externalReferenceKeys, x -> x is not null)"))
  )
  # Creating 'customerReferenceKeys' array of structs
  transform_df = transform_df.withColumn("customerReferenceKeys", F.expr("filter(customerReferenceKeys, x -> x is not null)"))
  )
  # Creating 'extractedDocumentData' array of structs
  transform_df = transform_df.withColumn("extractedDocumentData", array())
  
  # Specify the desired order of columns
  desired_column_order = [
      "_id",
      "schemaVersion",
      "documentstatus",
      "documentBaseName",
      "documentExtension",
      "mimeType",
      "createdBy",
      "modifiedBy",
      "migratedBy",
      "migrateBatchID",
      "migrationModifiedDateTime",
      "systemCreatedDateTime",
      "systemModifiedDateTime",
      "documentCreatedDateTime",
      "documentBusinessDateTime",
      "lastAccessDateTime",
      "documentStatusDateTime",
      "Language",
      "contentSize",
      "pageCount",
      "skHash",
      "documentHashCode",
      "primaryRegion",
      "primaryDomain",
      "primarykeyType",
      "primarykey",
      "documentStateList",
      "documentos",
      "tuple",
      "version",
      "lockManagement",
      "storage",
      "bankDocument",
      "adminDocument",
      "supportingDocuments",
      "arrangements",
      "customerReferenceKeys",
      "externalReferenceKeys",
  ]
  
  # Select columns in the desired order
  transform_df = transform_df.select(desired_column_order)
  
  # Drop unnecessary columns
  columns_to_drop = [
      "hcp_blob_data",
      "dba_name",
      "documentTitle",
      "firewall_privacy_booking",
      "Firewall_privacy_jurisdi",
      "gci_no",
      "Label",
      "legal_name",
      "merchant_id",
      "prev_doc_source",
      "rtm_id",
      "sent_by",
      "updated_by",
      "object_id",
  ]
  transform_df = transform_df.drop(*columns_to_drop)

 return transformed_df

#Function to write to Mongo DB
def load_to_mongo(transformed_df):
    try:
        start_time = time.time()
        transformed_df.write.format("mongodb") \
            .mode("append") \
            .option("database", database) \
            .option("collection", collection) \
            .save()

        mongo_num_rows = transformed_df.count()
        logger.info(f"Number of rows written to Mongo Collection: {mongo_num_rows}")
        logger.info(f"Data write completed in {time.time() - start_time} seconds")
    except Exception as exc:
        logger.error(f"Error writing to MongoDB: {exc}")
        
#Function to calcualte migration time
def calculate_migration_time(start_time)
  end_time = datetime.now(pytz.utc)
  migration_time = end_time - start_time
  print(f"migration time: {migration_time}")

if __name__ == "__main__":
    start_time = datetime.now(pytz.utc)
    
    # Read data from Oracle
    start_time_read = datetime.now()
    oracle_df = read_data_from_oracle()
    
    if oracle_df.count() > 0:
        end_time_read = datetime.now()
        time_taken_read = end_time_read - start_time_read
        
        # Perform transformations
        start_time_transform = datetime.now()
        transform_df = transform(oracle_df)
        end_time_transform = datetime.now()
        time_taken_transform = end_time_transform - start_time_transform
        
        # Write data to MongoDB
        start_time_write = datetime.now()
        load_to_mongo(transform_df)
        
        # Calculate migration time
        calculate_migration_time(start_time)
    else:
        print("No new records found in Source")
    
    # Recon data loaded to MongoDB
    recon_migration(oracle_df)
    
# Stopping Spark session
spark.stop()
