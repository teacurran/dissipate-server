import { IConfigApp } from './config.constraint';

/** Used when running app inside docker-compose, pointing to database docker */
export const config: IConfigApp = {
  database: {
    host: process.env.POSTGRES_HOST || 'restor-db',
    port: parseInt(process.env.POSTGRES_PORT) || 5432,
    username: 'restor',
    password: 'restor',
    database: process.env.POSTGRES_DB || 'restor',
    dialect: 'postgres',
    environment: 'dockerCompose'
  },
  redis: {
    host: process.env.REDIS_HOST || 'redis',
    port: parseInt(process.env.REDIS_PORT) || 6379
  },
  storage: {
    operations: 'gweb-restor-dev-earthengine-tasks-cache',
    tileset: 'restor-dev-tilesets',
    staticData: 'restor-assets',
    shareData: 'gweb-restor-dev-share-data',
    userData: 'gweb-restor-dev-user-data'
  },
  serviceAccount: {
    email: 'earthengine-service-account@gweb-restor-dev.iam.gserviceaccount.com'
  },
  // frontend_url: 'http://web:3000'
  frontend_url: 'https://gweb-restor-dev.uc.r.appspot.com',
  iap_client_id:
    process.env.IAP_CLIENT_ID ||
    '114750871121-9spqf94risipst5s0kfejgp94fr25o36.apps.googleusercontent.com',
  api_key: 'AIzaSyD0WGhPp1k3vajw_lYTTI2tGYtd8BEOFaI',
  appengine_region_code: 'oa'
};
