// DynamicLayout SolidJS renderer — renders PFDL JSON in SolidJS apps.
// Single-file. Zero deps beyond solid-js. Works on Solid 1.8+.
//
// Example:
//   import DynamicLayout from './dynamiclayout-solid'
//   <DynamicLayout spec={ui} data={form()} onUpdate={setForm} onAction={handleAction} />

import { createSignal, createContext, useContext, For, Switch, Match, Show } from 'solid-js'
import { Dynamic } from 'solid-js/web'

const DLContext = createContext()

const REGISTRY = {}
function register(type, comp) { REGISTRY[type] = comp }

function RenderEl(props) {
  const Comp = REGISTRY[props.el?.type]
  return Comp ? Comp(props.el) : <span style={{ color: 'red' }}>Unknown: {props.el?.type}</span>
}

function Children(props) {
  return <For each={props.content || []}>{el => <RenderEl el={el} />}</For>
}

// Containers
register('ROW', (el) => <div class="dl-row"><Children content={el.content} /></div>)
register('COL', (el) => <div class="dl-col"><Children content={el.content} /></div>)
register('GROUP', (el) => <div class="dl-group"><Children content={el.content} /></div>)
register('FIELDSET', (el) => <fieldset><Show when={el.title}><legend>{el.title}</legend></Show><Children content={el.content} /></fieldset>)
register('INLINE_GROUP', (el) => <div style={{ display: 'inline-flex', gap: '0.5rem', 'align-items': 'center' }}><Children content={el.content} /></div>)
register('FRAGMENT', (el) => <Children content={el.content} />)

// Display
register('LABEL', (el) => <label style={{ display: 'block', 'margin-bottom': '4px', 'font-weight': '500' }}>{el.label}</label>)
register('ALERT', (el) => {
  const bg = { info: '#cff4fc', warning: '#fff3cd', danger: '#f8d7da', success: '#d1e7dd' }
  return <div style={{ padding: '12px', background: bg[el.color] || bg.info, 'border-radius': '6px', 'margin-bottom': '16px' }}>{el.message}</div>
})
register('BADGE', (el) => {
  const c = { primary: '#0d6efd', secondary: '#6c757d', success: '#198754', danger: '#dc3545' }
  return <span style={{ display: 'inline-block', padding: '3px 8px', 'font-size': '12px', 'font-weight': '700', background: c[el.color] || '#6c757d', color: '#fff', 'border-radius': '4px' }}>{el.title}</span>
})
register('SPACER', (el) => <div style={{ height: (el.width || 20) + 'px' }} />)
register('PROGRESS', (el) => {
  const c = { primary: '#0d6efd', success: '#198754', danger: '#dc3545' }
  return <div><Show when={el.label}><div style={{ 'margin-bottom': '4px' }}>{el.label}</div></Show><div style={{ height: '20px', background: '#e9ecef', 'border-radius': '4px', overflow: 'hidden' }}><div style={{ width: (el.progress || 0) + '%', height: '100%', background: c[el.color] || c.primary, transition: 'width 0.3s' }} /></div></div>
})

// Inputs
register('INPUT', function(el) {
  const ctx = useContext(DLContext)
  const [val, setVal] = createSignal(ctx?.data[el.id] || '')
  const onInput = (e) => { setVal(e.target.value); ctx?.setData({ [el.id]: e.target.value }) }
  const tp = el.dataType === 'PASSWORD' ? 'password' : el.dataType === 'INTEGER' || el.dataType === 'LONG' ? 'number' : 'text'
  return <div class="dl-field"><Show when={el.label}><label>{el.label}{el.required ? ' *' : ''}</label></Show><input type={tp} value={val()} onInput={onInput} maxLength={el.maxLength} style={{ width: '100%', padding: '6px 12px', border: '1px solid #ced4da', 'border-radius': '4px', 'box-sizing': 'border-box' }} /></div>
})
register('CHECKBOX', function(el) {
  const ctx = useContext(DLContext)
  const [checked, setChecked] = createSignal(!!ctx?.data[el.id])
  return <div style={{ 'margin-bottom': '12px', display: 'flex', 'align-items': 'center', gap: '8px' }}><input type="checkbox" checked={checked()} onChange={e => { setChecked(e.target.checked); ctx?.setData({ [el.id]: e.target.checked }) }} /><Show when={el.label}><label>{el.label}</label></Show></div>
})
register('TEXTAREA', function(el) {
  const ctx = useContext(DLContext)
  return <div class="dl-field"><Show when={el.label}><label>{el.label}</label></Show><textarea rows={el.rows || 3} value={ctx?.data[el.id] || ''} onInput={e => ctx?.setData({ [el.id]: e.target.value })} style={{ width: '100%', padding: '6px 12px', border: '1px solid #ced4da', 'border-radius': '4px', 'box-sizing': 'border-box' }} /></div>
})
register('SELECT', function(el) {
  const ctx = useContext(DLContext)
  return <div class="dl-field"><Show when={el.label}><label>{el.label}</label></Show><select value={ctx?.data[el.id] || ''} onChange={e => ctx?.setData({ [el.id]: e.target.value })} style={{ width: '100%', padding: '6px 12px', border: '1px solid #ced4da', 'border-radius': '4px' }}><option value="" /><For each={el.values || []}>{v => <option value={v.id}>{v.displayName}</option>}</For></select></div>
})
register('RATING', function(el) {
  const ctx = useContext(DLContext)
  const r = () => ctx?.data[el.id] || 0
  return <div class="dl-field"><Show when={el.label}><label>{el.label}</label></Show><div>{[1,2,3,4,5].map(n => <span style={{ cursor: 'pointer', 'font-size': '24px', color: n <= r() ? '#ffc107' : '#dee2e6' }} onClick={() => ctx?.setData({ [el.id]: n })}>★</span>)}</div></div>
})
register('READONLY_FIELD', function(el) {
  const ctx = useContext(DLContext)
  return <div class="dl-field"><Show when={el.label}><label>{el.label}</label></Show><div style={{ padding: '6px 12px', background: '#f8f9fa', 'border-radius': '4px' }}>{ctx?.data[el.id] || '—'}</div></div>
})

// Actions
register('BUTTON', (el) => {
  const c = { primary: '#0d6efd', secondary: '#6c757d', success: '#198754', danger: '#dc3545' }
  return <button style={{ padding: '8px 16px', border: 'none', 'border-radius': '4px', cursor: 'pointer', 'font-weight': '600', background: c[el.color] || c.primary, color: '#fff', 'margin-right': '8px' }} onClick={() => {
    document.querySelector('dl-root')?.dispatchEvent(new CustomEvent('dl-action', { detail: { id: el.id, action: el.responseAction }, bubbles: true }))
  }}>{el.title || el.id}</button>
})

// Root
export default function DynamicLayout(props) {
  const [data, setData] = createSignal(props.data || {})
  const ctx = { data: data(), setData: (u) => { const n = { ...data(), ...u }; setData(n); props.onUpdate?.(n) }, errors: props.errors || [] }

  return <DLContext.Provider value={ctx}>
    <div class="dl-root">
      <Show when={props.spec?.title}><h4>{props.spec.title}</h4></Show>
      <div class="dl-layout"><Children content={props.spec?.layout} /></div>
      <Show when={props.spec?.actions?.length}>
        <div style={{ display: 'flex', gap: '8px', 'margin-top': '16px', 'padding-top': '16px', 'border-top': '1px solid #dee2e6' }}>
          <For each={props.spec.actions}>{el => <RenderEl el={el} />}</For>
        </div>
      </Show>
    </div>
  </DLContext.Provider>
}
