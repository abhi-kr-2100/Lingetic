import { captureMessage } from "@sentry/nextjs";

type Severity = "fatal" | "error" | "warning" | "info";

export default function log(message: string, severity: Severity) {
  switch (severity) {
    case "fatal":
    case "error":
      if (process.env.NODE_ENV === "production") {
        captureMessage(message, {
          level: severity,
        });
      }
      console.error(message);
      break;
    case "warning":
      console.warn(message);
      break;
    case "info":
      console.info(message);
      break;
  }
}
