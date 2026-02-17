import React, { Component } from 'react';
import { AuthConsumer } from '../context/AuthContext';
import TextField from 'material-ui/TextField';
import RaisedButton from 'material-ui/RaisedButton';
import Paper from 'material-ui/Paper';
import { hashHistory } from 'react-router';

class AuthPage extends Component {
  constructor(props) {
    super(props);
    this.state = {
      isLogin: true,
      formData: {
        username: '',
        email: '',
        password: '',
        passwordConfirm: '',
        name: '',
      },
      error: '',
      loading: false,
    };
  }

  componentDidMount() {
    if (this.props.auth && this.props.auth.isAuthenticated) {
      hashHistory.push('/');
    }
  }

  componentDidUpdate(prevProps) {
    if (!prevProps.auth.isAuthenticated && this.props.auth.isAuthenticated) {
      hashHistory.push('/');
    }
  }

  handleInputChange = (field) => (event) => {
    this.setState({
      formData: {
        ...this.state.formData,
        [field]: event.target.value
      },
      error: '' // Clear error on input change
    });
  };

  handleSubmit = async (e) => {
    e.preventDefault();
    const { auth } = this.props;
    this.setState({ loading: true, error: '' });

    try {
      if (this.state.isLogin) {
        await auth.login(this.state.formData.username, this.state.formData.password);
        hashHistory.push('/'); // Redirect to home after login
      } else {
        if (this.state.formData.password !== this.state.formData.passwordConfirm) {
          this.setState({ error: 'Passwords do not match', loading: false });
          return;
        }
        await auth.register({
          username: this.state.formData.username,
          email: this.state.formData.email,
          password: this.state.formData.password,
          passwordConfirm: this.state.formData.passwordConfirm,
          name: this.state.formData.name,
        });
        this.setState({ loading: false });
        hashHistory.push('/'); // Redirect to home after registration
      }
    } catch (error) {
      this.setState({ error: error.message, loading: false });
    }
  };

  toggleMode = () => {
    this.setState({
      isLogin: !this.state.isLogin,
      error: '',
      formData: {
        username: '',
        email: '',
        password: '',
        passwordConfirm: '',
        name: '',
      }
    });
  };

  render() {
    const { isLogin, formData, error, loading } = this.state;

    return (
      <div style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '100vh',
        backgroundColor: '#f5f5f5',
        padding: '20px'
      }}>
        <Paper style={{
          padding: '40px',
          width: '100%',
          maxWidth: '400px'
        }}>
          <h2 style={{ textAlign: 'center', marginBottom: '30px' }}>
            {isLogin ? 'Login' : 'Register'}
          </h2>

          {error && (
            <div style={{
              color: '#d32f2f',
              backgroundColor: '#ffebee',
              padding: '10px',
              borderRadius: '4px',
              marginBottom: '20px'
            }}>
              {error}
            </div>
          )}

          <form onSubmit={this.handleSubmit}>
            <TextField
              floatingLabelText="Username"
              fullWidth={true}
              value={formData.username}
              onChange={this.handleInputChange('username')}
              required
              style={{ marginBottom: '20px' }}
            />

            {!isLogin && (
              <div>
                <TextField
                  floatingLabelText="Email"
                  type="email"
                  fullWidth={true}
                  value={formData.email}
                  onChange={this.handleInputChange('email')}
                  required
                  style={{ marginBottom: '20px' }}
                />

                <TextField
                  floatingLabelText="Full Name"
                  fullWidth={true}
                  value={formData.name}
                  onChange={this.handleInputChange('name')}
                  required
                  style={{ marginBottom: '20px' }}
                />
              </div>
            )}

            <TextField
              floatingLabelText="Password"
              type="password"
              fullWidth={true}
              value={formData.password}
              onChange={this.handleInputChange('password')}
              required
              style={{ marginBottom: '20px' }}
            />

            {!isLogin && (
              <TextField
                floatingLabelText="Confirm Password"
                type="password"
                fullWidth={true}
                value={formData.passwordConfirm}
                onChange={this.handleInputChange('passwordConfirm')}
                required
                style={{ marginBottom: '20px' }}
              />
            )}

            <RaisedButton
              type="submit"
              label={loading ? 'Please wait...' : (isLogin ? 'Login' : 'Register')}
              primary={true}
              fullWidth={true}
              disabled={loading}
              style={{ marginBottom: '20px' }}
            />

            <div style={{ textAlign: 'center' }}>
              <RaisedButton
                label={isLogin ? 'Need an account? Register' : 'Already have an account? Login'}
                onClick={this.toggleMode}
                style={{ marginTop: '10px' }}
              />
            </div>
          </form>
        </Paper>
      </div>
    );
  }
}

export default (props) => (
  <AuthConsumer>
    {(auth) => <AuthPage {...props} auth={auth} />}
  </AuthConsumer>
);