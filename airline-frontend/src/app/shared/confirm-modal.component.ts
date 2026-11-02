import { AfterViewInit, Component, ElementRef, EventEmitter, HostListener, Input, Output, ViewChild } from '@angular/core';

@Component({
  selector: 'app-confirm-modal',
  standalone: true,
  styleUrl: './confirm-modal.component.css',
  templateUrl: './confirm-modal.component.html'
})
export class ConfirmModalComponent implements AfterViewInit {
  @Input({ required: true }) title = '';
  @Input({ required: true }) message = '';
  @Input() confirmLabel = 'Confirm';
  @Input() cancelLabel = 'Cancel';
  @Input() destructive = false;

  @Output() confirmed = new EventEmitter<void>();
  @Output() dismissed = new EventEmitter<void>();

  @ViewChild('cancelButton') private cancelButton?: ElementRef<HTMLButtonElement>;

  ngAfterViewInit(): void {
    this.cancelButton?.nativeElement.focus();
  }

  @HostListener('document:keydown.escape')
  dismiss(): void {
    this.dismissed.emit();
  }

  accept(): void {
    this.confirmed.emit();
  }
}
