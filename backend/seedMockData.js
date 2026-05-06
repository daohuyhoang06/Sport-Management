import sequelize from "./src/config/database.js";
import bcrypt from "bcrypt";

const MOCK_TAG = "[MOCK]";

const addDays = (baseDate, days) => {
  const d = new Date(baseDate);
  d.setDate(d.getDate() + days);
  return d;
};

const toDateOnly = (date) => {
  const d = new Date(date);
  return d.toISOString().slice(0, 10);
};

const makeDateTime = (dateOnly, hh, mm = 0) => {
  const d = new Date(`${dateOnly}T00:00:00`);
  d.setHours(hh, mm, 0, 0);
  return d;
};

const quoteIdent = (dialect, ident) => {
  if (dialect === "postgres") {
    return `"${ident}"`;
  }
  return `\`${ident}\``;
};

const getOne = async (query, replacements) => {
  const [rows] = await sequelize.query(query, { replacements });
  return rows[0] || null;
};

const upsertUser = async ({
  username,
  name,
  role,
  email,
  phone,
  password,
  personNameColumn,
}) => {
  const existing = await getOne(
    "SELECT person_id FROM person WHERE username = :username LIMIT 1",
    { username },
  );
  const hashedPassword = await bcrypt.hash(password, 10);

  if (!existing) {
    await sequelize.query(
      `
        INSERT INTO person (${personNameColumn}, username, password, email, phone, role, status)
        VALUES (:name, :username, :password, :email, :phone, :role, 'active')
      `,
      {
        replacements: {
          name,
          username,
          password: hashedPassword,
          email,
          phone,
          role,
        },
      },
    );
  } else {
    await sequelize.query(
      `
        UPDATE person
        SET ${personNameColumn} = :name,
            password = :password,
            email = :email,
            phone = :phone,
            role = :role,
            status = 'active'
        WHERE username = :username
      `,
      {
        replacements: {
          name,
          username,
          password: hashedPassword,
          email,
          phone,
          role,
        },
      },
    );
  }

  return getOne(
    "SELECT person_id, username, role FROM person WHERE username = :username LIMIT 1",
    { username },
  );
};

const ensureField = async ({
  field_name,
  manager_id,
  location,
  rentalPriceColumn,
  rental_price,
}) => {
  const existing = await getOne(
    "SELECT field_id FROM fields WHERE field_name = :field_name LIMIT 1",
    { field_name },
  );

  if (!existing) {
    if (rentalPriceColumn) {
      await sequelize.query(
        `
          INSERT INTO fields (manager_id, field_name, location, status, ${rentalPriceColumn})
          VALUES (:manager_id, :field_name, :location, 'active', :rental_price)
        `,
        { replacements: { field_name, manager_id, location, rental_price } },
      );
    } else {
      await sequelize.query(
        `
          INSERT INTO fields (manager_id, field_name, location, status)
          VALUES (:manager_id, :field_name, :location, 'active')
        `,
        { replacements: { field_name, manager_id, location } },
      );
    }
  } else if (rentalPriceColumn) {
    await sequelize.query(
      `
        UPDATE fields
        SET manager_id = :manager_id,
            location = :location,
            status = 'active',
            ${rentalPriceColumn} = :rental_price
        WHERE field_id = :field_id
      `,
      {
        replacements: {
          field_id: existing.field_id,
          manager_id,
          location,
          rental_price,
        },
      },
    );
  } else {
    await sequelize.query(
      `
        UPDATE fields
        SET manager_id = :manager_id,
            location = :location,
            status = 'active'
        WHERE field_id = :field_id
      `,
      {
        replacements: {
          field_id: existing.field_id,
          manager_id,
          location,
        },
      },
    );
  }

  return getOne(
    "SELECT field_id, manager_id, field_name FROM fields WHERE field_name = :field_name LIMIT 1",
    { field_name },
  );
};

const ensureSchedule = async ({
  field_id,
  start_time,
  end_time,
  price,
  scheduleColumns,
}) => {
  const existing = await getOne(
    `
      SELECT schedule_id
      FROM field_schedules
      WHERE field_id = :field_id
        AND start_time = :start_time
        AND end_time = :end_time
      LIMIT 1
    `,
    { field_id, start_time, end_time },
  );

  if (!existing) {
    if (scheduleColumns.is_available) {
      await sequelize.query(
        `
          INSERT INTO field_schedules (field_id, start_time, end_time, price, is_available)
          VALUES (:field_id, :start_time, :end_time, :price, 1)
        `,
        { replacements: { field_id, start_time, end_time, price } },
      );
    } else {
      await sequelize.query(
        `
          INSERT INTO field_schedules (field_id, start_time, end_time, price)
          VALUES (:field_id, :start_time, :end_time, :price)
        `,
        { replacements: { field_id, start_time, end_time, price } },
      );
    }
    return;
  }

  if (scheduleColumns.is_available) {
    await sequelize.query(
      `
        UPDATE field_schedules
        SET price = :price,
            is_available = 1
        WHERE schedule_id = :schedule_id
      `,
      { replacements: { schedule_id: existing.schedule_id, price } },
    );
    return;
  }

  await sequelize.query(
    `
      UPDATE field_schedules
      SET price = :price
      WHERE schedule_id = :schedule_id
    `,
    { replacements: { schedule_id: existing.schedule_id, price } },
  );
};

const ensureBooking = async ({
  customer_id,
  field_id,
  schedule_id,
  start_time,
  end_time,
  status,
  price,
  note,
  bookingColumns,
}) => {
  const existing = await getOne(
    "SELECT booking_id FROM bookings WHERE note = :note LIMIT 1",
    { note },
  );

  if (!existing) {
    if (bookingColumns.schedule_id) {
      await sequelize.query(
        `
          INSERT INTO bookings (customer_id, field_id, schedule_id, start_time, end_time, status, note, price)
          VALUES (:customer_id, :field_id, :schedule_id, :start_time, :end_time, :status, :note, :price)
        `,
        {
          replacements: {
            customer_id,
            field_id,
            schedule_id,
            start_time,
            end_time,
            status,
            note,
            price,
          },
        },
      );
    } else {
      await sequelize.query(
        `
          INSERT INTO bookings (customer_id, field_id, start_time, end_time, status, note, price)
          VALUES (:customer_id, :field_id, :start_time, :end_time, :status, :note, :price)
        `,
        {
          replacements: {
            customer_id,
            field_id,
            start_time,
            end_time,
            status,
            note,
            price,
          },
        },
      );
    }
  } else if (bookingColumns.schedule_id) {
    await sequelize.query(
      `
        UPDATE bookings
        SET customer_id = :customer_id,
            field_id = :field_id,
            schedule_id = :schedule_id,
            start_time = :start_time,
            end_time = :end_time,
            status = :status,
            price = :price
        WHERE booking_id = :booking_id
      `,
      {
        replacements: {
          booking_id: existing.booking_id,
          customer_id,
          field_id,
          schedule_id,
          start_time,
          end_time,
          status,
          price,
        },
      },
    );
  } else {
    await sequelize.query(
      `
        UPDATE bookings
        SET customer_id = :customer_id,
            field_id = :field_id,
            start_time = :start_time,
            end_time = :end_time,
            status = :status,
            price = :price
        WHERE booking_id = :booking_id
      `,
      {
        replacements: {
          booking_id: existing.booking_id,
          customer_id,
          field_id,
          start_time,
          end_time,
          status,
          price,
        },
      },
    );
  }

  return getOne("SELECT booking_id FROM bookings WHERE note = :note LIMIT 1", {
    note,
  });
};

const ensureReview = async ({
  customer_id,
  field_id,
  rating,
  comment,
  reviewColumns,
}) => {
  const existing = await getOne(
    "SELECT review_id FROM reviews WHERE comment = :comment LIMIT 1",
    { comment },
  );

  if (!existing) {
    if (reviewColumns.created_at) {
      await sequelize.query(
        `
          INSERT INTO reviews (customer_id, field_id, rating, comment, created_at)
          VALUES (:customer_id, :field_id, :rating, :comment, NOW())
        `,
        { replacements: { customer_id, field_id, rating, comment } },
      );
    } else {
      await sequelize.query(
        `
          INSERT INTO reviews (customer_id, field_id, rating, comment)
          VALUES (:customer_id, :field_id, :rating, :comment)
        `,
        { replacements: { customer_id, field_id, rating, comment } },
      );
    }
    return;
  }

  await sequelize.query(
    `
      UPDATE reviews
      SET customer_id = :customer_id,
          field_id = :field_id,
          rating = :rating
      WHERE review_id = :review_id
    `,
    {
      replacements: {
        review_id: existing.review_id,
        customer_id,
        field_id,
        rating,
      },
    },
  );
};

const seedMockData = async () => {
  try {
    await sequelize.authenticate();
    console.log("Database connected.");

    const dialect = sequelize.getDialect();
    const qi = sequelize.getQueryInterface();
    const personColumns = await qi.describeTable("person");
    const fieldColumns = await qi.describeTable("fields");
    const scheduleColumns = await qi.describeTable("field_schedules");
    const bookingColumns = await qi.describeTable("bookings");
    const reviewColumns = await qi.describeTable("reviews");

    const personNameColumn = personColumns.name ? "name" : "person_name";
    const rentalPriceColumn = fieldColumns.rental_price
      ? "rental_price"
      : fieldColumns.price
        ? "price"
        : null;

    const safePersonNameColumn = quoteIdent(dialect, personNameColumn);
    const safeRentalPriceColumn = rentalPriceColumn
      ? quoteIdent(dialect, rentalPriceColumn)
      : null;

    const admin = await upsertUser({
      username: "admin01",
      name: "Mock Admin",
      role: "admin",
      email: "admin01.mock@sport.local",
      phone: "0901000001",
      password: "123456",
      personNameColumn: safePersonNameColumn,
    });

    const manager1 = await upsertUser({
      username: "manager01",
      name: "Mock Manager 01",
      role: "manager",
      email: "manager01.mock@sport.local",
      phone: "0901000002",
      password: "123456",
      personNameColumn: safePersonNameColumn,
    });

    const manager2 = await upsertUser({
      username: "manager02",
      name: "Mock Manager 02",
      role: "manager",
      email: "manager02.mock@sport.local",
      phone: "0901000003",
      password: "123456",
      personNameColumn: safePersonNameColumn,
    });

    const user1 = await upsertUser({
      username: "user01",
      name: "Mock User 01",
      role: "user",
      email: "user01.mock@sport.local",
      phone: "0901000004",
      password: "123456",
      personNameColumn: safePersonNameColumn,
    });

    const user2 = await upsertUser({
      username: "user02",
      name: "Mock User 02",
      role: "user",
      email: "user02.mock@sport.local",
      phone: "0901000005",
      password: "123456",
      personNameColumn: safePersonNameColumn,
    });

    const user3 = await upsertUser({
      username: "user03",
      name: "Mock User 03",
      role: "user",
      email: "user03.mock@sport.local",
      phone: "0901000006",
      password: "123456",
      personNameColumn: safePersonNameColumn,
    });

    const field1 = await ensureField({
      field_name: `${MOCK_TAG} San A`,
      manager_id: manager1.person_id,
      location: "Cau Giay, Ha Noi",
      rentalPriceColumn: safeRentalPriceColumn,
      rental_price: 320000,
    });

    const field2 = await ensureField({
      field_name: `${MOCK_TAG} San B`,
      manager_id: manager2.person_id,
      location: "Nam Tu Liem, Ha Noi",
      rentalPriceColumn: safeRentalPriceColumn,
      rental_price: 380000,
    });

    const today = toDateOnly(new Date());
    const tomorrow = toDateOnly(addDays(new Date(), 1));

    const schedule1Start = makeDateTime(today, 9, 0);
    const schedule1End = makeDateTime(today, 11, 0);
    const schedule2Start = makeDateTime(tomorrow, 17, 0);
    const schedule2End = makeDateTime(tomorrow, 19, 0);
    const schedule3Start = makeDateTime(today, 19, 0);
    const schedule3End = makeDateTime(today, 21, 0);

    await ensureSchedule({
      field_id: field1.field_id,
      start_time: schedule1Start,
      end_time: schedule1End,
      price: 320000,
      scheduleColumns,
    });
    await ensureSchedule({
      field_id: field1.field_id,
      start_time: schedule2Start,
      end_time: schedule2End,
      price: 360000,
      scheduleColumns,
    });
    await ensureSchedule({
      field_id: field2.field_id,
      start_time: schedule3Start,
      end_time: schedule3End,
      price: 380000,
      scheduleColumns,
    });

    const schedule1 = await getOne(
      `
        SELECT schedule_id
        FROM field_schedules
        WHERE field_id = :field_id
          AND start_time = :start_time
          AND end_time = :end_time
        LIMIT 1
      `,
      {
        field_id: field1.field_id,
        start_time: schedule1Start,
        end_time: schedule1End,
      },
    );

    const schedule2 = await getOne(
      `
        SELECT schedule_id
        FROM field_schedules
        WHERE field_id = :field_id
          AND start_time = :start_time
          AND end_time = :end_time
        LIMIT 1
      `,
      {
        field_id: field2.field_id,
        start_time: schedule3Start,
        end_time: schedule3End,
      },
    );

    const schedule3 = await getOne(
      `
        SELECT schedule_id
        FROM field_schedules
        WHERE field_id = :field_id
          AND start_time = :start_time
          AND end_time = :end_time
        LIMIT 1
      `,
      {
        field_id: field1.field_id,
        start_time: schedule2Start,
        end_time: schedule2End,
      },
    );

    const booking1 = await ensureBooking({
      customer_id: user1.person_id,
      field_id: field1.field_id,
      schedule_id: schedule1?.schedule_id || null,
      start_time: schedule1Start,
      end_time: schedule1End,
      status: "completed",
      price: 320000,
      note: `${MOCK_TAG} completed booking #1`,
      bookingColumns,
    });

    await ensureBooking({
      customer_id: user2.person_id,
      field_id: field2.field_id,
      schedule_id: schedule2?.schedule_id || null,
      start_time: schedule3Start,
      end_time: schedule3End,
      status: "pending",
      price: 380000,
      note: `${MOCK_TAG} pending booking #2`,
      bookingColumns,
    });

    await ensureBooking({
      customer_id: user3.person_id,
      field_id: field1.field_id,
      schedule_id: schedule3?.schedule_id || null,
      start_time: schedule2Start,
      end_time: schedule2End,
      status: "approved",
      price: 360000,
      note: `${MOCK_TAG} approved booking #3`,
      bookingColumns,
    });

    await ensureReview({
      customer_id: user1.person_id,
      field_id: field1.field_id,
      rating: 5,
      comment: `${MOCK_TAG} San dep, dich vu tot`,
      reviewColumns,
    });

    await ensureReview({
      customer_id: user2.person_id,
      field_id: field2.field_id,
      rating: 4,
      comment: `${MOCK_TAG} Dat san de, gia hop ly`,
      reviewColumns,
    });

    console.log("\nMock data seeded successfully.");
    console.log("Login accounts (all password = 123456):");
    console.log(`- admin01 (role: admin, id: ${admin.person_id})`);
    console.log(
      `- manager01, manager02 (role: manager, ids: ${manager1.person_id}, ${manager2.person_id})`,
    );
    console.log(
      `- user01, user02, user03 (role: user, ids: ${user1.person_id}, ${user2.person_id}, ${user3.person_id})`,
    );
    console.log(
      `- Fields: ${field1.field_name} (id ${field1.field_id}), ${field2.field_name} (id ${field2.field_id})`,
    );
    console.log(`- Sample booking id: ${booking1?.booking_id || "N/A"}`);
  } catch (error) {
    console.error("Failed to seed mock data:", error.message);
    console.error(error);
    process.exitCode = 1;
  } finally {
    await sequelize.close();
  }
};

seedMockData();
