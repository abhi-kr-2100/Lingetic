#!/usr/bin/env python

from sys import argv, exit, stderr
from typing import Iterable

import scripts


def main(argv: Iterable[str]):
    if len(argv) < 1:
        print("Usage: main.py script arguments...", file=stderr)
        exit(1)

    script_name = argv[0]
    script_args = argv[1:]

    if not hasattr(scripts, script_name):
        print(f"Error: script '{script_name}' not found", file=stderr)
        exit(1)

    script_module = getattr(scripts, script_name)

    # Get the parser from the script module if it exists
    if hasattr(script_module, "get_parser"):
        parser = script_module.get_parser()
        args = parser.parse_args(script_args)
        script_module.main(**vars(args))
    else:
        # Fallback to old behavior for scripts without parsers
        script_module.main(*script_args)


if __name__ == "__main__":
    main(argv[1:])
