import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/auth.guard';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { ShellComponent } from './pages/shell/shell.component';
import { OverviewComponent } from './pages/overview/overview.component';
import { MoveMoneyComponent } from './pages/move-money/move-money.component';
import { ActivityComponent } from './pages/activity/activity.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: 'login', component: LoginComponent, canActivate: [guestGuard] },
  { path: 'register', component: RegisterComponent, canActivate: [guestGuard] },
  {
    path: 'app',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', component: OverviewComponent },
      { path: 'move', component: MoveMoneyComponent },
      { path: 'activity', component: ActivityComponent }
    ]
  },
  { path: '**', redirectTo: 'login' }
];
