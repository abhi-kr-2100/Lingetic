#!/usr/bin/env python

import os
import argparse
from pdf2image import convert_from_path


def convert_pdf_to_images(pdf_path: str, output_dir: str) -> None:
    """Convert each page of a PDF file to JPEG image.

    Args:
        pdf_path: Path to the PDF file
        output_dir: Directory to save the JPEG images
    """
    # Create output directory if it doesn't exist
    os.makedirs(output_dir, exist_ok=True)

    # Get PDF filename without extension
    pdf_basename = os.path.splitext(os.path.basename(pdf_path))[0]

    # Convert PDF to images
    images = convert_from_path(pdf_path)

    # Save each page as JPEG
    for i, image in enumerate(images):
        output_path = os.path.join(
            output_dir, f"{pdf_basename}_page_{i + 1}.jpg"
        )
        image.save(output_path, "JPEG")
        print(f"Saved {output_path}")


def main(pdf_path: str, output_dir: str = "output") -> None:
    """Entry point for the script.

    Args:
        pdf_path: Path to the PDF file to convert
        output_dir: Directory to save the JPEG images (default: "output")
    """
    if not os.path.exists(pdf_path):
        raise FileNotFoundError(f"PDF file not found: {pdf_path}")

    if not pdf_path.lower().endswith(".pdf"):
        raise ValueError("Input file must be a PDF")

    convert_pdf_to_images(pdf_path, output_dir)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="Convert PDF pages to JPEG images"
    )
    parser.add_argument("pdf_path", help="Path to the PDF file")
    parser.add_argument(
        "--output-dir",
        "-o",
        default="output",
        help="Directory to save JPEG images (default: output)",
    )
    args = parser.parse_args()
    main(args.pdf_path, args.output_dir)
