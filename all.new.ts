checkServerStatus(action: string): void {
  this.isServerStatusResponse = false;
  this.isStartStopResponse = false;

  if (action !== 'Status') {
    alert(`Do you want to ${action} Server?`);
  }

  if (this.selectedService) {
    // Check if "ALL" is selected
    const selectedServerPayload =
      this.selectedServer === 'ALL'
        ? this.serverMap[this.selectedService].map((server) => server.value) // Get all servers for the service
        : this.selectedServer.includes('|')
        ? this.selectedServer.split('|')
        : [this.selectedServer];

    const payload = {
      environment: this.selectedEnv,
      serversToCheck: selectedServerPayload,
      actionId: action,
    };

    console.log(payload);

    if (action === 'Status') {
      this.serverStatusService.getServerDetails(payload).subscribe(
        (response: any) => {
          this.isServerStatusResponse = true;

          // Group data by service name, handling "ALL"
          this.data =
            this.selectedServer === 'ALL'
              ? this.serverMap[this.selectedService].map((server) => ({
                  serverName: server.label,
                  serviceName: this.selectedService, // Use the selected service
                  serverStatus: response[server.value] || 'UNKNOWN', // Fallback if status is missing
                }))
              : Object.entries(response).map(([key, value]) => ({
                  serverName: key,
                  serviceName: this.selectedService,
                  serverStatus: value,
                }));
        },
        (error) => console.log('Error fetching server details:', error)
      );
    } else {
      this.isStartStopResponse = true;
      this.startStopResponse = {
        action,
        selectedEnv: this.selectedEnv,
        selectedServer: selectedServerPayload,
      };
    }
  }
}
