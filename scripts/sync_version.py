#!/usr/bin/env python3
import sys
import re
import subprocess
from pathlib import Path

def get_repo_root() -> Path:
    # Walk upwards from this script's directory until VERSION and app/build.gradle.kts are found
    curr = Path(__file__).resolve().parent
    while curr != curr.parent:
        if (curr / "VERSION").exists() and (curr / "app" / "build.gradle.kts").exists():
            return curr
        curr = curr.parent
    return Path.cwd()

def compute_version_code(version_name: str) -> int:
    parts = version_name.strip().split(".")
    if len(parts) != 3:
        raise ValueError(f"Version '{version_name}' must follow semver format X.Y.Z (e.g. 0.4.7)")
    major, minor, patch = (int(p) for p in parts)
    return major * 100_000 + minor * 100 + patch

def sync_build_gradle(repo_root: Path, version_name: str, version_code: int) -> bool:
    gradle_file = repo_root / "app" / "build.gradle.kts"
    content = gradle_file.read_text(encoding="utf-8")

    # Check if already in sync
    vc_match = re.search(r"versionCode\s*=\s*(\d+)", content)
    vn_match = re.search(r'versionName\s*=\s*"([^"]+)"', content)

    already_synced = (
        vc_match is not None and int(vc_match.group(1)) == version_code and
        vn_match is not None and vn_match.group(1) == version_name
    )

    # Check if old dynamic variables exist
    has_dynamic_vars = "val appVersion:" in content or "val appVersionCode:" in content

    if already_synced and not has_dynamic_vars:
        return False  # No changes needed

    # Remove legacy dynamic declarations if present
    new_content = re.sub(
        r"// Single source of truth:.*?\n\s*\.let\s*\{.*?\}\s*\n",
        "",
        content,
        flags=re.DOTALL
    )
    new_content = re.sub(r'val appVersion:\s*String\s*=\s*file\(.*?\)\.readText\(\)\.trim\(\)\s*\n', '', new_content)
    new_content = re.sub(r'val appVersionCode:\s*Int\s*=\s*appVersion\.split\(.*?\)\s*\n', '', new_content)
    new_content = re.sub(r'\s*\.let\s*\{.*?patch\.toInt\(\)\s*\}\s*\n', '', new_content)

    # Replace versionCode and versionName in defaultConfig
    new_content = re.sub(
        r"versionCode\s*=\s*([a-zA-Z0-9_]+)",
        f"versionCode = {version_code}",
        new_content,
        count=1
    )
    new_content = re.sub(
        r'versionName\s*=\s*([^\n]+)',
        f'versionName = "{version_name}"',
        new_content,
        count=1
    )

    gradle_file.write_text(new_content, encoding="utf-8")
    return True

def main():
    repo_root = get_repo_root()
    version_file = repo_root / "VERSION"

    is_hook = "--hook" in sys.argv
    is_check = "--check" in sys.argv

    # Check for explicit version argument
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    new_version_arg = args[0] if args else None

    if new_version_arg:
        version_file.write_text(f"{new_version_arg.strip()}\n", encoding="utf-8")

    version_name = version_file.read_text(encoding="utf-8").strip()
    version_code = compute_version_code(version_name)

    if is_check:
        gradle_file = repo_root / "app" / "build.gradle.kts"
        content = gradle_file.read_text(encoding="utf-8")
        vc_match = re.search(r"versionCode\s*=\s*(\d+)", content)
        vn_match = re.search(r'versionName\s*=\s*"([^"]+)"', content)
        if (
            vc_match and int(vc_match.group(1)) == version_code and
            vn_match and vn_match.group(1) == version_name and
            "val appVersion:" not in content
        ):
            print(f"VERSION is in sync: {version_name} ({version_code})")
            sys.exit(0)
        else:
            print(f"VERSION mismatch! Expected {version_name} ({version_code})")
            sys.exit(1)

    changed = sync_build_gradle(repo_root, version_name, version_code)

    if is_hook:
        if changed:
            # Stage the changed file
            subprocess.run(["git", "add", "app/build.gradle.kts"], cwd=repo_root, check=False)
            print(f"\n[version-sync] Mismatch detected between VERSION and app/build.gradle.kts!")
            print(f"[version-sync] Auto-fixed app/build.gradle.kts -> versionName \"{version_name}\", versionCode {version_code}")
            print(f"[version-sync] Changes have been staged for you.")
            print(f"[version-sync] Commit blocked to let you review. Please re-run: git commit\n")
            sys.exit(1)
        sys.exit(0)

    if changed:
        print(f"Synced app/build.gradle.kts to version {version_name} (versionCode {version_code})")
    else:
        print(f"app/build.gradle.kts is already in sync with VERSION {version_name} (versionCode {version_code})")

if __name__ == "__main__":
    main()
