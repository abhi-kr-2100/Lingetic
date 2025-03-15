import { useAuth } from '@clerk/nextjs';
import '@testing-library/jest-dom';

jest.mock('@clerk/nextjs', () => ({
  useAuth: jest.fn(),
}));

useAuth.mockReturnValue({
  isSignedIn: true,
  isLoaded: true,
  getToken: jest.fn().mockResolvedValue('mock-token'),
});
