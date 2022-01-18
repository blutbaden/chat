import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'custom-spinner',
  templateUrl: './custom-spinner.component.html',
  styleUrls: ['./custom-spinner.component.scss']
})
export class CustomSpinnerComponent implements OnInit {

  @Input() color: 'primary' | 'secondary' | 'success' | 'danger' | 'warning' | 'info' | 'light' | 'dark' = 'primary';
  @Input() type: 'border' | 'grow' = 'border';
  @Input() show: boolean = true;
  @Input() size: 'large' | 'small' = "small";

  constructor() {
  }

  ngOnInit(): void {
  }

  getSpinnerClass() {
    const spinnerClassColor = 'text-' + this.color;
    const spinnerClassType = 'spinner-' + this.type;
    return `${spinnerClassType} ${spinnerClassColor}`

  }
}
