import crypto from "crypto";
import fetch from "node-fetch";

const DEFAULT_CREATE_ENDPOINT =
  "https://test-payment.momo.vn/v2/gateway/api/create";

const getMomoConfig = () => ({
  endpoint: process.env.MOMO_ENDPOINT || DEFAULT_CREATE_ENDPOINT,
  partnerCode: process.env.MOMO_PARTNER_CODE,
  accessKey: process.env.MOMO_ACCESS_KEY,
  secretKey: process.env.MOMO_SECRET_KEY,
  requestType: process.env.MOMO_REQUEST_TYPE || "captureWallet",
  redirectUrl:
    process.env.MOMO_REDIRECT_URL ||
    "http://localhost:5000/api/payments/momo/return",
  ipnUrl:
    process.env.MOMO_IPN_URL || "http://localhost:5000/api/payments/momo/ipn",
  lang: process.env.MOMO_LANG || "vi",
});

export const assertMomoConfigured = () => {
  const config = getMomoConfig();
  const missing = ["partnerCode", "accessKey", "secretKey"].filter(
    (key) => !config[key],
  );

  if (missing.length > 0) {
    const vars = missing
      .map((key) => `MOMO_${key.replace(/[A-Z]/g, (m) => `_${m}`).toUpperCase()}`)
      .join(", ");
    throw new Error(`Missing MoMo sandbox config: ${vars}`);
  }

  return config;
};

export const signMomoPayload = (rawSignature, secretKey) =>
  crypto.createHmac("sha256", secretKey).update(rawSignature).digest("hex");

const buildCreateSignature = ({
  accessKey,
  amount,
  extraData,
  ipnUrl,
  orderId,
  orderInfo,
  partnerCode,
  redirectUrl,
  requestId,
  requestType,
}) =>
  [
    `accessKey=${accessKey}`,
    `amount=${amount}`,
    `extraData=${extraData}`,
    `ipnUrl=${ipnUrl}`,
    `orderId=${orderId}`,
    `orderInfo=${orderInfo}`,
    `partnerCode=${partnerCode}`,
    `redirectUrl=${redirectUrl}`,
    `requestId=${requestId}`,
    `requestType=${requestType}`,
  ].join("&");

const buildResultSignature = (payload, accessKey) =>
  [
    `accessKey=${accessKey}`,
    `amount=${payload.amount}`,
    `extraData=${payload.extraData || ""}`,
    `message=${payload.message || ""}`,
    `orderId=${payload.orderId}`,
    `orderInfo=${payload.orderInfo || ""}`,
    `orderType=${payload.orderType || ""}`,
    `partnerCode=${payload.partnerCode}`,
    `payType=${payload.payType || ""}`,
    `requestId=${payload.requestId}`,
    `responseTime=${payload.responseTime}`,
    `resultCode=${payload.resultCode}`,
    `transId=${payload.transId || ""}`,
  ].join("&");

export const createMomoPaymentRequest = async ({
  amount,
  orderId,
  orderInfo,
  requestId,
  extraData = "",
  redirectUrl,
  ipnUrl,
}) => {
  const config = assertMomoConfigured();
  const numericAmount = Number(amount);
  const payload = {
    partnerCode: config.partnerCode,
    requestType: config.requestType,
    ipnUrl: ipnUrl || config.ipnUrl,
    redirectUrl: redirectUrl || config.redirectUrl,
    orderId,
    amount: numericAmount,
    orderInfo,
    requestId,
    extraData,
    lang: config.lang,
  };

  const rawSignature = buildCreateSignature({
    ...payload,
    accessKey: config.accessKey,
  });

  payload.signature = signMomoPayload(rawSignature, config.secretKey);

  const response = await fetch(config.endpoint, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
    timeout: 30000,
  });

  const data = await response.json().catch(() => null);
  if (!response.ok || !data) {
    throw new Error(
      `MoMo create payment failed with HTTP ${response.status}`,
    );
  }

  return { request: payload, response: data };
};

export const verifyMomoResultSignature = (payload) => {
  const config = assertMomoConfigured();
  const expected = signMomoPayload(
    buildResultSignature(payload, config.accessKey),
    config.secretKey,
  );

  if (!payload.signature || expected.length !== payload.signature.length) {
    return false;
  }

  return crypto.timingSafeEqual(
    Buffer.from(expected, "utf8"),
    Buffer.from(payload.signature, "utf8"),
  );
};
