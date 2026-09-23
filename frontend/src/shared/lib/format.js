export const money = (minor, currency = "BDT") =>
  new Intl.NumberFormat(undefined, { style: "currency", currency }).format(
    minor /
      10 **
        new Intl.NumberFormat(undefined, {
          style: "currency",
          currency,
        }).resolvedOptions().maximumFractionDigits,
  );
export const dateTime = (value) =>
  new Date(value).toLocaleString(undefined, {
    dateStyle: "medium",
    timeStyle: "short",
  });
export const localDateTime = (value) => {
  const d = new Date(value);
  return new Date(d.getTime() - d.getTimezoneOffset() * 60000)
    .toISOString()
    .slice(0, 16);
};
