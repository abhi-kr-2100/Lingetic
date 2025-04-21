/**
 * Computes the SHA-1 hash of the input string.
 *
 * @param input The string to hash
 * @returns The SHA-1 hash as a hexadecimal string
 */
export async function sha1(input: string): Promise<string> {
  // Convert the input string to a Uint8Array
  const encoder = new TextEncoder();
  const data = encoder.encode(input);

  // Use the Web Crypto API to compute the SHA-1 hash
  const hashBuffer = await crypto.subtle.digest("SHA-1", data);

  // Convert the hash buffer to a hex string
  const hashArray = Array.from(new Uint8Array(hashBuffer));
  const hashHex = hashArray
    .map((b) => b.toString(16).padStart(2, "0"))
    .join("");

  return hashHex;
}
