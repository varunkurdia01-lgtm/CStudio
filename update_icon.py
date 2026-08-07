import os
import shutil
import base64

def main():
    res_dir = 'app/src/main/res'
    
    # 1. Create the directory app/src/main/res/drawable-nodpi if it does not exist.
    nodpi_dir = os.path.join(res_dir, 'drawable-nodpi')
    os.makedirs(nodpi_dir, exist_ok=True)
    
    # 2. Copy the uploaded "custom_icon.png" file to app/src/main/res/drawable-nodpi/custom_icon.png.
    src_icon = 'custom_icon.png'
    dest_icon = os.path.join(nodpi_dir, 'custom_icon.png')
    
    # Find the file in possible locations
    found_path = None
    search_paths = [src_icon, f"/{src_icon}", f"/workspace/{src_icon}", "1000048311.png"]
    for p in search_paths:
        if os.path.exists(p):
            found_path = p
            break
            
    if found_path:
        print(f"Found icon at {found_path}, copying to {dest_icon}")
        shutil.copy(found_path, dest_icon)
    else:
        print(f"Warning: {src_icon} not found. Creating a transparent placeholder to prevent build errors.")
        # 1x1 transparent PNG
        png_data = base64.b64decode('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAACklEQVR4nGMAAQAABQABDQottAAAAABJRU5ErkJggg==')
        with open(dest_icon, 'wb') as f:
            f.write(png_data)
        
    # 3. Overwrite the file app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml
    anydpi_dir = os.path.join(res_dir, 'mipmap-anydpi-v26')
    os.makedirs(anydpi_dir, exist_ok=True)
    with open(os.path.join(anydpi_dir, 'ic_launcher.xml'), 'w') as f:
        f.write('''<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>''')

    # 4. Overwrite the file app/src/main/res/drawable/ic_launcher_background.xml
    drawable_dir = os.path.join(res_dir, 'drawable')
    os.makedirs(drawable_dir, exist_ok=True)
    with open(os.path.join(drawable_dir, 'ic_launcher_background.xml'), 'w') as f:
        f.write('''<?xml version="1.0" encoding="utf-8"?>
<color xmlns:android="http://schemas.android.com/apk/res/android" android:color="#FFFFFF"/>''')

    # 5. Overwrite the file app/src/main/res/drawable/ic_launcher_foreground.xml
    with open(os.path.join(drawable_dir, 'ic_launcher_foreground.xml'), 'w') as f:
        f.write('''<?xml version="1.0" encoding="utf-8"?>
<inset xmlns:android="http://schemas.android.com/apk/res/android"
    android:drawable="@drawable/custom_icon"
    android:inset="18%" />''')

    # 6. Safely delete all old .webp and .png files named ic_launcher or ic_launcher_round
    mipmaps = ['mipmap-mdpi', 'mipmap-hdpi', 'mipmap-xhdpi', 'mipmap-xxhdpi', 'mipmap-xxxhdpi']
    for m in mipmaps:
        mdir = os.path.join(res_dir, m)
        if os.path.exists(mdir):
            for file in os.listdir(mdir):
                if file.startswith('ic_launcher') and (file.endswith('.png') or file.endswith('.webp')):
                    try:
                        os.remove(os.path.join(mdir, file))
                        print(f"Deleted {os.path.join(mdir, file)}")
                    except Exception as e:
                        print(f"Failed to delete {file}: {e}")

if __name__ == '__main__':
    main()
