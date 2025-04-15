// This file configures the initialization of Sentry on the server.
// The config you add here will be used whenever the server handles a request.
// https://docs.sentry.io/platforms/javascript/guides/nextjs/

import * as Sentry from "@sentry/nextjs";

Sentry.init({
  dsn: "https://86de7112fa096440cb5a8ab83332bfb4@o4508705106952192.ingest.de.sentry.io/4508705241432144",

  // Disable Sentry in development mode
  enabled: process.env.NEXT_PUBLIC_ENVIRONMENT !== "development",

  // Define how likely traces are sampled. Adjust this value in production, or use tracesSampler for greater control.
  tracesSampleRate: 1,

  // Setting this option to true will print useful information to the console while you're setting up Sentry.
  debug: false,
});
