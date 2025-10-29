import argparse
import http.server
import os
import socketserver
from pathlib import Path
from typing import Any


class CORSRequestHandler(http.server.SimpleHTTPRequestHandler):
    """Simple HTTP handler that serves files from a given directory with CORS.

    Adds CORS headers and handles OPTIONS requests for browser fetches.
    """

    def __init__(self, *args: Any, directory: str | None = None, **kwargs: Any) -> None:
        # Pass the directory to the parent to restrict serving to that root
        super().__init__(*args, directory=directory, **kwargs)

    def end_headers(self) -> None:
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header(
            "Access-Control-Allow-Headers",
            "Origin, X-Requested-With, Content-Type, Accept, Authorization",
        )
        self.send_header("Access-Control-Allow-Methods", "GET, OPTIONS")
        super().end_headers()

    def do_OPTIONS(self) -> None:  # noqa: N802
        self.send_response(204)
        self.end_headers()

    # Minimal Range support for media files requested by browsers
    def send_head(self):  # type: ignore[override]
        path = self.translate_path(self.path)
        if os.path.isdir(path):
            return self.list_directory(path)
        ctype = self.guess_type(path)
        try:
            f = open(path, "rb")
        except OSError:
            self.send_error(404, "File not found")
            return None

        fs = os.fstat(f.fileno())
        file_len = fs.st_size
        range_header = self.headers.get("Range")
        if range_header:
            try:
                units, range_spec = range_header.split("=", 1)
                if units.strip().lower() != "bytes":
                    raise ValueError
                start_str, end_str = range_spec.split("-", 1)
                start = int(start_str) if start_str else 0
                end = int(end_str) if end_str else file_len - 1
                if start >= file_len:
                    # Unsatisfiable range
                    self.send_response(416)
                    self.send_header("Content-Range", f"bytes */{file_len}")
                    self.end_headers()
                    f.close()
                    return None
                if end >= file_len:
                    end = file_len - 1
                length = end - start + 1
                self.send_response(206)
                self.send_header("Content-type", ctype)
                self.send_header("Accept-Ranges", "bytes")
                self.send_header("Content-Range", f"bytes {start}-{end}/{file_len}")
                self.send_header("Content-Length", str(length))
                self.end_headers()
                self.wfile.write(f.read()[start : end + 1])
                f.close()
                return None
            except Exception:
                # If Range parsing fails, fall back to full content
                pass

        self.send_response(200)
        self.send_header("Content-type", ctype)
        self.send_header("Content-Length", str(file_len))
        self.send_header("Accept-Ranges", "bytes")
        self.end_headers()
        return f


def get_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description=(
            "Start a local HTTP server that serves files from a directory, "
            "mimicking a simple S3 bucket for local development."
        )
    )
    parser.add_argument(
        "directory",
        help="Path to the directory containing assets to serve (e.g., .mp3 files)",
    )
    parser.add_argument(
        "--host",
        default="127.0.0.1",
        help="Host to bind (default: 127.0.0.1)",
    )
    parser.add_argument(
        "--port",
        type=int,
        default=9000,
        help="Port to listen on (default: 9000)",
    )
    return parser


def main(directory: str, host: str, port: int) -> None:
    root = Path(directory).resolve()
    if not root.exists() or not root.is_dir():
        raise SystemExit(f"Directory does not exist or is not a directory: {root}")

    handler = lambda *args, **kwargs: CORSRequestHandler(
        *args, directory=str(root), **kwargs
    )

    with socketserver.ThreadingTCPServer((host, port), handler) as httpd:
        print(f"Serving {root} at http://{host}:{port}")
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("Shutting down...")
            httpd.shutdown()
