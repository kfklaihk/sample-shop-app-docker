import React, { Component, createContext } from 'react';

const AuthContext = createContext();

export const AuthConsumer = AuthContext.Consumer;

export class AuthProvider extends Component {
  constructor(props) {
    super(props);
    this.state = {
      user: null,
      loading: true,
    };

    // Check for stored tokens on app start
    const accessToken = localStorage.getItem('accessToken');
    const refreshToken = localStorage.getItem('refreshToken');
    const username = localStorage.getItem('username');

    if (accessToken && refreshToken && username) {
      this.state.user = { username, accessToken, refreshToken };
    }
    this.state.loading = false;
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
      const userData = {
        username: data.username,
        accessToken: data.accessToken,
        refreshToken: data.refreshToken,
      };

      localStorage.setItem('accessToken', data.accessToken);
      localStorage.setItem('refreshToken', data.refreshToken);
      localStorage.setItem('username', data.username);

      this.setState({ user: userData });
      return userData;
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

      this.setState({ user });
      return user;
    } catch (error) {
      throw error;
    }
  };

  logout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('username');
    this.setState({ user: null });
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
        ...this.state.user,
        accessToken: data.accessToken,
        refreshToken: data.refreshToken,
      };

      this.setState({ user: updatedUser });
      return updatedUser;
    } catch (error) {
      // If refresh fails, logout user
      this.logout();
      throw error;
    }
  };

  getAuthHeaders = () => {
    const accessToken = localStorage.getItem('accessToken');
    return accessToken ? { 'Authorization': `Bearer ${accessToken}` } : {};
  };

  render() {
    const { children } = this.props;
    const { user, loading } = this.state;

    const value = {
      user,
      loading,
      login: this.login,
      register: this.register,
      logout: this.logout,
      refreshToken: this.refreshToken,
      getAuthHeaders: this.getAuthHeaders,
      isAuthenticated: !!user,
    };

    return (
      <AuthContext.Provider value={value}>
        {children}
      </AuthContext.Provider>
    );
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