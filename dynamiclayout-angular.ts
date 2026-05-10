// DynamicLayout Angular renderer — renders PFDL JSON in Angular apps.
// Single-file standalone component. Zero deps beyond Angular 15+.
//
// Example (standalone):
//   import { DynamicLayoutComponent } from './dynamiclayout-angular.ts'
//   @Component({ standalone: true, imports: [DynamicLayoutComponent], template: `<dl-layout [spec]="ui" [(data)]="form" />` })

import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges } from '@angular/core'
import { CommonModule } from '@angular/common'
import { FormsModule } from '@angular/forms'

type Element = { [key: string]: any }
type Spec = { title?: string; uid?: string; layout?: Element[]; actions?: Element[] }

@Component({
  standalone: true,
  selector: 'dl-layout',
  imports: [CommonModule, FormsModule],
  template: `
    <div class="dl-root">
      <h4 *ngIf="spec?.title">{{ spec.title }}</h4>
      <div class="dl-layout">
        <ng-container *ngFor="let el of spec?.layout; trackBy: trackKey">
          <ng-container *ngTemplateOutlet="renderEl; context: { el: el }" />
        </ng-container>
      </div>
      <div class="dl-actions" *ngIf="spec?.actions?.length">
        <ng-container *ngFor="let el of spec.actions; trackBy: trackKey">
          <ng-container *ngTemplateOutlet="renderEl; context: { el: el }" />
        </ng-container>
      </div>
    </div>

    <!-- Elements -->
    <ng-template #renderEl let-el="el">
      <!-- Containers -->
      <div *ngIf="el.type === 'ROW'" class="dl-row">
        <ng-container *ngFor="let child of el.content; trackBy: trackKey">
          <ng-container *ngTemplateOutlet="renderEl; context: { el: child }" />
        </ng-container>
      </div>
      <div *ngIf="el.type === 'COL'" class="dl-col">
        <ng-container *ngFor="let child of el.content; trackBy: trackKey">
          <ng-container *ngTemplateOutlet="renderEl; context: { el: child }" />
        </ng-container>
      </div>
      <fieldset *ngIf="el.type === 'FIELDSET'">
        <legend *ngIf="el.title">{{ el.title }}</legend>
        <ng-container *ngFor="let child of el.content; trackBy: trackKey">
          <ng-container *ngTemplateOutlet="renderEl; context: { el: child }" />
        </ng-container>
      </fieldset>
      <div *ngIf="el.type === 'INLINE_GROUP'" style="display:inline-flex;gap:0.5rem;align-items:center">
        <ng-container *ngFor="let child of el.content; trackBy: trackKey">
          <ng-container *ngTemplateOutlet="renderEl; context: { el: child }" />
        </ng-container>
      </div>

      <!-- Display -->
      <label *ngIf="el.type === 'LABEL'" style="display:block;margin-bottom:4px;font-weight:500">{{ el.label }}</label>
      <div *ngIf="el.type === 'ALERT'" [style.background]="alertBg(el.color)" [style.color]="alertTx(el.color)" style="padding:12px;border-radius:6px;margin-bottom:16px">{{ el.message }}</div>
      <span *ngIf="el.type === 'BADGE'" [style.background]="badgeColor(el.color)" style="display:inline-block;padding:3px 8px;font-size:12px;font-weight:700;color:#fff;border-radius:4px">{{ el.title }}</span>
      <div *ngIf="el.type === 'SPACER'" [style.height.px]="el.width || 20"></div>
      <div *ngIf="el.type === 'PROGRESS'">
        <div *ngIf="el.label" style="margin-bottom:4px">{{ el.label }}</div>
        <div style="height:20px;background:#e9ecef;border-radius:4px;overflow:hidden">
          <div [style.width.%]="el.progress||0" [style.background]="badgeColor(el.color || 'primary')" style="height:100%;transition:width 0.3s"></div>
        </div>
      </div>

      <!-- Inputs -->
      <div *ngIf="el.type === 'INPUT'" style="margin-bottom:12px">
        <label *ngIf="el.label" style="display:block;margin-bottom:2px">{{ el.label }}{{ el.required ? ' *' : '' }}</label>
        <input [type]="inputType(el.dataType)" [(ngModel)]="data[el.id]" [maxlength]="el.maxLength" style="width:100%;padding:6px 12px;border:1px solid #ced4da;border-radius:4px;box-sizing:border-box" />
      </div>
      <div *ngIf="el.type === 'CHECKBOX'" style="margin-bottom:12px;display:flex;align-items:center;gap:8px">
        <input type="checkbox" [(ngModel)]="data[el.id]" /><label *ngIf="el.label">{{ el.label }}</label>
      </div>
      <div *ngIf="el.type === 'TEXTAREA'" style="margin-bottom:12px">
        <label *ngIf="el.label" style="display:block;margin-bottom:2px">{{ el.label }}</label>
        <textarea [(ngModel)]="data[el.id]" [rows]="el.rows || 3" style="width:100%;padding:6px 12px;border:1px solid #ced4da;border-radius:4px;box-sizing:border-box"></textarea>
      </div>
      <div *ngIf="el.type === 'SELECT'" style="margin-bottom:12px">
        <label *ngIf="el.label" style="display:block;margin-bottom:2px">{{ el.label }}</label>
        <select [(ngModel)]="data[el.id]" style="width:100%;padding:6px 12px;border:1px solid #ced4da;border-radius:4px;box-sizing:border-box">
          <option value=""></option>
          <option *ngFor="let v of el.values" [value]="v.id">{{ v.displayName }}</option>
        </select>
      </div>
      <div *ngIf="el.type === 'RATING'" style="margin-bottom:12px">
        <label *ngIf="el.label" style="display:block;margin-bottom:2px">{{ el.label }}</label>
        <span *ngFor="let n of [1,2,3,4,5]" [style.color]="n <= (data[el.id]||0) ? '#ffc107' : '#dee2e6'" style="cursor:pointer;font-size:24px" (click)="data[el.id] = n">★</span>
      </div>
      <div *ngIf="el.type === 'READONLY_FIELD'" style="margin-bottom:12px">
        <label *ngIf="el.label" style="display:block;margin-bottom:2px;font-weight:500">{{ el.label }}</label>
        <div style="padding:6px 12px;background:#f8f9fa;border-radius:4px">{{ data[el.id] || '—' }}</div>
      </div>

      <!-- Actions -->
      <button *ngIf="el.type === 'BUTTON'" [style.background]="badgeColor(el.color||'primary')" style="padding:8px 16px;border:none;border-radius:4px;cursor:pointer;font-weight:600;color:#fff;margin-right:8px" (click)="onAction.emit({id: el.id, action: el.responseAction})">{{ el.title || el.id }}</button>

      <span *ngIf="!knownTypes.includes(el.type)" style="color:red">Unknown: {{ el.type }}</span>
    </ng-template>
  `,
  styles: [`
    .dl-root { font-family: system-ui, sans-serif; }
    .dl-row { display: flex; flex-wrap: wrap; margin: 0 -8px; }
    .dl-col { flex: 1 1 0%; padding: 0 8px; min-width: 200px; }
    .dl-actions { display: flex; gap: 8px; margin-top: 16px; padding-top: 16px; border-top: 1px solid #dee2e6; }
    fieldset { border: 1px solid #dee2e6; border-radius: 6px; padding: 16px; margin-bottom: 16px; }
    legend { font-size: 1rem; font-weight: 600; margin-bottom: 8px; }
  `]
})
export class DynamicLayoutComponent {
  @Input() spec: Spec | null = null
  @Input() data: { [key: string]: any } = {}
  @Output() dataChange = new EventEmitter<{ [key: string]: any }>()
  @Output() onAction = new EventEmitter<{ id: string; action?: any }>()

  knownTypes = ['ROW','COL','FIELDSET','INLINE_GROUP','GROUP','FRAGMENT','LABEL','ALERT','BADGE','SPACER','PROGRESS','INPUT','CHECKBOX','TEXTAREA','SELECT','RATING','READONLY_FIELD','BUTTON']

  trackKey(i: number, el: Element) { return el.key || i }

  alertBg(c: string) { return ({ info:'#cff4fc', warning:'#fff3cd', danger:'#f8d7da', success:'#d1e7dd' } as any)[c] || '#cff4fc' }
  alertTx(c: string) { return ({ info:'#055160', warning:'#664d03', danger:'#842029', success:'#0f5132' } as any)[c] || '#055160' }
  badgeColor(c: string) { return ({ primary:'#0d6efd', secondary:'#6c757d', success:'#198754', danger:'#dc3545', warning:'#ffc107', info:'#0dcaf0' } as any)[c] || '#6c757d' }
  inputType(dt: string) { return dt === 'PASSWORD' ? 'password' : dt === 'INTEGER' || dt === 'LONG' ? 'number' : dt === 'DATE' ? 'date' : 'text' }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['data']) this.dataChange.emit(this.data)
  }
}
