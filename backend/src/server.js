import app from "./app.js";

const PORT = process.env.PORT || 5000;

app.listen(PORT, () => {
  console.log(`🚀 Server running on port ${PORT}`);
  console.log(`📝 Swagger UI: http://localhost:${PORT}/api/docs`);
  console.log(`🧾 OpenAPI JSON: http://localhost:${PORT}/api/docs-json`);
  console.log(`🔐 Auth endpoints: http://localhost:${PORT}/api/auth`);
});
