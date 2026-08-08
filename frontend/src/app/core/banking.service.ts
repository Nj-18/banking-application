import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import {
  AmountRequest,
  BankAccount,
  CreateAccountRequest,
  Customer,
  StatementResponse,
  TransactionItem,
  TransferRequest,
  TransferResponse
} from './models';

@Injectable({ providedIn: 'root' })
export class BankingService {
  constructor(private readonly http: HttpClient) {}

  getCustomer(id: number) {
    return this.http.get<Customer>(`/api/customer/${id}`);
  }

  createAccount(payload: CreateAccountRequest) {
    return this.http.post<BankAccount>('/api/accounts/', payload);
  }

  deposit(payload: AmountRequest) {
    return this.http.post('/api/accounts/deposit', payload);
  }

  withdraw(payload: AmountRequest) {
    return this.http.post('/api/accounts/withdraw', payload);
  }

  transfer(payload: TransferRequest) {
    return this.http.post<TransferResponse>('/api/transactions/transfer', payload);
  }

  getTransactions(accountNumber: string) {
    return this.http.get<TransactionItem[]>(`/api/transactions/${accountNumber}`);
  }

  getStatement(accountNumber: string, fromDate: string, toDate: string, page = 0, size = 20) {
    const params = new HttpParams()
      .set('accountNumber', accountNumber)
      .set('fromDate', fromDate)
      .set('toDate', toDate)
      .set('page', page)
      .set('size', size);
    return this.http.get<StatementResponse>('/api/transactions/statement', { params });
  }
}
