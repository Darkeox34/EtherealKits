# HeavenKits

A comprehensive kit management plugin for Minecraft servers using Paper/Spigot, featuring MongoDB integration and intuitive GUI management.

## Features

- **Kit Management**: Create, edit, and delete kits with custom items, enchantments, and properties
- **GUI Interface**: User-friendly inventory-based menus for both players and administrators
- **MongoDB Integration**: Persistent data storage with MongoDB database
- **Cooldown System**: Configurable cooldowns with automatic cleanup of expired entries
- **Permission System**: Granular permission control for kits and commands
- **Multi-language Support**: Italian language interface (easily customizable)
- **Real-time Updates**: Live cooldown display and automatic inventory updates

## Requirements

- **Server**: Paper 1.20+ (uses Paper-specific APIs)
- **Java**: Java 17+
- **Database**: MongoDB instance (local or remote)

## Installation

1. Download the plugin JAR file
2. Place it in your server's `plugins` folder
3. Configure MongoDB connection in `config.yml`
4. Restart your server
5. Configure permissions as needed

## Configuration

### config.yml
```yaml
mongodb:
  host: localhost
  port: 27017
  database: minecraft
  username: ""
  password: ""
```

**MongoDB Configuration Options:**
- `host`: MongoDB server hostname
- `port`: MongoDB server port (default: 27017)
- `database`: Database name to use
- `username`: MongoDB username (leave empty for no authentication)
- `password`: MongoDB password (leave empty for no authentication)

## Commands

### Main Command: `/hk`

#### Player Commands
- `/hk kits` - Opens the kits GUI menu

#### Admin Commands
- `/hk admin help` - Shows all admin commands
- `/hk admin list` - Lists all available kits
- `/hk admin create <name>` - Creates a new kit
- `/hk admin delete <name>` - Deletes an existing kit
- `/hk admin rename <old_name> <new_name>` - Renames a kit
- `/hk admin edit <name>` - Opens the kit editor GUI
- `/hk admin gui` - Opens the admin kits management GUI

## Permissions

### Basic Permissions
- `heavenkits.hk` - Access to main `/hk` command
- `hk.commands.kits` - Access to kits GUI
- `hk.commands.admin` - Access to admin commands
- `hk.cooldown.bypass` - Bypass all kit cooldowns

### Kit-Specific Permissions
Each kit can have a custom permission requirement. Players need the specific permission to redeem that kit.

## Kit Management

### Creating Kits
1. Use `/hk admin create <name>` or the GUI
2. Edit the kit using `/hk admin edit <name>` or the GUI
3. Configure kit properties:
   - **Display Material**: Icon shown in menus
   - **Display Name**: Formatted name with color codes
   - **Items**: Add items with custom names, lore, and enchantments
   - **Cooldown**: Time between redemptions
   - **Permission**: Required permission to access

### Kit Properties

#### Items
- **Material**: Base Minecraft item type
- **Quantity**: Stack size (1-64)
- **Custom Name**: MiniMessage formatted display name
- **Lore**: Multiple lines of item description
- **Enchantments**: Any enchantment with custom levels

#### Cooldowns
Cooldowns support flexible time formats:
- `7d12h` - 7 days and 12 hours
- `10m` - 10 minutes  
- `5h15m10s` - 5 hours, 15 minutes, 10 seconds

## GUI Features

### Player Kit Menu
- Visual kit display with materials and names
- Real-time cooldown countdown
- Permission-based access control
- Left-click to redeem, right-click to preview

### Admin Management
- **Kit Editor**: Comprehensive item management interface
- **Enchantment Menu**: Browse and apply all available enchantments
- **Chat Prompts**: In-game text input for names, values, and confirmations
- **Bulk Operations**: Manage multiple kits efficiently

### Interactive Elements
- **Click Actions**: Different mouse buttons for different actions
- **Tooltips**: Hover information for all interactive elements
- **Real-time Updates**: Live cooldown displays and inventory refresh
- **Smart Navigation**: Breadcrumb-style menu navigation

## Database Schema

### Kits Collection
```json
{
  "name": "kit_name",
  "display_name": "{\"text\":\"Kit Display Name\"}",
  "display_material": "DIAMOND_SWORD",
  "cooldown": 86400000,
  "permission": "kits.vip",
  "items": [
    {
      "material": "DIAMOND_SWORD",
      "name": "{\"text\":\"Epic Sword\"}",
      "qty": 1,
      "lore": ["{\"text\":\"A legendary weapon\"}"],
      "enchantments": {
        "minecraft:sharpness": 5,
        "minecraft:unbreaking": 3
      }
    }
  ]
}
```

### Cooldowns Collection
```json
{
  "_id": "cooldowns",
  "players": {
    "player-uuid": {
      "kit_name": 1640995200000
    }
  }
}
```

## Technical Details

### Architecture
- **Event-driven Design**: Bukkit event system for menu interactions
- **Async Operations**: Database operations run asynchronously
- **Memory Management**: Efficient cooldown tracking with automatic cleanup
- **Component System**: Adventure API for rich text formatting

### Key Components

#### KitManager
- Central management for all kit operations
- Handles cooldown logic and validation
- Manages database synchronization
- Inventory space validation before redemption

#### GUI System
- **BaseMenu**: Abstract menu framework
- **ChatPrompts**: In-game text input system
- **Event Handling**: Automatic listener management

#### Data Layer
- **MongoDB**: Primary data storage
- **KitSerializer**: Handles object serialization/deserialization
- **Async Processing**: Non-blocking database operations

### Performance Optimizations
- **Concurrent Collections**: Thread-safe cooldown management
- **Batch Operations**: Efficient database updates
- **Memory Cleanup**: Automatic removal of expired data
- **Lazy Loading**: On-demand data retrieval

## Development

### Building
1. Clone the repository
2. Ensure you have Java 17+ and Maven
3. Run `mvn clean package`
4. JAR file will be in the `target` directory

### Dependencies
- **Paper API**: Server framework
- **MongoDB Java Driver**: Database connectivity
- **Adventure API**: Text components and formatting

### Code Structure
```
src/main/java/gg/ethereallabs/heavenkits/
├── commands/           # Command handling
├── data/              # Database operations
├── events/            # Event listeners  
├── gui/               # Menu interfaces
├── kits/              # Kit management logic
└── HeavenKits.java    # Main plugin class
```

## Troubleshooting

### Common Issues

**Database Connection Failed**
- Verify MongoDB is running
- Check connection credentials in config.yml
- Ensure database exists and is accessible

**Kit Not Appearing**
- Check player has required permission
- Verify kit was saved correctly
- Restart server to reload kits from database

**Inventory Full Error**
- Player needs sufficient empty inventory slots
- Kit items require individual slots (stacking not automatic)
- Clear inventory space before redemption

**Cooldown Not Working**
- Verify cooldown format is correct (e.g., `7d12h`)
- Check if player has bypass permission
- Database may need time to sync changes

### Debug Information
Enable debug logging by checking server logs for HeavenKits entries. MongoDB connection status and kit loading information is logged on startup.

## Support

For issues, suggestions, or contributions:
- Create issues on the GitHub repository
- Check existing documentation and troubleshooting guides
- Provide server version, plugin version, and error logs when reporting issues

## License

This project is licensed under the terms specified in the repository license file.
