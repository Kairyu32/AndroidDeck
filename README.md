# AndroidDeck

Control your Windows PC from your Android phone! remotely launch apps, files, and shortcuts over Wi-Fi.

## Features
- 📱 **Remote Launch:** Open any app or file on your PC.
- 🔍 **Auto-Scan:** Automatically finds installed apps in your Start Menu.
- 💾 **Favorites:** Save custom paths to files or games.
- ⚡ **Quick Connect:** Save your PC's IP address for one-tap connection.

## Installation

### 1. Desktop Server (Windows)
1. Download `server.exe` from the [Releases] page.
2. Run it. You might need to allow it through your firewall.

### 2. Android App
1. Download `app-release.apk` from the [Releases] page.
2. Install it on your phone.
3. Enter your PC's local IP (can be found by running "ipconfig" from the command prompt).

## Development
- **Server:** Python 3, pywin32, pystray
- **Client:** Android (Java)