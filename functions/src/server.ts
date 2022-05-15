import express, {Request, Response} from "express"
import bodyParser = require("body-parser");
import * as firebaseAdmin from "firebase-admin"
import cors from "cors"

const PORT = process.env.PORT || 3000

async function start() {
  firebaseAdmin.initializeApp({
    projectId: "gffft-auth",
    storageBucket: "gffft-auth.appspot.com",
  })

  const app = express()

  app.disable("x-powered-by")

  // TODO: restrict for prod
  const corsOptions: cors.CorsOptions = {
    origin: true,
  }
  const corsMiddleware = cors(corsOptions)
  app.use(corsMiddleware)

  app.use(bodyParser.json({limit: "50mb"}))
  app.use(bodyParser.urlencoded({extended: true}))

  app.get("/_ah/warmup", (req: Request, res: Response) => {
    res.sendStatus(204)
  })

  app.listen(PORT, () => {
    console.info(`Server listening on port: ${PORT}`)
  })
}

start()

