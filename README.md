⚡ LightEmAll
A puzzle game where you connect wires to power up an entire electrical grid! Rotate tiles and move the power station to light up every wire in the network.
🎮 How to Play
Your goal is to illuminate every tile on the board by creating a continuous electrical connection from the power station to all wires.
🖱️ Controls

Mouse Click - Rotate any tile clockwise to change wire connections
Arrow Keys - Move the power station through connected wires

⬆️ Up - Move power station up
⬇️ Down - Move power station down
⬅️ Left - Move power station left
➡️ Right - Move power station right


🎯 Game Rules
The power station (⭐) can only move through connected wires
Wires light up yellow when powered, stay gray when not
You must create one continuous network connecting all tiles
Win by powering every single tile on the board!

✨ Features
Procedural Generation - Each game creates a unique puzzle using Kruskal's algorithm
Smart Grid System - Uses Depth-First Search to determine which tiles receive power
Visual Feedback - Clear distinction between powered (yellow) and unpowered (gray) wires
Multiple Board Sizes - Play on different grid dimensions
Winning Animation - Celebration message when you complete the puzzle

🛠️ Technical Details
Built with Java using several computer science concepts:

Minimum Spanning Tree - Kruskal's algorithm generates connected puzzle layouts
Union-Find Data Structure - Efficiently tracks tile connections during generation
Depth-First Search - Propagates power through the wire network
Graph Theory - Models the electrical grid as a connected graph

🎲 Game Generation
The puzzle creates a random but solvable board by:

Building a Complete Grid - Start with all possible tile connections
Creating Minimum Spanning Tree - Use Kruskal's algorithm to ensure connectivity
Random Rotation - Scramble tiles to create the puzzle challenge
Power Station Placement - Position the starting power source

📁 Project Structure
LightEmAll.java
├── LightEmAll class      # Main game logic and world state
├── GamePiece class      # Individual tile representation
├── Edge class           # Connection between tiles
├── UnionFind class      # Disjoint set data structure
└── Tests class          # Comprehensive unit testing
🚀 Running the Game
The project includes multiple game configurations:

Small Grid (3x3) - Quick puzzle for testing
Medium Grid (4x4) - Balanced challenge
Large Grid (8x9) - Complex puzzle for advanced players

Simply run any of the example classes to start playing!
🧪 Testing
Includes thorough unit tests covering:
Board generation algorithms
Tile rotation mechanics
Power propagation logic
User interaction handling
Win condition detection
