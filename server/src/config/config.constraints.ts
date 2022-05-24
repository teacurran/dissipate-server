import {ClientOpts} from 'redis'
import {Options} from 'sequelize'

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
}

interface IDatabaseConfiguration extends Options {
  environment: EnvironmentType;
}
