import log from "./logger";

export default function assert(
  isAssertionTrue: boolean,
  assertionDescription: string
) {
  if (isAssertionTrue) {
    return;
  }

  log(assertionDescription, "fatal");
  throw new Error("Assertion failed.");
}
