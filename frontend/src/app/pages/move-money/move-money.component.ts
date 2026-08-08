import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/auth.service';
import { BankingService } from '../../core/banking.service';
import { BankAccount } from '../../core/models';
import { extractErrorMessage, formatMoney } from '../../core/utils';

@Component({
  selector: 'app-move-money',
  standalone: true,
  imports: [ReactiveFormsModule, CurrencyPipe],
  templateUrl: './move-money.component.html',
  styleUrl: './move-money.component.scss'
})
export class MoveMoneyComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly banking = inject(BankingService);

  readonly accounts = signal<BankAccount[]>([]);
  readonly tab = signal<'deposit' | 'withdraw' | 'transfer'>('deposit');
  /** Transfer to one of my accounts, or type another customer's account number. */
  readonly transferTarget = signal<'own' | 'other'>('own');
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

  destinationAccounts(): BankAccount[] {
    const from = this.transferForm.controls.fromAccountNumber.value;
    return this.accounts().filter((account) => account.accountNumber !== from);
  }

  ngOnInit(): void {
    this.reloadAccounts();
    this.transferForm.controls.fromAccountNumber.valueChanges.subscribe((from) => {
      if (this.transferTarget() !== 'own') {
        return;
      }
      const options = this.accounts().filter((account) => account.accountNumber !== from);
      const stillValid = options.some(
        (account) => account.accountNumber === this.transferForm.controls.toAccountNumber.value
      );
      if (!stillValid) {
        this.transferForm.patchValue({ toAccountNumber: options[0]?.accountNumber ?? '' });
      }
    });
  }

  reloadAccounts(): void {
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
          const second = accounts[1] ?? null;
          if (second) {
            this.transferForm.patchValue({ toAccountNumber: second.accountNumber });
            this.transferTarget.set('own');
          } else {
            this.transferForm.patchValue({ toAccountNumber: '' });
            this.transferTarget.set('other');
          }
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

  setTransferTarget(mode: 'own' | 'other'): void {
    this.transferTarget.set(mode);
    this.message.set('');
    this.error.set('');
    if (mode === 'own') {
      const from = this.transferForm.controls.fromAccountNumber.value;
      const next = this.accounts().find((account) => account.accountNumber !== from);
      this.transferForm.patchValue({ toAccountNumber: next?.accountNumber ?? '' });
    } else {
      this.transferForm.patchValue({ toAccountNumber: '' });
    }
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
        this.reloadAccounts();
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

    const { fromAccountNumber, toAccountNumber, amount, remarks } = this.transferForm.getRawValue();
    if (fromAccountNumber.trim() === toAccountNumber.trim()) {
      this.error.set('Choose two different accounts for a transfer.');
      return;
    }

    this.busy.set(true);
    this.message.set('');
    this.error.set('');
    this.banking
      .transfer({
        fromAccountNumber: fromAccountNumber.trim(),
        toAccountNumber: toAccountNumber.trim(),
        amount,
        remarks: remarks?.trim() || 'Transfer'
      })
      .subscribe({
        next: (res) => {
          this.busy.set(false);
          this.message.set(
            `${res.message} Sent ${formatMoney(res.amount)} from ${res.fromAccount} to ${res.toAccount}. ` +
              `New balances: ${formatMoney(res.senderBalance)} / ${formatMoney(res.receiverBalance)}.`
          );
          this.reloadAccounts();
        },
        error: (err) => {
          this.busy.set(false);
          this.error.set(extractErrorMessage(err));
        }
      });
  }
}
