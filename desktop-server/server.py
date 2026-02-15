import socket
import json
import os
import win32com.client
import threading
import pystray
import sys
from PIL import Image, ImageDraw

# --- CONFIGURATION ---
PORT = 5000
SERVER_RUNNING = True

def get_installed_apps():
    apps = []
    # 1. Start Menu (Common)
    start_menu_common = os.path.join(os.environ['ProgramData'], r'Microsoft\Windows\Start Menu\Programs')
    # 2. Start Menu (User)
    start_menu_user = os.path.join(os.environ['APPDATA'], r'Microsoft\Windows\Start Menu\Programs')

    locations = [start_menu_common, start_menu_user]

    shell = win32com.client.Dispatch("WScript.Shell")

    for location in locations:
        if os.path.exists(location):
            for root, dirs, files in os.walk(location):
                for file in files:
                    if file.endswith(".lnk"):
                        try:
                            full_path = os.path.join(root, file)
                            shortcut = shell.CreateShortCut(full_path)
                            target_path = shortcut.Targetpath
                            
                            if target_path.endswith(".exe"):
                                name = file.replace(".lnk", "")
                                apps.append({"name": name, "path": target_path})
                        except:
                            continue
    return apps

def handle_client(conn, addr):
    print(f"Connected by {addr}")
    try:
        data = conn.recv(1024).decode()
        if not data: return

        if data == "SCAN":
            apps = get_installed_apps()
            conn.sendall(json.dumps(apps).encode())
            print(f"Sent {len(apps)} apps to client.")
        
        elif data.startswith("RUN:"):
            path_to_run = data[4:]
            if os.path.exists(path_to_run):
                os.startfile(path_to_run)
                conn.sendall(b"OK")
                print(f"Launched: {path_to_run}")
            else:
                conn.sendall(b"ERROR: File not found")
                print(f"Failed to launch: {path_to_run}")

    except Exception as e:
        print(f"Error: {e}")
    finally:
        conn.close()

def start_server(icon):
    """Runs the socket server in a background thread."""
    global SERVER_RUNNING
    
    # Create the socket
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.bind(('0.0.0.0', PORT))
    s.listen()
    s.settimeout(1.0) # Check for shutdown every 1 second
    
    print(f"Server listening on port {PORT}...")
    icon.notify("Remote Launcher is running in the background.", "Server Started")

    while SERVER_RUNNING:
        try:
            conn, addr = s.accept()
            # Handle client in a new thread so we don't block
            threading.Thread(target=handle_client, args=(conn, addr)).start()
        except socket.timeout:
            continue
        except Exception as e:
            print(f"Server Error: {e}")

    s.close()
    print("Server stopped.")
    icon.stop()

def resource_path(relative_path):
    """Gets the path to the resource"""
    try:
        base_path = sys._MEIPASS
    except Exception:
        base_path = os.path.abspath(".")
    
    return os.path.join(base_path, relative_path)

def create_image():
   try:
       return Image.open(resource_path("icon.png"))
   except:
       width = 64
       height = 64
       image = Image.new('RGB', (width, height), "red")
       return image

def on_quit(icon, item):
    """Callback when the user clicks 'Quit'."""
    global SERVER_RUNNING
    SERVER_RUNNING = False
    icon.notify("Stopping server...", "Remote Launcher")


if __name__ == '__main__':
    # Create the System Tray Icon
    icon = pystray.Icon("RemoteLauncher")
    icon.icon = create_image()
    icon.title = "Remote Launcher Server"
    icon.menu = pystray.Menu(
        pystray.MenuItem("Quit", on_quit)
    )
    # Starts the server on a seperate thread
    server_thread = threading.Thread(target=start_server, args=(icon,))
    server_thread.start()
    icon.run()