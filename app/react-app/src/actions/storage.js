export const getJwtToken = () => localStorage.getItem('accessToken');
export const setJwtToken = (token) => localStorage.setItem('accessToken', token);
export const removeJwtToken = () => localStorage.removeItem('accessToken');
