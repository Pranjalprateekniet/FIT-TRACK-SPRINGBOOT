// Fallback in-memory storage if expo-secure-store is not installed
let memoryStorage: Record<string, string> = {};

let SecureStore: any;
try {
  // Try to load dynamically to avoid static compilation errors if the module is missing
  SecureStore = require('expo-secure-store');
} catch (e) {
  SecureStore = {
    setItemAsync: async (key: string, value: string) => {
      memoryStorage[key] = value;
    },
    getItemAsync: async (key: string) => {
      return memoryStorage[key] || null;
    },
    deleteItemAsync: async (key: string) => {
      delete memoryStorage[key];
    }
  };
}

export const saveToken = async (key: string, value: string): Promise<void> => {
  try {
    await SecureStore.setItemAsync(key, value);
  } catch (error) {
    console.warn("SecureStore setItem failed, falling back to memory:", error);
    memoryStorage[key] = value;
  }
};

export const getToken = async (key: string): Promise<string | null> => {
  try {
    return await SecureStore.getItemAsync(key);
  } catch (error) {
    console.warn("SecureStore getItem failed, falling back to memory:", error);
    return memoryStorage[key] || null;
  }
};

export const deleteToken = async (key: string): Promise<void> => {
  try {
    await SecureStore.deleteItemAsync(key);
  } catch (error) {
    console.warn("SecureStore deleteItem failed, falling back to memory:", error);
    delete memoryStorage[key];
  }
};
