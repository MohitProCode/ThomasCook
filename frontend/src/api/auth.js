import { apiFetch } from './client.js';
export async function registerAccount(payload) { return apiFetch('/api/auth/register', { method:'POST', body:payload }); }
export async function loginAccount(payload) { return apiFetch('/api/auth/login', { method:'POST', body:payload }); }
export async function forgotPassword(email) { return apiFetch('/api/auth/forgot-password', { method:'POST', body:{ email } }); }
export async function resetPassword(token,newPassword) { return apiFetch('/api/auth/reset-password', { method:'POST', body:{ token,newPassword } }); }
