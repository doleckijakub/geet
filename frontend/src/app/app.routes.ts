import { Routes } from '@angular/router';

import { App } from './app';
import Login from './auth/login/login';
import Register from './auth/register/register';
import Home from './home/home';

export const routes: Routes = [
  { path: '', component: App },
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'home', component: Home },
];
