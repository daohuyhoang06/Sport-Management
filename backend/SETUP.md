# Setup Instructions

## 1. Clone repository

```bash
git clone <repository-url>
cd INT_3306_1/backend
```

## 2. Install dependencies

```bash
npm install
```

## 3. Setup environment variables

Copy `.env.example` to `.env`:

```bash
# Windows
copy .env.example .env

# Linux/Mac
cp .env.example .env
```

Then edit `.env` and update your MySQL password:

```env
DB_PASSWORD=your_mysql_password_here
```

## 4. Create database

Open MySQL and create database:

```sql
CREATE DATABASE sport_management;
```

## 5. Run migrations

```bash
npm run db:migrate
```

## 6. Start server

```bash
npm run dev
```

## Important Notes

- ⚠️ **NEVER** commit `.env` file to git (it's already in `.gitignore`)
- ⚠️ **NEVER** put real passwords in `config.json` or any committed files
- ✅ **ALWAYS** use `.env` file for sensitive information
- ✅ Update `.env.example` when adding new environment variables (without real values)

## Security Checklist

- [ ] `.env` is in `.gitignore`
- [ ] `config.json` has no real passwords
- [ ] All team members have their own `.env` file
- [ ] Production uses different credentials
