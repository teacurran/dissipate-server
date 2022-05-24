import {IConfigApp} from './config.constraints'

/** Used when running app inside docker-compose, pointing to database docker */
export const config: IConfigApp = {
  database: {
    host: process.env.POSTGRES_HOST || 'dissipate-db',
    port: parseInt(process.env.POSTGRES_PORT || '') || 5432,
    username: 'dissipate',
    password: 'dissipate',
    database: process.env.POSTGRES_DB || 'dissipate',
    dialect: 'postgres',
    environment: 'dockerCompose',
  },
  redis: {
    host: process.env.REDIS_HOST || 'redis',
    port: parseInt(process.env.REDIS_PORT || '') || 6379,
  },
}
