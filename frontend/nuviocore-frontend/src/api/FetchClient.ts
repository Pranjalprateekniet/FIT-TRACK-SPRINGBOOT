import { getToken, saveToken, deleteToken } from '../utils/SecureStore';

// For Android Emulator, use 'http://10.0.2.2:8080'
// For iOS/Web, use 'http://10.0.2.2:8080'
const BASE_URL = 'http://10.0.2.2:8080';

let accessToken: string | null = null;
let isRefreshing = false;
let refreshSubscribers: ((token: string) => void)[] = [];

const subscribeTokenRefresh = (cb: (token: string) => void) => {
  refreshSubscribers.push(cb);
};

const onRefreshed = (token: string) => {
  refreshSubscribers.forEach((cb) => cb(token));
  refreshSubscribers = [];
};

export const setAccessToken = (token: string | null) => {
  accessToken = token;
};

export const getAccessToken = () => accessToken;

interface RequestOptions extends RequestInit {
  body?: any;
}

export const fetchClient = async (endpoint: string, options: RequestOptions = {}): Promise<any> => {
  const url = `${BASE_URL}${endpoint}`;

  const headers = new Headers(options.headers || {});
  if (!headers.has('Content-Type') && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }

  if (accessToken) {
    headers.set('Authorization', `Bearer ${accessToken}`);
  }

  const config: RequestInit = {
    ...options,
    headers,
  };

  if (options.body && !(options.body instanceof FormData) && typeof options.body !== 'string') {
    config.body = JSON.stringify(options.body);
  }

  try {
    const response = await fetch(url, config);

    // Handle 401 Unauthorized (Access token expired)
    if (response.status === 401 && !endpoint.includes('/api/auth/login') && !endpoint.includes('/api/auth/refresh')) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          subscribeTokenRefresh((newToken) => {
            headers.set('Authorization', `Bearer ${newToken}`);
            fetch(url, { ...config, headers })
              .then(async (res) => {
                if (res.headers.get('content-type')?.includes('application/json')) {
                  return res.json();
                }
                return res.text();
              })
              .then(resolve)
              .catch(reject);
          });
        });
      }

      isRefreshing = true;
      const refreshToken = await getToken('refreshToken');

      if (!refreshToken) {
        isRefreshing = false;
        setAccessToken(null);
        await deleteToken('refreshToken');
        throw new Error('SESSION_EXPIRED');
      }

      try {
        const refreshResponse = await fetch(`${BASE_URL}/api/auth/refresh`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ refreshToken }),
        });

        if (!refreshResponse.ok) {
          throw new Error('REFRESH_FAILED');
        }

        const data = await refreshResponse.json();
        setAccessToken(data.accessToken);
        await saveToken('refreshToken', data.refreshToken);

        isRefreshing = false;
        onRefreshed(data.accessToken);

        headers.set('Authorization', `Bearer ${data.accessToken}`);
        const retryResponse = await fetch(url, { ...config, headers });

        if (retryResponse.headers.get('content-type')?.includes('application/json')) {
          return await retryResponse.json();
        }
        return await retryResponse.text();

      } catch (error) {
        isRefreshing = false;
        setAccessToken(null);
        await deleteToken('refreshToken');
        throw new Error('SESSION_EXPIRED');
      }
    }

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(errorText || `Request failed with status ${response.status}`);
    }

    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      return await response.json();
    } else {
      return await response.text();
    }

  } catch (error) {
    throw error;
  }
};
