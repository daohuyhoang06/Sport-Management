import app from "./app.js";

const PORT = process.env.PORT || 5000;
const HOST = process.env.HOST || "0.0.0.0";

app.listen(PORT, HOST, () => {
  const docsHost = HOST === "0.0.0.0" ? "localhost" : HOST;

  console.log(`Server running on ${HOST}:${PORT}`);
  console.log(`Swagger UI: http://${docsHost}:${PORT}/api/docs`);
  console.log(`OpenAPI JSON: http://${docsHost}:${PORT}/api/docs-json`);
  console.log(`Auth endpoints: http://${docsHost}:${PORT}/api/auth`);
});
