import './src/config/dotenv.js';
import sequelize from './src/config/database.js';
import fs from 'fs';

const setupChatTables = async () => {
  try {
    console.log('🔧 Setting up chat tables...');
    
    const sql = fs.readFileSync('./database/chat_tables.sql', 'utf8');
    
    // Split by semicolon and execute each statement
    const statements = sql.split(';').filter(stmt => stmt.trim());
    
    for (const statement of statements) {
      if (statement.trim()) {
        await sequelize.query(statement);
        console.log('✅ Executed:', statement.substring(0, 50) + '...');
      }
    }
    
    console.log('✅ Chat tables created successfully!');
    process.exit(0);
  } catch (error) {
    console.error('❌ Error setting up chat tables:', error);
    process.exit(1);
  }
};

setupChatTables();
