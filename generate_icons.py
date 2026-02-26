import os
from PIL import Image

def generate_icons(source_path, res_dir, zoom=0.75):
    try:
        img = Image.open(source_path).convert("RGBA")
    except Exception as e:
        print(f"Error opening image: {e}")
        return

    standard_sizes = {
        "mdpi": 48,
        "hdpi": 72,
        "xhdpi": 96,
        "xxhdpi": 144,
        "xxxhdpi": 192,
    }

    adaptive_sizes = {
        "mdpi": 108,
        "hdpi": 162,
        "xhdpi": 216,
        "xxhdpi": 324,
        "xxxhdpi": 432,
    }

    def make_square(image, size, is_adaptive=False):
        target_content_size = int(size * (2/3) * zoom) if is_adaptive else int(size * zoom)
        
        width, height = image.size
        aspect = width / height
        
        if aspect > 1:
            new_width = target_content_size
            new_height = int(target_content_size / aspect)
        else:
            new_height = target_content_size
            new_width = int(target_content_size * aspect)
            
        resized = image.resize((new_width, new_height), Image.Resampling.LANCZOS)
        
        square = Image.new("RGBA", (size, size), (0, 0, 0, 0))
        
        offset_x = (size - new_width) // 2
        offset_y = (size - new_height) // 2
        
        square.paste(resized, (offset_x, offset_y), resized)
        
        return square

    for density, size in standard_sizes.items():
        density_dir = os.path.join(res_dir, f"mipmap-{density}")
        os.makedirs(density_dir, exist_ok=True)
        
        standard_img = make_square(img, size, is_adaptive=False)
        standard_img.save(os.path.join(density_dir, "ic_launcher.png"))
        standard_img.save(os.path.join(density_dir, "ic_launcher_round.png"))

    for density, size in adaptive_sizes.items():
        density_dir = os.path.join(res_dir, f"mipmap-{density}")
        os.makedirs(density_dir, exist_ok=True)
        
        foreground_img = make_square(img, size, is_adaptive=True)
        foreground_img.save(os.path.join(density_dir, "ic_launcher_foreground.png"))
        
    print(f"Successfully generated all icons at {zoom}x!")

if __name__ == "__main__":
    generate_icons("icon.png", "app/src/main/res", zoom=0.75)
