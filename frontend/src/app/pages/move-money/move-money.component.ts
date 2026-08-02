import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/auth.service';
import { BankingService } from '../../core/banking.service';
import { BankAccount } from '../../core/models';
import { extractErrorMessage } from '../../core/utils';

@Component({
  selector: 'app-move-money',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './move-money.component.html',
  styleUrl: './move-money.component.scss'
})
export class MoveMoneyComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly banking = inject(BankingService);

  readonly accounts = signal<BankAccount[]>([]);
  readonly tab = signal<'deposit' | 'withdraw' | 'transfer'>('deposit');
  readonly message = signal('');
  readonly error = signal('');
  readonly busy = signal(false);

  readonly amountForm = this.fb.nonNullable.group({
    accountNumber: ['', Validators.required],
    amount: [100, [Validators.required, Validators.min(0.01)]]
  });

  readonly transferForm = this.fb.nonNullable.group({
    fromAccountNumber: ['', Validators.required],
    toAccountNumber: ['', Validators.required],
    amount: [100, [Validators.required, Validators.min(0.01)]],
    remarks: ['Transfer']
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
          this.amountForm.patchValue({ accountNumber: accounts[0].accountNumber });
          this.transferForm.patchValue({ fromAccountNumber: accounts[0].accountNumber });
        }
      },
      error: (err) => this.error.set(extractErrorMessage(err))
    });
  }

  setTab(tab: 'deposit' | 'withdraw' | 'transfer'): void {
    this.tab.set(tab);
    this.message.set('');
    this.error.set('');
  }

  submitAmount(kind: 'deposit' | 'withdraw'): void {
    if (this.amountForm.invalid) {
      this.amountForm.markAllAsTouched();
      return;
    }
    this.busy.set(true);
    this.message.set('');
    this.error.set('');
    const payload = this.amountForm.getRawValue();
    const request$ = kind === 'deposit' ? this.banking.deposit(payload) : this.banking.withdraw(payload);
    request$.subscribe({
      next: (res: any) => {
        this.busy.set(false);
        this.message.set(res?.message || `${kind} completed.`);
      },
      error: (err) => {
        this.busy.set(false);
        this.error.set(extractErrorMessage(err));
      }
    });
  }

  submitTransfer(): void {
    if (this.transferForm.invalid) {
      this.transferForm.markAllAsTouched();
      return;
    }
    this.busy.set(true);
    this.message.set('');
    this.error.set('');
    this.banking.transfer(this.transferForm.getRawValue()).subscribe({
      next: (res) => {
        this.busy.set(false);
        this.message.set(res.message || 'Transfer completed.');
      },
      error: (err) => {
        this.busy.set(false);
        this.error.set(extractErrorMessage(err));
      }
    });
  }
}
