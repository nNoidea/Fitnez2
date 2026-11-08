#!/usr/bin/env python3
"""Fitnez2 App Icon Generator.

Converts icon.svg (Single Source of Truth) to:
 - icon_round.svg / icon_round.png (1024x1024 with white circle background for README & web)
 - fastlane/metadata/android/en-US/images/icon.png (512x512 store icon)
 - app/src/main/res/mipmap-*/ (Android launcher icons: standard, round, adaptive foreground)

Uses standard library Python + ffmpeg (zero extra pip dependencies).
"""

import shutil
import subprocess
import sys
from pathlib import Path

# Android launcher density mapping: (standard_size_px, adaptive_fg_size_px)
DENSITIES: dict[str, tuple[int, int]] = {
    "mdpi": (48, 108),
    "hdpi": (72, 162),
    "xhdpi": (96, 216),
    "xxhdpi": (144, 324),
    "xxxhdpi": (192, 432),
}


def run_ffmpeg(
    svg_path: Path,
    out_path: Path,
    scale_w: int,
    scale_h: int,
    pad_w: int | None = None,
    pad_h: int | None = None,
    is_webp: bool = False,
) -> None:
    out_path.parent.mkdir(parents=True, exist_ok=True)
    filters = [f"scale={scale_w}:{scale_h}:flags=lanczos"]
    if pad_w is not None and pad_h is not None:
        pad_x = (pad_w - scale_w) // 2
        pad_y = (pad_h - scale_h) // 2
        filters.append(f"pad={pad_w}:{pad_h}:{pad_x}:{pad_y}:color=0x00000000")

    cmd = [
        "ffmpeg",
        "-y",
        "-v",
        "error",
        "-i",
        str(svg_path),
        "-vf",
        ",".join(filters),
    ]
    if is_webp:
        cmd.extend(["-c:v", "libwebp", "-lossless", "1"])
    cmd.append(str(out_path))

    subprocess.run(cmd, check=True)


def generate_all_icons(root_dir: Path) -> None:
    svg_src = root_dir / "icon.svg"
    res_dir = root_dir / "app" / "src" / "main" / "res"
    fastlane_dir = root_dir / "fastlane" / "metadata" / "android" / "en-US" / "images"

    if not svg_src.is_file():
        sys.exit(f"Error: {svg_src} not found!")

    if not shutil.which("ffmpeg"):
        sys.exit("Error: ffmpeg is required to render icons from SVG.")

    print(f"Generating icons from {svg_src}...")

    # Extract inner geometry from icon.svg to build round version with white circular background
    svg_content = svg_src.read_text(encoding="utf-8")
    start_idx = svg_content.find("<g")
    end_idx = svg_content.rfind("</g>") + 4
    inner_g = svg_content[start_idx:end_idx] if start_idx != -1 and end_idx != -1 else ""

    round_svg_path = root_dir / "icon_round.svg"
    round_svg_content = f"""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" width="512" height="512">
  <!-- White circular background for GitHub Dark Mode, README, and round launchers -->
  <circle cx="256" cy="256" r="248" fill="#FFFFFF" />
  <g transform="translate(256, 256) scale(0.8) translate(-256, -256)">
    {inner_g}
  </g>
</svg>
"""
    round_svg_path.write_text(round_svg_content, encoding="utf-8")

    # 1. Master icon_round.png (1024x1024, with white circle background for README/web)
    print("  -> icon_round.png (1024x1024, white circle background)")
    run_ffmpeg(round_svg_path, root_dir / "icon_round.png", 1024, 1024)

    # 2. Fastlane store icon (512x512, white circle background)
    print("  -> fastlane/metadata/android/en-US/images/icon.png (512x512, white circle background)")
    run_ffmpeg(round_svg_path, fastlane_dir / "icon.png", 512, 512)

    # 4. Android Mipmaps
    for density, (std_size, fg_size) in DENSITIES.items():
        density_dir = res_dir / f"mipmap-{density}"

        # Standard legacy launcher icon (80% content padding inside canvas)
        content_std = int(std_size * 0.8)
        std_icon = density_dir / "ic_launcher.webp"
        print(f"  -> mipmap-{density}: ic_launcher.webp ({std_size}x{std_size})")
        run_ffmpeg(
            svg_src,
            std_icon,
            content_std,
            content_std,
            pad_w=std_size,
            pad_h=std_size,
            is_webp=True,
        )

        # Round launcher icon with white circle background (100% full circle)
        round_icon = density_dir / "ic_launcher_round.webp"
        print(f"  -> mipmap-{density}: ic_launcher_round.webp ({std_size}x{std_size})")
        run_ffmpeg(
            round_svg_path,
            round_icon,
            std_size,
            std_size,
            is_webp=True,
        )

        # Adaptive Foreground (50% content size inside 108dp canvas for Android circle/squircle masking)
        content_fg = int(fg_size * 54 / 108)
        fg_icon = density_dir / "ic_launcher_foreground.webp"
        print(f"  -> mipmap-{density}: ic_launcher_foreground.webp ({fg_size}x{fg_size})")
        run_ffmpeg(
            svg_src,
            fg_icon,
            content_fg,
            content_fg,
            pad_w=fg_size,
            pad_h=fg_size,
            is_webp=True,
        )

    print("All icons successfully updated from icon.svg!")


if __name__ == "__main__":
    project_root = Path(__file__).resolve().parent.parent
    generate_all_icons(project_root)
