import { PostHog } from "posthog-node";
import assert from "./utilities/assert";

export default function PostHogClient() {
  assert(
    process.env.NEXT_PUBLIC_POSTHOG_KEY != null,
    "PostHog key is required"
  );
  const posthogClient = new PostHog(process.env.NEXT_PUBLIC_POSTHOG_KEY!, {
    host: "https://eu.i.posthog.com",
    flushAt: 1,
    flushInterval: 0,
  });
  return posthogClient;
}
