checkServerStatus(action: string): void {
  this.isServerStatusResponse = false;
  this.isStartStopResponse = false;

  if (action !== 'Status') {
    alert(`Do you want to ${action} Server?`);
  }

  if (this.selectedServer && this.selectedService) {
    const selectedServerPayload = this.selectedServer.includes('|') 
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
          this.data = Object.entries(response).map(([key, value]) => ({
            serverName: key,
            serviceName: this.selectedService,
            serverStatus: value
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
