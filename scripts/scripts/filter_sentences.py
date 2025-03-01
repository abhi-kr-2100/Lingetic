import json
import sys


def main(lang_code: str, file_path: str):
    try:
        with open(file_path, "r", encoding="utf-8") as file:
            sentences = json.load(file)

        filtered_sentences = [
            sentence
            for sentence in sentences
            if any(
                t["lang"] == lang_code for t in sentence.get("translations", [])
            )
        ]

        json.dump(filtered_sentences, sys.stdout, ensure_ascii=False, indent=2)
    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print(
            "Usage: python script.py <language_code> <file_path>",
            file=sys.stderr,
        )
        sys.exit(1)

    language_code = sys.argv[1]
    file_path = sys.argv[2]
    main(language_code, file_path)
