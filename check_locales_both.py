import json
from pathlib import Path

LOCALE_DIRS = {
    "android": Path("composeApp/src/androidMain/assets/locales"),
    "ios": Path("iosApp/iosApp/locales"),
}

def check_locale_dir(name: str, locale_dir: Path):
    print(f"\n===== Checking {name}: {locale_dir} =====")

    if not locale_dir.exists():
        print(f"Directory not found: {locale_dir}")
        return

    base_file = locale_dir / "en.json"
    if not base_file.exists():
        print(f"Base file not found: {base_file}")
        return

    with open(base_file, "r", encoding="utf-8") as f:
        base = json.load(f)

    base_keys = set(base.keys())
    all_ok = True

    for file in sorted(locale_dir.glob("*.json")):
        with open(file, "r", encoding="utf-8") as f:
            data = json.load(f)

        keys = set(data.keys())
        missing = sorted(base_keys - keys)
        extra = sorted(keys - base_keys)

        if missing or extra:
            all_ok = False
            print(f"\n--- {file.name} ---")
            if missing:
                print("Missing keys:")
                for k in missing:
                    print(f"  - {k}")
            if extra:
                print("Extra keys:")
                for k in extra:
                    print(f"  - {k}")

    if all_ok:
        print("All locale files match en.json")

for name, path in LOCALE_DIRS.items():
    check_locale_dir(name, path)