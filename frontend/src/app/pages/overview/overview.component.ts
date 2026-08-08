import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/auth.service';
import { BankingService } from '../../core/banking.service';
import { BankAccount, Customer } from '../../core/models';
import { extractErrorMessage, formatMoney } from '../../core/utils';

@Component({
  selector: 'app-overview',
  standalone: true,
  imports: [ReactiveFormsModule, CurrencyPipe],
  templateUrl: './overview.component.html',
  styleUrl: './overview.component.scss'
})
export class OverviewComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly banking = inject(BankingService);

  readonly customer = signal<Customer | null>(null);
  readonly accounts = signal<BankAccount[]>([]);
  readonly loading = signal(true);
  readonly message = signal('');
  readonly error = signal('');
  readonly creating = signal(false);

  readonly createForm = this.fb.nonNullable.group({
    accountType: ['SAVINGS', Validators.required],
    openingBalance: [1000, [Validators.required, Validators.min(0)]]
  });

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    const customerId = this.auth.user()?.customerId;
    if (!customerId) {
      this.loading.set(false);
      this.error.set('No customer profile is linked to this login.');
      return;
    }
    this.loading.set(true);
    this.banking.getCustomer(customerId).subscribe({
      next: (customer) => {
        this.customer.set(customer);
        this.accounts.set(customer.bankAccounts ?? []);
        this.loading.set(false);
        this.error.set('');
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(extractErrorMessage(err, 'Could not load accounts.'));
      }
    });
  }

  createAccount(): void {
    const customerId = this.auth.user()?.customerId;
    if (!customerId || this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }
    this.creating.set(true);
    this.message.set('');
    this.error.set('');
    this.banking
      .createAccount({
        customerId,
        accountType: this.createForm.controls.accountType.value,
        openingBalance: this.createForm.controls.openingBalance.value
      })
      .subscribe({
        next: () => {
          this.creating.set(false);
          this.message.set('Account opened successfully.');
          this.reload();
        },
        error: (err) => {
          this.creating.set(false);
          this.error.set(extractErrorMessage(err, 'Could not open account.'));
        }
      });
  }

  totalBalance(): number {
    return this.accounts().reduce((sum, account) => sum + (account.balance ?? 0), 0);
  }

  money(value: number): string {
    return formatMoney(value);
  }
}
