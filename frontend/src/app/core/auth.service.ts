import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { LoginRequest, LoginResponse, RegisterRequest, RegisterResponse } from './models';

const TOKEN_KEY = 'northline_token';
const USER_KEY = 'northline_user';

export interface SessionUser {
  username: string;
  role: string;
  customerId: number | null;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly user = signal<SessionUser | null>(this.readUser());
  readonly token = signal<string | null>(localStorage.getItem(TOKEN_KEY));

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router
  ) {}

  get isAuthenticated(): boolean {
    return !!this.token();
  }

  login(payload: LoginRequest) {
    return this.http.post<LoginResponse>('/api/auth/login', payload).pipe(
      tap((res) => this.persistSession(res))
    );
  }

  register(payload: RegisterRequest) {
    return this.http.post<RegisterResponse>('/api/auth/register', payload);
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.token.set(null);
    this.user.set(null);
    void this.router.navigateByUrl('/login');
  }

  private persistSession(res: LoginResponse): void {
    localStorage.setItem(TOKEN_KEY, res.token);
    const session: SessionUser = {
      username: res.username,
      role: res.role,
      customerId: res.customerId
    };
    localStorage.setItem(USER_KEY, JSON.stringify(session));
    this.token.set(res.token);
    this.user.set(session);
  }

  private readUser(): SessionUser | null {
    const raw = localStorage.getItem(USER_KEY);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as SessionUser;
    } catch {
      return null;
    }
  }
}
