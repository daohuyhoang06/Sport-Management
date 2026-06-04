import sequelize from "./src/config/database.js";

const MOMO_MIGRATION = "20260509000001-add-momo-fields-to-payments.cjs";

const columnDefinitions = [
  ["payment_status", "VARCHAR(45) NULL DEFAULT 'pending' AFTER payment_method"],
  ["transaction_id", "VARCHAR(100) NULL AFTER payment_status"],
  ["paid_at", "DATETIME NULL AFTER transaction_id"],
  ["provider", "VARCHAR(45) NULL AFTER payment_method"],
  ["order_id", "VARCHAR(200) NULL AFTER provider"],
  ["request_id", "VARCHAR(200) NULL AFTER order_id"],
  ["pay_url", "TEXT NULL AFTER request_id"],
  ["deeplink", "TEXT NULL AFTER pay_url"],
  ["qr_code_url", "TEXT NULL AFTER deeplink"],
  ["raw_response", "TEXT NULL AFTER qr_code_url"],
  ["failure_reason", "TEXT NULL AFTER raw_response"],
];

const indexes = [
  ["idx_payments_provider", "provider"],
  ["idx_payments_order_id", "order_id"],
  ["idx_payments_request_id", "request_id"],
];

const getPaymentColumns = async () => {
  const [columns] = await sequelize.query("SHOW COLUMNS FROM payments");
  return new Set(columns.map((column) => column.Field));
};

const indexExists = async (indexName) => {
  const [rows] = await sequelize.query(
    "SHOW INDEX FROM payments WHERE Key_name = ?",
    { replacements: [indexName] },
  );
  return rows.length > 0;
};

const ensureMomoPaymentColumns = async () => {
  try {
    const columns = await getPaymentColumns();

    for (const [name, definition] of columnDefinitions) {
      if (!columns.has(name)) {
        console.log(`Adding payments.${name}...`);
        await sequelize.query(`ALTER TABLE payments ADD COLUMN ${name} ${definition}`);
      } else {
        console.log(`payments.${name} already exists`);
      }
    }

    for (const [indexName, column] of indexes) {
      if (!(await indexExists(indexName))) {
        console.log(`Adding ${indexName}...`);
        await sequelize.query(
          `CREATE INDEX ${indexName} ON payments (${column})`,
        );
      } else {
        console.log(`${indexName} already exists`);
      }
    }

    await sequelize.query(
      `INSERT INTO SequelizeMeta (name)
       VALUES (?)
       ON DUPLICATE KEY UPDATE name = name`,
      { replacements: [MOMO_MIGRATION] },
    );

    console.log("MoMo payment columns are ready.");
  } catch (error) {
    console.error("Failed to prepare payments table:", error.message);
    process.exitCode = 1;
  } finally {
    await sequelize.close();
  }
};

ensureMomoPaymentColumns();
