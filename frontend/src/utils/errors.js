export const getErrorMessage = (error, fallback) => {
  const responseError = error.response?.data?.error;
  const detailMessages = responseError?.details
    ?.map((detail) => detail.message)
    .filter(Boolean);

  if (detailMessages?.length > 0) {
    return detailMessages.join(" ");
  }

  return responseError?.message || error.response?.data?.message || fallback;
};
