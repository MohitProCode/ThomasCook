import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { getAccount } from '../api/account.js';
const AuthContext = createContext(null);
const TOKEN='tc_access_token', USER='tc_user';
function read(key) { try { return JSON.parse(localStorage.getItem(key)); } catch { return null; } }
export function AuthProvider({ children }) {
  const [user,setUser]=useState(()=>read(USER));
  useEffect(() => { if (localStorage.getItem(TOKEN)) getAccount().then(account => { localStorage.setItem(USER, JSON.stringify(account)); setUser(account); }).catch(() => { localStorage.removeItem(TOKEN); localStorage.removeItem(USER); setUser(null); }); }, []);
  function save(data) { localStorage.setItem(TOKEN,data.token); localStorage.setItem(USER,JSON.stringify(data.user)); setUser(data.user); }
  function logout() { localStorage.removeItem(TOKEN); localStorage.removeItem(USER); setUser(null); }
  const value=useMemo(()=>({user,save,logout,isAuthenticated:Boolean(user)}),[user]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
export function useAuth() { return useContext(AuthContext); }
