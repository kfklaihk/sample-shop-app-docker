import React from 'react';
import { hashHistory } from 'react-router';

const ProtectedRoute = ({ component: Component, ...rest }) => {
  // For React Router v2, we need to handle authentication differently
  // This is a simplified version that checks auth before rendering
  const isAuthenticated = localStorage.getItem('accessToken') !== null;

  if (!isAuthenticated) {
    hashHistory.push('/auth');
    return null;
  }

  // Render the protected component
  return <Component {...rest} />;
};

export default ProtectedRoute;