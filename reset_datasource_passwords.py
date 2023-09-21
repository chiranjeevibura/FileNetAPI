from weblogic.management.security.authentication import UserPasswordLoginModule
datasources = [
    {"name": "ds1", "username": "user1", "password": "new_password1"},
    {"name": "ds2", "username": "user2", "password": "new_password2"},
    # Add the rest of your datasources here
]
for ds_info in datasources:
    datasource_name = ds_info["name"]
    username = ds_info["username"]
    new_password = ds_info["password"]
    
    try:
        # Connect to WebLogic Server
        connect('admin_user', 'admin_password', 'admin_url')

        # Retrieve the datasource MBean
        ds = getMBean('/JDBCSystemResources/' + datasource_name + '/JDBCResource/' + datasource_name)

        # Set the new password for the datasource
        ds.JDBCConnectionPoolParams.Password = new_password

        # Save and activate changes
        save()
        activate()

        print(f"Password for datasource {datasource_name} reset successfully.")

    except Exception as e:
        print(f"Failed to reset password for datasource {datasource_name}: {str(e)}")
    finally:
        # Disconnect from WebLogic Server
        disconnect()

*************
wlst.sh reset_datasource_passwords.py
*************
  
This script will iterate through your list of datasources, connect to WebLogic Server, reset the password for each datasource, save the changes, and then disconnect. If any errors occur during the process, it will print an error message and continue with the next datasource.

Make sure to replace "admin_user", "admin_password", and "admin_url" with the appropriate credentials and WebLogic Server URL for administrative access. Also, adjust the datasources list to include all 24 datasources along with their corresponding username/password combinations.

Ensure that you have appropriate permissions and access rights to perform these administrative operations on WebLogic Server.
