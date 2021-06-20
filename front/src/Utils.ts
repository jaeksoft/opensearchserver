export const toDateTime = (timeMs?: number): string | undefined => {
  if (!timeMs)
    return undefined;
  const date = new Date(timeMs);
  return date.toLocaleString();
};
