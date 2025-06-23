import { useState } from 'react';
import { api } from '../api';
import type { ApiResponse } from '../api';
import type { User } from '../types';

const UserRegisterPage = () => {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    phoneNumber: ''
  });
  const [loading, setLoading] = useState(false);
  const [user, setUser] = useState<User | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      const response = await api.post<ApiResponse<User>>('/users', formData);
      setUser(response.data.data);
      alert('회원가입이 완료되었습니다!');
      
      // 로컬 스토리지에 사용자 정보 저장 (간단한 로그인 대용)
      localStorage.setItem('currentUser', JSON.stringify(response.data.data));
    } catch (error) {
      console.error('회원가입 실패:', error);
      alert('회원가입에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  if (user) {
    return (
      <div className="max-w-md mx-auto bg-white rounded-lg shadow-md p-6">
        <div className="text-center">
          <div className="text-green-600 text-6xl mb-4">✅</div>
          <h2 className="text-2xl font-bold mb-4">가입 완료!</h2>
          <div className="text-left space-y-2">
            <p><strong>이름:</strong> {user.name}</p>
            <p><strong>이메일:</strong> {user.email}</p>
            <p><strong>전화번호:</strong> {user.phoneNumber}</p>
          </div>
          <button
            onClick={() => window.location.href = '/'}
            className="mt-6 w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700"
          >
            이벤트 보러가기
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-md mx-auto">
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-2xl font-bold mb-6 text-center">회원 가입</h2>
        
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              이름
            </label>
            <input
              type="text"
              required
              value={formData.name}
              onChange={(e) => setFormData({...formData, name: e.target.value})}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="홍길동"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              이메일
            </label>
            <input
              type="email"
              required
              value={formData.email}
              onChange={(e) => setFormData({...formData, email: e.target.value})}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="example@email.com"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              전화번호
            </label>
            <input
              type="tel"
              required
              pattern="[0-9]{3}-[0-9]{4}-[0-9]{4}"
              value={formData.phoneNumber}
              onChange={(e) => setFormData({...formData, phoneNumber: e.target.value})}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="010-1234-5678"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-600 text-white py-2 rounded-md hover:bg-blue-700 disabled:opacity-50"
          >
            {loading ? '가입 중...' : '가입하기'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default UserRegisterPage;