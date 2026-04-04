require('dotenv').config();
const fs = require('fs');
const path = require('path');

// Read config.json
const configPath = path.join(__dirname, 'config', 'config.json');
const config = require(configPath);

// Update password from .env
config.development.password = process.env.DB_PASSWORD || '';
config.test.password = process.env.DB_PASSWORD || '';

// Write back to config.json
fs.writeFileSync(configPath, JSON.stringify(config, null, 2));

console.log('✅ Config updated with password from .env');
console.log('⚠️  Remember: This is temporary for migrations only');
console.log('🔒 Never commit config.json with real passwords!');
