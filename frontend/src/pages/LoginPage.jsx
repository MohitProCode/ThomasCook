import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { loginAccount, registerAccount } from '../api/auth.js';
import { useAuth } from '../context/AuthContext.jsx';
import ErrorBanner from '../components/ErrorBanner.jsx';

export default function LoginPage() {
  const [mode,setMode]=useState('login'); const [email,setEmail]=useState(''); const [password,setPassword]=useState(''); const [displayName,setDisplayName]=useState(''); const [error,setError]=useState(''); const [busy,setBusy]=useState(false);
  const auth=useAuth(); const navigate=useNavigate();
  async function submit(e) { e.preventDefault(); setBusy(true); setError(''); try { const data=mode==='login' ? await loginAccount({email,password}) : await registerAccount({email,password,displayName}); auth.save(data); navigate('/my-booking'); } catch(err) { setError(err.message); } finally { setBusy(false); } }
  return <section><h1>{mode==='login'?'Sign in':'Create your account'}</h1><p className="muted">Use one account to manage every Thomas Cook booking.</p><form className="card form" onSubmit={submit}>{mode==='register'&&<div className="field"><label>Full name</label><input required minLength="2" value={displayName} onChange={e=>setDisplayName(e.target.value)} /></div>}<div className="field"><label>Email</label><input required type="email" autoComplete="email" value={email} onChange={e=>setEmail(e.target.value)} /></div><div className="field"><label>Password</label><input required minLength="8" type="password" autoComplete={mode==='login'?'current-password':'new-password'} value={password} onChange={e=>setPassword(e.target.value)} /></div>{error&&<ErrorBanner message={error}/>}<button className="btn btn-primary" disabled={busy}>{busy?'Please wait…':mode==='login'?'Sign in':'Create account'}</button>{mode==='login'&&<Link to="/forgot-password">Forgot password?</Link>}</form><p className="muted">{mode==='login'?'New here?':'Already have an account?'} <button className="btn btn-ghost" type="button" onClick={()=>setMode(mode==='login'?'register':'login')}>{mode==='login'?'Create account':'Sign in'}</button></p><Link to="/">Back to stays</Link></section>;
}
