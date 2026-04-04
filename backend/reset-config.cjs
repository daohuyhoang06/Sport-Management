const fs = require('fs');
const path = require('path');

// Read config.json
const configPath = path.join(__dirname, 'config', 'config.json');
const config = require(configPath);

// Reset passwords to null
config.development.password = null;
config.test.password = null;
config.production.password = null;

// Write back to config.json
fs.writeFileSync(configPath, JSON.stringify(config, null, 2));

console.log('✅ Config passwords reset to null');
console.log('🔒 Safe to commit config.json now!');
