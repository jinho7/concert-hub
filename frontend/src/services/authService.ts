import { LoginRequest, JwtTokens, ApiResponse } from '../types/auth';

const TOKEN_KEY = 'concert_hub_tokens';
const API_BASE_URL = 'http://localhost:8080/api';

class AuthService {
  // 로그인
  async login(loginData: LoginRequest): Promise<JwtTokens> {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(loginData),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || '로그인에 실패했습니다.');
      }

      const result: ApiResponse<JwtTokens> = await response.json();
      
      if (result.success && result.data) {
        this.setTokens(result.data);
        return result.data;
      } else {
        throw new Error(result.message || '로그인에 실패했습니다.');
      }
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    }
  }

  // 로그아웃
  async logout(): Promise<void> {
    try {
      const tokens = this.getTokens();
      if (tokens?.accessToken) {
        await fetch(`${API_BASE_URL}/auth/logout`, {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${tokens.accessToken}`,
            'Content-Type': 'application/json',
          },
        });
      }
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      this.clearTokens();
    }
  }

  // 토큰 갱신
  async refreshTokens(): Promise<JwtTokens | null> {
    try {
      const tokens = this.getTokens();
      if (!tokens?.refreshToken) {
        return null;
      }

      const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ refreshToken: tokens.refreshToken }),
      });

      if (!response.ok) {
        this.clearTokens();
        return null;
      }

      const result: ApiResponse<JwtTokens> = await response.json();
      
      if (result.success && result.data) {
        this.setTokens(result.data);
        return result.data;
      } else {
        this.clearTokens();
        return null;
      }
    } catch (error) {
      console.error('Token refresh error:', error);
      this.clearTokens();
      return null;
    }
  }

  // 토큰 저장 - localStorage에 JWT 토큰만 저장
  setTokens(tokens: JwtTokens): void {
    localStorage.setItem(TOKEN_KEY, JSON.stringify(tokens));
  }

  // 토큰 조회
  getTokens(): JwtTokens | null {
    try {
      const tokens = localStorage.getItem(TOKEN_KEY);
      return tokens ? JSON.parse(tokens) : null;
    } catch {
      return null;
    }
  }

  // 토큰 삭제
  clearTokens(): void {
    localStorage.removeItem(TOKEN_KEY);
    // 기존 currentUser도 함께 삭제 (마이그레이션)
    localStorage.removeItem('currentUser');
  }

  // 로그인 상태 확인
  isAuthenticated(): boolean {
    const tokens = this.getTokens();
    return !!tokens?.accessToken;
  }

  // 액세스 토큰 조회 - API 요청 시 사용
  getAccessToken(): string | null {
    const tokens = this.getTokens();
    return tokens?.accessToken || null;
  }
}

export default new AuthService();
