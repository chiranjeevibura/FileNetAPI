# List of datasources and their new passwords
datasources = [
    {"name": "ds1", "new_password": "new_password1"},
    {"name": "ds2", "new_password": "new_password2"},
    # Add more datasources here
]

print("*** Trying to Connect.... *****")
connect('weblogic', 'weblogic', 't3://localhost:7001')
print("*** Connected *****")
cd('Servers/AdminServer')
edit()
startEdit()
cd('JDBCSystemResources')

# Iterate through the datasources and reset passwords
for ds_info in datasources:
    ds_name = ds_info["name"]
    new_password = ds_info["new_password"]
    try:
        ds_path = '/JDBCSystemResources/' + ds_name + '/JDBCResource/' + ds_name

        print('Changing Password for DataSource ', ds_name)
        cd(ds_path + '/JDBCDriverParams/' + ds_name)
        set('PasswordEncrypted', new_password)

        print("*** CONGRATES !!! Password has been changed for DataSource: ", ds_name)
        print('')

    except Exception as e:
        print('Failed to reset password for DataSource ', ds_name, ':', str(e))
        print('')

save()
activate()
