import { ClientOpts } from 'redis';
import { Options } from 'sequelize';

/**
 * this file is here to get a typed environment
 */
export type EnvironmentType =
  | 'local'
  | 'dockerCompose'
  | 'production'
  | 'devProxy'
  | 'stagingProxy';

export interface IConfigApp {
  database: IDatabaseConfiguration;
  redis: ClientOpts;

  storage: {
    operations: string;
    staticData: string;
    shareData: string;
    userData: string;
  };

  serviceAccount: {
    email: string;
  };

  frontend_url: string;
  iap_client_id?: string;
  api_key: string;
  appengine_region_code: string;
}

interface IDatabaseConfiguration extends Options {
  environment: EnvironmentType;
}
