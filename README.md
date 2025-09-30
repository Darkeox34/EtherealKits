![HeavenKits Logo](https://i.imgur.com/DXxOYSH.png)

# HeavenKits

A powerful and feature-rich Minecraft plugin for managing custom kits with flexible storage (MongoDB or local files), cooldown systems, and an intuitive GUI interface.

## Features

- **Custom Kit Management**: Create, edit, and delete custom kits with ease
- **MongoDB Integration**: Persistent data storage using MongoDB database
- **Local Storage Support**: File-based storage when MongoDB is disabled
- **Cooldown System**: Configurable cooldowns for each kit
- **Permission-Based Access**: Fine-grained permission control for different kits
- **Interactive GUI**: User-friendly graphical interface for kit management
- **Admin Tools**: Comprehensive admin commands for kit administration
- **Real-time Updates**: Automatic cooldown cleanup and data synchronization

## Requirements

- **Minecraft Server**: Paper/Spigot 1.21+
- **Java**: Java 21 or higher
- **Database**: MongoDB instance (optional)

## Installation

1. Download the latest `HeavenKits-1.0.jar` from the releases
2. Place the JAR file in your server's `plugins` folder
3. Start your server to generate the configuration files
4. Configure storage in `config.yml` (MongoDB on/off and connection details)
5. Restart the server

## Configuration

### config.yml

```yaml
mongodb:
  enabled: false        # If true, use MongoDB; if false, use local files
  host: localhost        # MongoDB host address
  port: 27017           # MongoDB port
  database: minecraft   # Database name
  username: ""          # MongoDB username (leave empty if no auth)
  password: ""          # MongoDB password (leave empty if no auth)
```

Storage behavior:
- When `mongodb.enabled` is `true`, all data is stored exclusively in MongoDB.
- When `mongodb.enabled` is `false` (default), data is stored locally under `plugins/HeavenKits/data/`:
  - Kits in `plugins/HeavenKits/data/kits/*.json`
  - Player cooldowns in `plugins/HeavenKits/data/cooldowns.json`

## Commands

### Main Command
- `/hk` or `/heavenkits` - Main plugin command

### Subcommands

#### For Players
- `/hk kits` - Open the kits GUI menu

#### For Administrators
- `/hk admin help` - Display admin help menu
- `/hk admin create <kit_name>` - Create a new kit
- `/hk admin delete <kit_name>` - Delete an existing kit
- `/hk admin edit <kit_name>` - Edit kit properties
- `/hk admin list` - List all available kits
- `/hk admin rename <old_name> <new_name>` - Rename a kit

## Permissions

### Basic Permissions
- `heavenkits.hk` - Access to main command (default: op)
- `heavenkits.admin` - Access to admin commands (default: op)
- `heavenkits.kits` - Access to kits menu (default: true)

### Kit-Specific Permissions
Each kit can have a custom permission requirement set by administrators. If no permission is set, the kit defaults to "None" (accessible to all players).

## Usage Guide

### For Players

1. **Accessing Kits**
   - Use `/hk kits` to open the kits menu
   - Click on any available kit to redeem it
   - Kits with active cooldowns will display remaining time

2. **Kit Cooldowns**
   - Each kit has a configurable cooldown period
   - Cooldowns are tracked per player and persist across server restarts
   - Remaining cooldown time is displayed in the GUI

### For Administrators

1. **Creating Kits**
   ```
   /hk admin create MyKit
   ```
   - Creates a new empty kit
   - Use the GUI to add items and configure properties

2. **Editing Kits**
   ```
   /hk admin edit MyKit
   ```
   - Opens the kit editor GUI
   - Configure display name, icon, cooldown, and permissions
   - Add/remove items with custom enchantments

3. **Managing Items**
  - Items are added to kits through the GUI interface
  - Support for custom enchantments and item metadata
  - Items are automatically serialized and stored in the selected storage (MongoDB or local files)

## Technical Details

### Storage Details

The plugin supports two storage backends:

- MongoDB (when enabled):
  - Collections:
    - `kits`: Kit definitions with items, cooldowns, and permissions
    - `cooldowns`: Player-specific cooldown tracking

- Local files (default):
  - Files:
    - `data/kits/*.json`: One JSON file per kit
    - `data/cooldowns.json`: Player cooldowns JSON document

### Performance Features

- **Asynchronous Operations**: Database operations run asynchronously
- **Cooldown Cleanup**: Automatic cleanup of expired cooldowns every hour
- **Concurrent Safety**: Thread-safe cooldown management
- **Efficient Serialization**: Optimized item and kit serialization

## Development

### Building from Source

1. Clone the repository
2. Ensure Java 21+ is installed
3. Run `./gradlew shadowJar`
4. Find the compiled JAR in `build/libs/`

### Dependencies

- **Paper API**: 1.21.4-R0.1-SNAPSHOT
- **MongoDB Driver**: 4.11.0
- **Adventure API**: Included with Paper

## Support

For issues, feature requests, or contributions, please contact the development team at EtherealLabs.

## License

This project is licensed under the terms specified in the LICENSE file.

---

**Version**: 1.0  
**Author**: EtherealLabs  
**Minecraft Version**: 1.21+  
**Last Updated**: 2025-30-09