#!/usr/bin/env python

from typing import Dict, List, Any, Optional
import argparse
import os
import sys
import boto3
from botocore.exceptions import ClientError
import mimetypes
import logging


logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
)
logger = logging.getLogger(__name__)


def generate_object_key(filepath: str, prefix: Optional[str] = None) -> str:
    """
    Generate a unique object key for R2 storage based on the file path.

    Args:
        filepath: Path to the file to be uploaded.
        prefix: Optional prefix to add to the object key.

    Returns:
        A unique object key for R2 storage.
    """
    filename = os.path.basename(filepath)

    if prefix:
        prefix = prefix.rstrip("/") + "/"
        return f"{prefix}{filename}"

    return filename


def get_content_type(filepath: str) -> str:
    """
    Determine the content type of a file.

    Args:
        filepath: Path to the file.

    Returns:
        The content type of the file.
    """
    content_type, _ = mimetypes.guess_type(filepath)
    return content_type or "application/octet-stream"


def create_r2_client(
    account_id: str, access_key_id: str, secret_access_key: str
) -> boto3.client:
    """
    Create a Cloudflare R2 client using boto3.

    Args:
        account_id: Cloudflare account ID.
        access_key_id: R2 access key ID.
        secret_access_key: R2 secret access key.

    Returns:
        A boto3 S3 client configured for Cloudflare R2.
    """
    return boto3.client(
        service_name="s3",
        endpoint_url=f"https://{account_id}.r2.cloudflarestorage.com",
        aws_access_key_id=access_key_id,
        aws_secret_access_key=secret_access_key,
        region_name="auto",  # R2 uses 'auto' as the region
    )


def upload_file(
    client: boto3.client, filepath: str, bucket_name: str, object_key: str
) -> bool:
    """
    Upload a file to Cloudflare R2.

    Args:
        client: Boto3 S3 client configured for R2.
        filepath: Path to the file to be uploaded.
        bucket_name: Name of the R2 bucket.
        object_key: Key for the object in R2 storage.

    Returns:
        True if the upload was successful, False otherwise.
    """
    try:
        content_type = get_content_type(filepath)

        with open(filepath, "rb") as file:
            client.put_object(
                Bucket=bucket_name,
                Key=object_key,
                Body=file,
                ContentType=content_type,
            )
        return True

    except ClientError as e:
        logger.error(f"Error uploading {filepath}: {e}")
        return False

    except Exception as e:
        logger.error(f"Unexpected error uploading {filepath}: {e}")
        return False


def process_directory(
    client: boto3.client,
    directory: str,
    bucket_name: str,
    prefix: Optional[str] = None,
    recursive: bool = False,
) -> Dict[str, List[Any]]:
    """
    Process a directory and upload files to R2.

    Args:
        client: Boto3 S3 client configured for R2.
        directory: Directory containing files to upload.
        bucket_name: Name of the R2 bucket.
        prefix: Optional prefix for object keys.
        recursive: If True, process subdirectories recursively.

    Returns:
        A dictionary with lists of successful and failed uploads.
    """
    if not os.path.exists(directory):
        logger.error(f"Directory '{directory}' not found")
        sys.exit(1)

    if not os.path.isdir(directory):
        logger.error(f"'{directory}' is not a directory")
        sys.exit(1)

    results = {"successful": [], "failed": []}

    for root, dirs, files in os.walk(directory):
        if not recursive and root != directory:
            continue

        for filename in files:
            filepath = os.path.join(root, filename)

            relative_path = os.path.relpath(filepath, directory)
            dir_part = os.path.dirname(relative_path)

            if prefix and dir_part and recursive:
                full_prefix = f"{prefix.rstrip('/')}/{dir_part}"
            elif prefix:
                full_prefix = prefix
            elif dir_part and recursive:
                full_prefix = dir_part
            else:
                full_prefix = None

            object_key = generate_object_key(filepath, full_prefix)

            if upload_file(client, filepath, bucket_name, object_key):
                results["successful"].append(
                    {"file": filepath, "object_key": object_key}
                )
                logger.info(f"Uploaded: {filepath} -> {object_key}")
            else:
                results["failed"].append(filepath)
                logger.warning(f"Failed to upload: {filepath}")

    return results


def main(
    directory: str,
    bucket_name: str,
    account_id: str,
    access_key_id: str,
    secret_access_key: str,
    prefix: Optional[str] = None,
    recursive: bool = False,
    verbose: bool = False,
) -> None:
    """
    Main function to upload files from a directory to Cloudflare R2.

    Args:
        directory: Directory containing files to upload.
        bucket_name: Name of the R2 bucket.
        account_id: Cloudflare account ID.
        access_key_id: R2 access key ID.
        secret_access_key: R2 secret access key.
        prefix: Optional prefix for object keys.
        recursive: If True, process subdirectories recursively.
        verbose: If True, enable verbose logging.
    """
    if verbose:
        logger.setLevel(logging.DEBUG)

    client = create_r2_client(account_id, access_key_id, secret_access_key)

    results = process_directory(
        client, directory, bucket_name, prefix, recursive
    )

    logger.info(
        f"Upload complete: {len(results['successful'])} successful, {len(results['failed'])} failed"
    )

    if results["failed"]:
        logger.warning("Failed uploads:")
        for filepath in results["failed"]:
            logger.warning(f"  - {filepath}")


def get_parser() -> argparse.ArgumentParser:
    """
    Create and configure the argument parser for the script.

    Returns:
        An ArgumentParser object configured with the script's arguments.
    """
    parser = argparse.ArgumentParser(
        description="Upload files from a directory to Cloudflare R2 storage."
    )
    parser.add_argument(
        "-d",
        "--directory",
        required=True,
        help="Directory containing files to upload.",
    )
    parser.add_argument(
        "-b",
        "--bucket-name",
        default="lingetic-speech-recordings",
        help="Name of the R2 bucket.",
    )
    parser.add_argument(
        "-a", "--account-id", required=True, help="Cloudflare account ID."
    )
    parser.add_argument(
        "-k", "--access-key-id", required=True, help="R2 access key ID."
    )
    parser.add_argument(
        "-s", "--secret-access-key", required=True, help="R2 secret access key."
    )
    parser.add_argument(
        "-p", "--prefix", help="Optional prefix for object keys in R2."
    )
    parser.add_argument(
        "-r",
        "--recursive",
        action="store_true",
        help="Recursively process subdirectories.",
    )
    parser.add_argument(
        "-v", "--verbose", action="store_true", help="Enable verbose logging."
    )

    return parser


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()

    main(
        directory=args.directory,
        bucket_name=args.bucket_name,
        account_id=args.account_id,
        access_key_id=args.access_key_id,
        secret_access_key=args.secret_access_key,
        prefix=args.prefix,
        recursive=args.recursive,
        verbose=args.verbose,
    )
