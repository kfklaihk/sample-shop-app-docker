import React from 'react';
import { Route, hashHistory } from 'react-router';
import { useAuth } from '../context/AuthContext';

const ProtectedRoute = ({ component: Component, ...rest }) => {
  const { isAuthenticated, loading } = useAuth();

  // Show loading while checking authentication
  if (loading) {
    return (
      <div style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '200px',
        fontSize: '16px'
      }}>
        Loading...
      </div>
    );
  }

  // If not authenticated, redirect to auth page
  if (!isAuthenticated) {
    hashHistory.push('/auth');
    return null;
  }

  // Render the protected component
  return <Route {...rest} component={Component} />;
};

export default ProtectedRoute;