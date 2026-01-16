import React, { Component } from 'react';

// Manual implementation of Context API for React 15 which doesn't have React.createContext
const listeners = new Set();
let currentContextValue = {
  user: null,
  loading: true,
  isAuthenticated: false,
};

const updateContext = (newValue) => {
  currentContextValue = { ...currentContextValue, ...newValue };
  listeners.forEach(listener => listener(currentContextValue));
};

export class AuthConsumer extends Component {
  constructor(props) {
    super(props);
    this.state = { value: currentContextValue };
  }
  componentDidMount() {
    listeners.add(this.handleChange);
    // Sync with global value in case it changed between constructor and mount
    if (this.state.value !== currentContextValue) {
      this.setState({ value: currentContextValue });
    }
  }
  componentWillUnmount() {
    listeners.delete(this.handleChange);
  }
  handleChange = (value) => {
    this.setState({ value });
  };
  render() {
    return this.props.children(this.state.value);
  }
}

export class AuthProvider extends Component {
  constructor(props) {
    super(props);

    // Initial check for tokens to avoid "loading" flash if possible
    const accessToken = localStorage.getItem('accessToken');
    const refreshToken = localStorage.getItem('refreshToken');
    const username = localStorage.getItem('username');

    let initialUser = null;
    if (accessToken && refreshToken && username) {
      initialUser = { username, accessToken, refreshToken };
    }

    // Initialize the global context value
    updateContext({
      user: initialUser,
      loading: false,
      isAuthenticated: !!initialUser,
      login: this.login,
      register: this.register,
      logout: this.logout,
      refreshToken: this.refreshToken,
      getAuthHeaders: this.getAuthHeaders,
    });
  }

  login = async (username, password) => {
    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ username, password }),
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || 'Login failed');
      }

      const data = await response.json();
      const user = {
        username: data.username,
        accessToken: data.accessToken,
        refreshToken: data.refreshToken,
      };

      localStorage.setItem('accessToken', data.accessToken);
      localStorage.setItem('refreshToken', data.refreshToken);
      localStorage.setItem('username', data.username);

      updateContext({ user, isAuthenticated: true });
      return user;
    } catch (error) {
      throw error;
    }
  };

  register = async (userData) => {
    try {
      const response = await fetch('/api/auth/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(userData),
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || 'Registration failed');
      }

      const data = await response.json();
      const user = {
        username: data.username,
        accessToken: data.accessToken,
        refreshToken: data.refreshToken,
      };

      localStorage.setItem('accessToken', data.accessToken);
      localStorage.setItem('refreshToken', data.refreshToken);
      localStorage.setItem('username', data.username);

      updateContext({ user, isAuthenticated: true });
      return user;
    } catch (error) {
      throw error;
    }
  };

  logout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('username');
    updateContext({ user: null, isAuthenticated: false });
  };

  refreshToken = async () => {
    try {
      const refreshToken = localStorage.getItem('refreshToken');
      if (!refreshToken) {
        throw new Error('No refresh token available');
      }

      const response = await fetch('/api/auth/refresh-token', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ refreshToken }),
      });

      if (!response.ok) {
        throw new Error('Token refresh failed');
      }

      const data = await response.json();
      localStorage.setItem('accessToken', data.accessToken);
      localStorage.setItem('refreshToken', data.refreshToken);

      const updatedUser = {
        ...currentContextValue.user,
        accessToken: data.accessToken,
        refreshToken: data.refreshToken,
      };

      updateContext({ user: updatedUser });
      return updatedUser;
    } catch (error) {
      this.logout();
      throw error;
    }
  };

  getAuthHeaders = () => {
    const accessToken = localStorage.getItem('accessToken');
    return accessToken ? { 'Authorization': `Bearer ${accessToken}` } : {};
  };

  render() {
    // In React 15, we just return the children
    return this.props.children;
  }
}

// Helper component to use auth context in class components
export const withAuth = (WrappedComponent) => {
  return class extends Component {
    render() {
      return (
        <AuthConsumer>
          {(auth) => <WrappedComponent {...this.props} auth={auth} />}
        </AuthConsumer>
      );
    }
  };
};
