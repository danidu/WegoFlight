# Docker Installation Guide for Windows

This guide will help you install Docker Engine and Docker Compose on your Windows machine.

## Prerequisites

- Windows 10 64-bit: Pro, Enterprise, or Education (Build 19041 or higher)
- OR Windows 11 64-bit: Home or Pro (Build 22000 or higher)
- WSL 2 feature enabled
- Virtualization enabled in BIOS

## Installation Steps

### Step 1: Enable WSL 2 (Windows Subsystem for Linux)

1. **Open PowerShell as Administrator**:
   - Press `Windows Key + X`
   - Select "Windows PowerShell (Admin)" or "Terminal (Admin)"

2. **Run the following commands**:
   ```powershell
   # Enable WSL feature
   dism.exe /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart

   # Enable Virtual Machine Platform
   dism.exe /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart

   # Restart your computer
   Restart-Computer
   ```

3. **After restart, set WSL 2 as default**:
   ```powershell
   wsl --set-default-version 2
   ```

### Step 2: Download Docker Desktop

1. **Download Docker Desktop for Windows**:
   - Visit: https://www.docker.com/products/docker-desktop/
   - Click "Download for Windows"
   - The file will be named: `Docker Desktop Installer.exe`

   **Direct Download Link**: https://desktop.docker.com/win/main/amd64/Docker%20Desktop%20Installer.exe

### Step 3: Install Docker Desktop

1. **Run the installer**:
   - Double-click `Docker Desktop Installer.exe`
   - If prompted by User Account Control, click "Yes"

2. **Installation options**:
   - ✅ Check "Use WSL 2 instead of Hyper-V" (recommended)
   - ✅ Check "Add shortcut to desktop" (optional)
   - Click "OK"

3. **Wait for installation to complete**:
   - The installer will download and install Docker Desktop
   - This may take several minutes

4. **Restart when prompted**:
   - Click "Close and restart" when installation completes
   - Your computer will restart

### Step 4: Start Docker Desktop

1. **After restart, start Docker Desktop**:
   - Find Docker Desktop in the Start menu
   - Click to launch it
   - You may see a "Docker Desktop starting..." message

2. **Accept the service agreement**:
   - Read and accept the terms of service
   - Click "Accept"

3. **Wait for Docker to start**:
   - Docker Desktop will start in the system tray
   - The Docker icon should show "Docker Desktop is running" when ready
   - This may take 1-2 minutes on first start

### Step 5: Verify Installation

1. **Open PowerShell or Command Prompt**

2. **Check Docker version**:
   ```bash
   docker --version
   ```
   Expected output: `Docker version 24.x.x, build xxxxx`

3. **Check Docker Compose version**:
   ```bash
   docker compose version
   ```
   Expected output: `Docker Compose version v2.x.x`

4. **Test Docker**:
   ```bash
   docker run hello-world
   ```
   This should download and run a test container, showing:
   ```
   Hello from Docker!
   This message shows that your installation appears to be working correctly.
   ```

## Troubleshooting

### Issue: "WSL 2 installation is incomplete"

**Solution**:
1. Download the WSL2 Linux kernel update package:
   - https://wslstorestorage.blob.core.windows.net/wslblob/wsl_update_x64.msi
2. Run the installer
3. Restart Docker Desktop

### Issue: "Hardware assisted virtualization and data execution protection must be enabled"

**Solution**:
1. **Enable Virtualization in BIOS**:
   - Restart your computer
   - Enter BIOS/UEFI settings (usually F2, F10, F12, or Del during boot)
   - Find "Virtualization" or "Intel VT-x" or "AMD-V"
   - Enable it
   - Save and exit

2. **Enable Hyper-V** (if using Hyper-V instead of WSL 2):
   ```powershell
   Enable-WindowsOptionalFeature -Online -FeatureName Microsoft-Hyper-V -All
   ```

### Issue: Docker Desktop won't start

**Solution**:
1. Check Windows updates:
   - Settings → Update & Security → Windows Update
   - Install all pending updates
   - Restart

2. Check WSL 2 status:
   ```powershell
   wsl --status
   ```
   Should show: `Default Version: 2`

3. Restart Docker Desktop:
   - Right-click Docker icon in system tray
   - Select "Restart Docker Desktop"

### Issue: "Docker daemon is not running"

**Solution**:
1. Make sure Docker Desktop is running (check system tray)
2. If not running, start Docker Desktop from Start menu
3. Wait for it to fully start (whale icon should be steady, not animated)

## Quick Verification Commands

Run these commands to verify everything is working:

```bash
# Check Docker version
docker --version

# Check Docker Compose version
docker compose version

# Check Docker info
docker info

# List running containers
docker ps

# Test with hello-world
docker run hello-world
```

## Next Steps

Once Docker is installed and verified:

1. **Navigate to your project directory**:
   ```bash
   cd "C:\Users\LITA W\Documents\DANIEL\Project\Java\WegoFlight"
   ```

2. **Build and run the application**:
   ```bash
   docker-compose up --build
   ```

3. **Access the API**:
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html

## Alternative: Using Chocolatey (Package Manager)

If you have Chocolatey installed, you can install Docker Desktop with:

```powershell
# Install Chocolatey first (if not installed)
# Visit: https://chocolatey.org/install

# Install Docker Desktop
choco install docker-desktop -y
```

## System Requirements

- **RAM**: Minimum 4GB (8GB recommended)
- **Disk Space**: At least 4GB free
- **CPU**: 64-bit processor with virtualization support
- **Windows**: Windows 10/11 64-bit

## Additional Resources

- **Docker Desktop Documentation**: https://docs.docker.com/desktop/windows/
- **WSL 2 Documentation**: https://docs.microsoft.com/en-us/windows/wsl/
- **Docker Troubleshooting**: https://docs.docker.com/desktop/troubleshoot/

## Need Help?

If you encounter issues:
1. Check the Docker Desktop logs: Settings → Troubleshoot → View logs
2. Check Windows Event Viewer for errors
3. Visit Docker Desktop forums: https://forums.docker.com/

---

**Note**: Docker Desktop includes both Docker Engine and Docker Compose, so you only need to install Docker Desktop to get both tools.

