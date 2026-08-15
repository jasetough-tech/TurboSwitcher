# TurboSwitcher - Oppo A5x Termux Setup

## 🚀 One-Line Setup

If you already have the repository cloned, just run:

```bash
cd ~/TurboSwitcher
chmod +x setup-ssh-and-clone.sh
./setup-ssh-and-clone.sh
```

## 🔑 What This Script Does

1. **Creates SSH directory** (~/.ssh)
2. **Generates SSH key** (Ed25519 - secure & fast)
3. **Shows your public key** - you'll copy this
4. **Prompts you to add it to GitHub** - easy steps
5. **Tests SSH connection** - verifies it works
6. **Clones the repository** - git@github.com (SSH instead of HTTPS)
7. **Checks out the correct branch** - feature/turboswitcher-app

## ✅ Quick Start

### Step 1: Run SSH Setup
```bash
chmod +x setup-ssh-and-clone.sh
./setup-ssh-and-clone.sh
```

The script will:
- Show your SSH public key
- Ask you to add it to GitHub
- Clone the repo automatically

### Step 2: Add Key to GitHub (While Script Waits)
1. Open **Firefox** or **Chrome** on your Oppo A5x
2. Go to: **https://github.com/settings/keys**
3. Click **"New SSH key"** (green button, top right)
4. For **Title**: Enter `"Oppo A5x Termux"`
5. For **Key**: Paste the key shown by the script
6. Click **"Add SSH key"**

### Step 3: Confirm in Termux
Press **Y** when script asks "Have you added the key to GitHub?"

The script will then clone your repo automatically!

## 🛠️ If You Already Have the Repo

If you already cloned it with HTTPS, just update the remote:

```bash
cd ~/TurboSwitcher
git remote remove origin
git remote add origin git@github.com:jasetough-tech/TurboSwitcher.git
git pull origin feature/turboswitcher-app
```

## 📋 Commands Reference

```bash
# View your SSH public key (to add to GitHub)
cat ~/.ssh/id_ed25519.pub

# Test SSH connection
ssh -T git@github.com

# Clone with SSH
git clone git@github.com:jasetough-tech/TurboSwitcher.git

# Update remote from HTTPS to SSH
git remote set-url origin git@github.com:jasetough-tech/TurboSwitcher.git
```

## ✨ Why SSH?

- ✅ No password needed
- ✅ More secure (key-based authentication)
- ✅ Faster than HTTPS
- ✅ Works offline
- ✅ Perfect for mobile development

## 🚨 Troubleshooting

### "Permission denied (publickey)"
- Make sure you added the SSH key to GitHub
- Check: https://github.com/settings/keys
- Wait a moment for GitHub to sync the key

### "Could not read from remote repository"
- Test: `ssh -T git@github.com`
- Should see: "Hi jasetough-tech! You've successfully authenticated..."

### "No such file or directory"
- Make sure you're in the right directory: `pwd`
- Should be: `/root/TurboSwitcher` or `~/TurboSwitcher`

---

**Need help?** Share the error message from the terminal!
