import React from 'react';
import { useAuth } from '../../context/AuthContext';
import RaisedButton from 'material-ui/RaisedButton';
import './styles.css'

const Header = () => {
  const { user, logout, isAuthenticated } = useAuth();
  const title = isAuthenticated
    ? `Welcome to the atsea shop, ${user.username}!`
    : 'Welcome to the atsea shop'
  const subtitle = isAuthenticated
    ? 'You are logged in and can browse products and place orders'
    : 'Please login to browse products and place orders'

  const handleLogout = () => {
    logout();
  };

  return (
    <div className='headerSection'>
      <div className='headerTitle'>
        {title}
      </div>
      <div className='headerSubtitle'>
        {subtitle}
      </div>
      {isAuthenticated && (
        <div style={{ marginTop: '10px' }}>
          <RaisedButton
            label="Logout"
            onClick={handleLogout}
            secondary
          />
        </div>
      )}
    </div>
  );
}

export default Header;
