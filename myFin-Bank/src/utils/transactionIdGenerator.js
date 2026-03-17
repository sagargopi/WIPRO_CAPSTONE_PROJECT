/**
 * Generate a user-friendly transaction ID
 * Format: USER${userId}TXN${random}
 * Example: USER123TXN7A9K2P5
 */
export const generateUserFriendlyTransactionId = (userId) => {
  if (!userId) {
    throw new Error("User ID is required to generate transaction ID");
  }

  const randomComponent = Math.random()
    .toString(36)
    .substring(2, 10)
    .toUpperCase();

  return `USER${userId}TXN${randomComponent}`;
};
