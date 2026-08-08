import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/auth.service';
import { BankingService } from '../../core/banking.service';
import { BankAccount, TransactionItem } from '../../core/models';
import { daysAgoIso, extractErrorMessage, todayIso } from '../../core/utils';

@Component({
  selector: 'app-activity',
  standalone: true,
  imports: [ReactiveFormsModule, CurrencyPipe, DatePipe],
  templateUrl: './activity.component.html',
  styleUrl: './activity.component.scss'
})
export class ActivityComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly banking = inject(BankingService);

  readonly accounts = signal<BankAccount[]>([]);
  readonly transactions = signal<TransactionItem[]>([]);
  readonly balance = signal<number | null>(null);
  readonly error = signal('');
  readonly loading = signal(false);

  readonly form = this.fb.nonNullable.group({
    accountNumber: ['', Validators.required],
    fromDate: [daysAgoIso(30), Validators.required],
    toDate: [todayIso(), Validators.required]
  });

  ngOnInit(): void {
    const customerId = this.auth.user()?.customerId;
    if (!customerId) {
      this.error.set('No customer profile is linked to this login.');
      return;
    }
    this.banking.getCustomer(customerId).subscribe({
      next: (customer) => {
        const accounts = customer.bankAccounts ?? [];
        this.accounts.set(accounts);
        if (accounts[0]) {
          this.form.patchValue({ accountNumber: accounts[0].accountNumber });
          this.load();
        }
      },
      error: (err) => this.error.set(extractErrorMessage(err))
    });
  }

  load(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.error.set('');
    const { accountNumber, fromDate, toDate } = this.form.getRawValue();
    this.banking.getStatement(accountNumber, fromDate, toDate).subscribe({
      next: (statement) => {
        this.transactions.set(statement.transactions ?? []);
        this.balance.set(statement.currentBalance);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(extractErrorMessage(err));
      }
    });
  }
}
