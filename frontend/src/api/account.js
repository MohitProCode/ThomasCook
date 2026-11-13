import { apiFetch } from './client.js';
export function getAccount(){return apiFetch('/api/account');}
export function updateAccount(displayName){return apiFetch('/api/account',{method:'PATCH',body:{displayName}});}
export function changePassword(currentPassword,newPassword){return apiFetch('/api/account/password',{method:'POST',body:{currentPassword,newPassword}});}
