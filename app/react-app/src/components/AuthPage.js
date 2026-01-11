import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import TextField from 'material-ui/TextField';
import RaisedButton from 'material-ui/RaisedButton';
import Paper from 'material-ui/Paper';
import { hashHistory } from 'react-router';

const AuthPage = () => {
  const { login, register } = useAuth();
  const [isLogin, setIsLogin] = useState(true);
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    passwordConfirm: '',
    name: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleInputChange = (field) => (event) => {
    setFormData(prev => ({
      ...prev,
      [field]: event.target.value
    }));
    setError(''); // Clear error on input change
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      if (isLogin) {
        await login(formData.username, formData.password);
        hashHistory.push('/'); // Redirect to home after login
      } else {
        if (formData.password !== formData.passwordConfirm) {
          setError('Passwords do not match');
          setLoading(false);
          return;
        }
        await register({
          username: formData.username,
          email: formData.email,
          password: formData.password,
          passwordConfirm: formData.passwordConfirm,
          name: formData.name,
        });
        hashHistory.push('/'); // Redirect to home after registration
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const toggleMode = () => {
    setIsLogin(!isLogin);
    setError('');
    setFormData({
      username: '',
      email: '',
      password: '',
      passwordConfirm: '',
      name: '',
    });
  };

  const paperStyle = {
    padding: 20,
    margin: '20px auto',
    maxWidth: 400,
  };

  const formStyle = {
    display: 'flex',
    flexDirection: 'column',
    gap: '16px',
  };

  const buttonStyle = {
    marginTop: '16px',
  };

  return (
    <div style={{ padding: '20px', maxWidth: '400px', margin: '0 auto' }}>
      <Paper style={paperStyle}>
        <h2 style={{ textAlign: 'center', marginBottom: '20px' }}>
          {isLogin ? 'Login' : 'Register'}
        </h2>

        <form onSubmit={handleSubmit} style={formStyle}>
          <TextField
            hintText="Username"
            floatingLabelText="Username"
            value={formData.username}
            onChange={handleInputChange('username')}
            required
            fullWidth
          />

          {!isLogin && (
            <div>
              <TextField
                hintText="Email"
                floatingLabelText="Email"
                type="email"
                value={formData.email}
                onChange={handleInputChange('email')}
                required
                fullWidth
              />

              <TextField
                hintText="Full Name"
                floatingLabelText="Full Name"
                value={formData.name}
                onChange={handleInputChange('name')}
                required
                fullWidth
              />
            </div>
          )}

          <TextField
            hintText="Password"
            floatingLabelText="Password"
            type="password"
            value={formData.password}
            onChange={handleInputChange('password')}
            required
            fullWidth
          />

          {!isLogin && (
            <TextField
              hintText="Confirm Password"
              floatingLabelText="Confirm Password"
              type="password"
              value={formData.passwordConfirm}
              onChange={handleInputChange('passwordConfirm')}
              required
              fullWidth
            />
          )}

          {error && (
            <div style={{ color: 'red', fontSize: '14px', textAlign: 'center' }}>
              {error}
            </div>
          )}

          <RaisedButton
            label={loading ? 'Please wait...' : (isLogin ? 'Login' : 'Register')}
            primary
            type="submit"
            disabled={loading}
            fullWidth
            style={buttonStyle}
          />

          <div style={{ textAlign: 'center', marginTop: '16px' }}>
            <span>
              {isLogin ? "Don't have an account? " : "Already have an account? "}
            </span>
            <button
              type="button"
              onClick={toggleMode}
              style={{
                background: 'none',
                border: 'none',
                color: '#1976d2',
                cursor: 'pointer',
                textDecoration: 'underline',
              }}
            >
              {isLogin ? 'Register' : 'Login'}
            </button>
          </div>
        </form>
      </Paper>
    </div>
  );
};

export default AuthPage;