export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  message: string;
  username: string;
  role: string;
  token: string;
  authenticated: boolean;
  customerId: number | null;
}

export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  firstName: string;
  lastName: string;
  mobileNumber: string;
  customerId?: number | null;
}

export interface RegisterResponse {
  userId: number;
  username: string;
  email: string;
  role: string;
  message: string;
}

export interface BankAccount {
  id: number;
  accountNumber: string;
  accountType: string;
  balance: number;
  accountStatus: string;
}

export interface Customer {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  mobileNumber: string;
  customerStatus: string;
  bankAccounts: BankAccount[] | null;
}

export interface CreateAccountRequest {
  customerId: number;
  accountType: string;
  openingBalance: number;
}

export interface AmountRequest {
  accountNumber: string;
  amount: number;
}

export interface TransferRequest {
  fromAccountNumber: string;
  toAccountNumber: string;
  amount: number;
  remarks: string;
}

export interface TransferResponse {
  transactionReference: string;
  fromAccount: string;
  toAccount: string;
  amount: number;
  senderBalance: number;
  receiverBalance: number;
  message: string;
}

export interface TransactionItem {
  transactionReference: string;
  transactionType: string;
  amount: number;
  balanceAfterTransaction: number | null;
  status: string;
  transactionDate: string;
}

export interface StatementResponse {
  accountNumber: string;
  currentBalance: number;
  transactions: TransactionItem[];
}

export interface ApiError {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  path?: string;
}
