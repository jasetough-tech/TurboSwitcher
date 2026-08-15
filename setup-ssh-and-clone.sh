#!/bin/bash
# TurboSwitcher - SSH Setup & Git Clone Script for Termux
# This script sets up SSH authentication and clones the repository
# Usage: chmod +x setup-ssh-and-clone.sh && ./setup-ssh-and-clone.sh

set -e

echo "═══════════════════════════════════════════════════════════════════════════════"
echo "  TurboSwitcher - SSH Setup & Clone Script"
echo "═══════════════════════════════════════════════════════════════════════════════"
echo ""

# Step 1: Create .ssh directory if it doesn't exist
echo "📁 Setting up SSH directory..."
mkdir -p ~/.ssh
chmod 700 ~/.ssh
echo "✅ SSH directory ready"
echo ""

# Step 2: Check if key already exists
if [ -f ~/.ssh/id_ed25519 ]; then
    echo "🔑 SSH key already exists at ~/.ssh/id_ed25519"
    echo "   Using existing key..."
    echo ""
else
    echo "🔑 Generating new SSH key (Ed25519)..."
    ssh-keygen -t ed25519 -f ~/.ssh/id_ed25519 -N "" -C "turboswitcher@oppo-a5x"
    echo "✅ SSH key generated"
    echo ""
fi

# Step 3: Display public key
echo "═══════════════════════════════════════════════════════════════════════════════"
echo "  YOUR PUBLIC KEY (COPY THIS)"
echo "═══════════════════════════════════════════════════════════════════════════════"
echo ""
cat ~/.ssh/id_ed25519.pub
echo ""
echo "═══════════════════════════════════════════════════════════════════════════════"
echo ""

# Step 4: Instructions for adding key to GitHub
echo "⚠️  IMPORTANT: Add this key to GitHub"
echo ""
echo "1. Open browser on your phone"
echo "2. Go to: https://github.com/settings/keys"
echo "3. Click 'New SSH key'"
echo "4. Title: 'Oppo A5x Termux'"
echo "5. Paste the public key above"
echo "6. Click 'Add SSH key'"
echo ""
read -p "Have you added the key to GitHub? (y/n): " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "❌ Please add the SSH key to GitHub first."
    echo "   Instructions: https://github.com/settings/keys"
    exit 1
fi

echo ""
echo "═══════════════════════════════════════════════════════════════════════════════"
echo "  CLONING REPOSITORY"
echo "═══════════════════════════════════════════════════════════════════════════════"
echo ""

# Step 5: Test SSH connection
echo "🔗 Testing SSH connection to GitHub..."
if ssh -T git@github.com 2>&1 | grep -q "successfully authenticated"; then
    echo "✅ SSH connection successful"
else
    echo "⚠️  Attempting connection (this may take a moment)..."
    ssh -T git@github.com || true
fi
echo ""

# Step 6: Clone repository
echo "📥 Cloning TurboSwitcher repository..."
cd ~

if [ -d "TurboSwitcher" ]; then
    echo "   Removing old repository..."
    rm -rf TurboSwitcher
fi

if git clone git@github.com:jasetough-tech/TurboSwitcher.git; then
    echo "✅ Repository cloned successfully"
    cd TurboSwitcher
    
    echo ""
    echo "🔀 Checking out feature branch..."
    if git checkout feature/turboswitcher-app; then
        echo "✅ Checked out feature/turboswitcher-app"
    else
        echo "⚠️  Couldn't checkout feature branch, staying on main"
    fi
else
    echo "❌ Failed to clone repository"
    echo "   Check that the SSH key was added to GitHub"
    exit 1
fi

echo ""
echo "═══════════════════════════════════════════════════════════════════════════════"
echo "  ✅ ALL SETUP COMPLETE!"
echo "═══════════════════════════════════════════════════════════════════════════════"
echo ""
echo "📂 Repository location: ~/TurboSwitcher"
echo ""
echo "Next steps:"
echo "  cd ~/TurboSwitcher"
echo "  chmod +x build-and-install.sh"
echo "  ./build-and-install.sh"
echo ""
echo "═══════════════════════════════════════════════════════════════════════════════"
echo ""
