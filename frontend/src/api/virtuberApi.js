import api from "./axios";

const unwrap = (response) => response.data.data;

export const signup = async ({ userId, password }) => {
  const response = await api.post("/api/v1/auth/signup", {
    userId,
    password,
  });

  return unwrap(response);
};

export const signin = async ({ userId, password }) => {
  const response = await api.post("/api/v1/auth/signin", {
    userId,
    password,
  });

  return unwrap(response);
};

export const getStocks = async () => {
  const response = await api.get("/api/v1/stocks");
  return unwrap(response);
};

export const getStockPriceHistories = async (stockId) => {
  const response = await api.get(`/api/v1/stocks/${stockId}/price-histories`);
  return unwrap(response);
};

export const getMyAccount = async () => {
  const response = await api.get("/api/v1/accounts/me");
  return unwrap(response);
};

export const buyStock = async ({ stockId, quantity }) => {
  const response = await api.post("/api/v1/trades/buy", {
    stockId,
    quantity,
  });

  return unwrap(response);
};

export const sellStock = async ({ stockId, quantity }) => {
  const response = await api.post("/api/v1/trades/sell", {
    stockId,
    quantity,
  });

  return unwrap(response);
};

export const getTrades = async () => {
  const response = await api.get("/api/v1/trades");
  return unwrap(response);
};

export const resetAccount = async ({ password }) => {
  const response = await api.put("/api/v1/accounts/me/init", {
    password,
  });

  return unwrap(response);
};
