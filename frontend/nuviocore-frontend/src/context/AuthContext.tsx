import React, { createContext, useContext, useState, useEffect } from 'react';
import { fetchClient, setAccessToken } from '../api/FetchClient';
import { saveToken, getToken, deleteToken } from '../utils/SecureStore';

interface AuthContextType {
  isAuthenticated: boolean;
  isLoading: boolean;
  user: string | null;
  error: string | null;
  login: (email: string, password: string) => Promise<void>;
  register: (name: string, email: string, password: string) => Promise<string>;
  verifyEmail: (token: string) => Promise<string>;
  logout: () => Promise<void>;
  clearError: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

// Pure JS base64 decoder to avoid global atob reliance in mobile engines
const decodeJwt = (token: string) => {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    const base64Url = parts[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=';
    let str = base64;
    let output = '';
    let buffer = 0;
    let bits = 0;
    
    for (let i = 0; i < str.length; i++) {
      const char = str[i];
      if (char === '=') break;
      const idx = chars.indexOf(char);
      if (idx === -1) continue;
      buffer = (buffer << 6) | idx;
      bits += 6;
      if (bits >= 8) {
        bits -= 8;
        output += String.fromCharCode((buffer >> bits) & 0xff);
      }
    }
    return JSON.parse(output);
  } catch (e) {
    console.error("Failed to decode JWT:", e);
    return null;
  }
};

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const clearError = () => setError(null);

  // Auto Login on startup if a valid refresh token is available
  useEffect(() => {
    const initializeAuth = async () => {
      try {
        const refreshToken = await getToken('refreshToken');
        if (refreshToken) {
          const data = await fetchClient('/api/auth/refresh', {
            method: 'POST',
            body: { refreshToken }
          });
          setAccessToken(data.accessToken);
          await saveToken('refreshToken', data.refreshToken);
          
          const payload = decodeJwt(data.accessToken);
          if (payload && payload.sub) {
            setUser(payload.sub);
            setIsAuthenticated(true);
          }
        }
      } catch (err) {
        console.log("Auto-login failed:", err);
      } finally {
        setIsLoading(false);
      }
    };
    initializeAuth();
  }, []);

  const login = async (email: string, password: string) => {
    setError(null);
    try {
      const data = await fetchClient('/api/auth/login', {
        method: 'POST',
        body: { email, password }
      });
      
      setAccessToken(data.accessToken);
      await saveToken('refreshToken', data.refreshToken);
      
      const payload = decodeJwt(data.accessToken);
      setUser(payload && payload.sub ? payload.sub : email);
      setIsAuthenticated(true);
    } catch (err: any) {
      setError(err.message || 'Login failed');
      throw err;
    }
  };

  const register = async (name: string, email: string, password: string) => {
    setError(null);
    try {
      const message = await fetchClient('/api/auth/register', {
        method: 'POST',
        body: { name, email, password }
      });
      return message;
    } catch (err: any) {
      setError(err.message || 'Registration failed');
      throw err;
    }
  };

  const verifyEmail = async (token: string) => {
    setError(null);
    try {
      const message = await fetchClient(`/api/auth/verify-email?token=${token}`, {
        method: 'GET'
      });
      return message;
    } catch (err: any) {
      setError(err.message || 'Email verification failed');
      throw err;
    }
  };

  const logout = async () => {
    setAccessToken(null);
    await deleteToken('refreshToken');
    setUser(null);
    setIsAuthenticated(false);
  };

  return (
    <AuthContext.Provider value={{
      isAuthenticated,
      isLoading,
      user,
      error,
      login,
      register,
      verifyEmail,
      logout,
      clearError
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
