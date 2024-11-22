import { Component } from '@angular/core';
import { ServerStatusService } from 'src/app/services/server-status.service';

type TransformedResponse = { serverName: string; serviceName: string; serverStatus: string };

@Component({
  selector: 'app-environment-validator-prod',
  templateUrl: './environment-validator-prod.component.html',
  styleUrls: ['./environment-validator-prod.component.scss']
})
export class EnvironmentValidatorProdComponent {
  selectedEnv: string = 'PROD';
  selectedEnvToAdd: string[] = ['PROD'];
  servers: any[] = [];
  selectedServer: string = '';
  data: TransformedResponse[] = [];
  isServerStatusResponse: boolean = false;
  isStartStopResponse: boolean = false;
  startStopResponse: any = null;
  selectedService: string = '';
  serviceNames: string[];
  serverMap = {
    "CREATE": [
      { label: "abc-create-create-pl.abc-create.com", value: "abc-create-create-pl.abc-create.com" },
      { label: "ah--001.sdi.corp.abc-create.com (ah--001/8091)", value: "ah--001.sdi.corp.abc-create.com-8091" }
    ],
    "AIL": [
      { label: "AIL Server 1", value: "ail-server-1" },
      { label: "AIL Server 2", value: "ail-server-2" }
    ]
  };

  constructor(private serverStatusService: ServerStatusService) {
    this.serviceNames = Object.keys(this.serverMap);
  }

  generateServers(selectedService: string): void {
    console.log('generateServers==', this.selectedService);
    this.servers = this.serverMap[selectedService] || [];
    this.selectedServer = '';
  }

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
            // Group data by service name and transform it for 3-column view
            this.data = Object.entries(response).map(([key, value]) => ({
              serverName: key,
              serviceName: this.selectedService,
              serverStatus: value
            }));
          },
          (error) => console.log('Error fetching server details:', error)
        );
      } else {
        // For Start/Stop operations
        this.isStartStopResponse = true;
        this.startStopResponse = {
          action,
          selectedEnv: this.selectedEnv,
          selectedServer: selectedServerPayload,
        };
      }
    }
  }
}
