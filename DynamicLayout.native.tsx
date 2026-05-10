// DynamicLayout React Native renderer — renders PFDL JSON in React Native apps.
// Single-file. Works with React Native 0.73+ (iOS, Android).
//
// Example:
//   import DynamicLayout from './DynamicLayout.native'
//   <DynamicLayout spec={ui} data={formData} onUpdate={setFormData} onAction={handleAction} />

import React, { useState, createContext, useContext } from 'react'
import { View, Text, TextInput, Switch, ScrollView, TouchableOpacity, StyleSheet } from 'react-native'

const DLContext = createContext({ data: {}, setData: () => {}, errors: [] })

const REGISTRY = {}

function register(type, comp) { REGISTRY[type] = comp }

function RenderEl({ el }) {
  const Comp = REGISTRY[el.type]
  if (!Comp) return <Text style={{ color: 'red' }}>Unknown: {el.type}</Text>
  return <Comp {...el} />
}

function RenderChildren({ content }) {
  if (!content) return null
  return content.map((el, i) => <RenderEl key={el.key || i} el={el} />)
}

// Containers
register('ROW', ({ content }) => <View style={s.row}>{<RenderChildren content={content} />}</View>)
register('COL', ({ content }) => <View style={s.col}>{<RenderChildren content={content} />}</View>)
register('FIELDSET', ({ title, content }) => <View style={s.fieldset}>{title ? <Text style={s.legend}>{title}</Text> : null}<RenderChildren content={content} /></View>)
register('GROUP', ({ content }) => <View>{<RenderChildren content={content} />}</View>)
register('INLINE_GROUP', ({ content }) => <View style={{ flexDirection: 'row', gap: 8 }}>{<RenderChildren content={content} />}</View>)

// Display
register('LABEL', ({ label }) => <Text style={s.label}>{label}</Text>)
register('ALERT', ({ message, color }) => {
  const bg = { info: '#cff4fc', warning: '#fff3cd', danger: '#f8d7da', success: '#d1e7dd' }
  return <View style={[s.alert, { backgroundColor: bg[color] || bg.info }]}><Text>{message}</Text></View>
})
register('BADGE', ({ title, color }) => {
  const c = { primary: '#0d6efd', secondary: '#6c757d', success: '#198754', danger: '#dc3545' }
  return <View style={[s.badge, { backgroundColor: c[color] || '#6c757d' }]}><Text style={{ color: '#fff', fontSize: 12, fontWeight: '700' }}>{title}</Text></View>
})
register('SPACER', ({ width }) => <View style={{ height: width || 20 }} />)
register('PROGRESS', ({ label, progress, color }) => {
  const c = { primary: '#0d6efd', success: '#198754', danger: '#dc3545' }
  return <View>{label ? <Text>{label}</Text> : null}<View style={{ height: 20, backgroundColor: '#e9ecef', borderRadius: 4, overflow: 'hidden' }}><View style={{ width: `${progress || 0}%`, height: '100%', backgroundColor: c[color] || c.primary }} /></View></View>
})

// Inputs
register('INPUT', function DlInput({ id, label, required, dataType, maxLength }) {
  const { data, setData } = useContext(DLContext)
  return <View style={{ marginBottom: 12 }}>
    {label ? <Text>{label}{required ? ' *' : ''}</Text> : null}
    <TextInput value={data[id] || ''} onChangeText={v => setData({ [id]: v })} maxLength={maxLength}
      secureTextEntry={dataType === 'PASSWORD'} keyboardType={dataType === 'INTEGER' || dataType === 'LONG' ? 'numeric' : 'default'}
      style={{ borderWidth: 1, borderColor: '#ced4da', borderRadius: 4, padding: 8 }} />
  </View>
})
register('CHECKBOX', ({ id, label }) => {
  const { data, setData } = useContext(DLContext)
  return <View style={{ flexDirection: 'row', alignItems: 'center', marginBottom: 12, gap: 8 }}>
    <Switch value={!!data[id]} onValueChange={v => setData({ [id]: v })} />
    {label ? <Text>{label}</Text> : null}
  </View>
})
register('TEXTAREA', ({ id, label, rows }) => {
  const { data, setData } = useContext(DLContext)
  return <View style={{ marginBottom: 12 }}>
    {label ? <Text>{label}</Text> : null}
    <TextInput value={data[id] || ''} onChangeText={v => setData({ [id]: v })} multiline numberOfLines={rows || 3}
      style={{ borderWidth: 1, borderColor: '#ced4da', borderRadius: 4, padding: 8, minHeight: (rows || 3) * 20 }} />
  </View>
})
register('SELECT', ({ id, label, values }) => {
  const { data, setData } = useContext(DLContext)
  return <View style={{ marginBottom: 12 }}>
    {label ? <Text>{label}</Text> : null}
    <View style={{ borderWidth: 1, borderColor: '#ced4da', borderRadius: 4 }}>
      {(values || []).map((v, i) => <TouchableOpacity key={i} style={{ padding: 10, backgroundColor: data[id] === v.id ? '#e7f1ff' : '#fff' }} onPress={() => setData({ [id]: v.id })}><Text>{v.displayName}</Text></TouchableOpacity>)}
    </View>
  </View>
})
register('RATING', ({ id, label }) => {
  const { data, setData } = useContext(DLContext)
  const r = data[id] || 0
  return <View style={{ marginBottom: 12 }}>
    {label ? <Text>{label}</Text> : null}
    <View style={{ flexDirection: 'row', gap: 4 }}>
      {[1,2,3,4,5].map(n => <Text key={n} style={{ fontSize: 28, color: n <= r ? '#ffc107' : '#dee2e6' }} onPress={() => setData({ [id]: n })}>★</Text>)}
    </View>
  </View>
})
register('READONLY_FIELD', ({ id, label }) => {
  const { data } = useContext(DLContext)
  return <View style={{ marginBottom: 12 }}>{label ? <Text style={{ fontWeight: '500' }}>{label}</Text> : null}<View style={{ padding: 8, backgroundColor: '#f8f9fa', borderRadius: 4 }}><Text>{data[id] || '—'}</Text></View></View>
})

// Actions
register('BUTTON', ({ id, title, color }) => {
  const c = { primary: '#0d6efd', secondary: '#6c757d', success: '#198754', danger: '#dc3545' }
  return <TouchableOpacity style={[s.btn, { backgroundColor: c[color] || c.primary }]}><Text style={{ color: '#fff', fontWeight: '600' }}>{title || id}</Text></TouchableOpacity>
})

export default function DynamicLayout({ spec, data: initData = {}, onUpdate, onAction }) {
  const [data, setDataState] = useState(initData)
  function setData(update) { setDataState(prev => { const next = { ...prev, ...update }; onUpdate?.(next); return next }) }

  return <DLContext.Provider value={{ data, setData, errors: [] }}>
    <ScrollView style={{ padding: 16 }}>
      {spec?.title ? <Text style={{ fontSize: 20, fontWeight: 'bold', marginBottom: 16 }}>{spec.title}</Text> : null}
      {spec?.layout ? <RenderChildren content={spec.layout} /> : null}
      {spec?.actions?.length ? <View style={{ flexDirection: 'row', gap: 8, marginTop: 16, paddingTop: 16, borderTopWidth: 1, borderTopColor: '#dee2e6' }}>
        {spec.actions.map((el, i) => <TouchableOpacity key={i} style={[s.btn, { backgroundColor: btnColor(el.color || 'primary') }]} onPress={() => onAction?.(el.id, el.responseAction)}><Text style={{ color: '#fff', fontWeight: '600' }}>{el.title || el.id}</Text></TouchableOpacity>)}
      </View> : null}
    </ScrollView>
  </DLContext.Provider>
}

function btnColor(c) { return ({ primary: '#0d6efd', secondary: '#6c757d', success: '#198754', danger: '#dc3545' })[c] || '#0d6efd' }

const s = StyleSheet.create({
  row: { flexDirection: 'row', flexWrap: 'wrap', marginHorizontal: -4 },
  col: { flex: 1, minWidth: 150, paddingHorizontal: 4 },
  fieldset: { borderWidth: 1, borderColor: '#dee2e6', borderRadius: 8, padding: 12, marginBottom: 16 },
  legend: { fontSize: 16, fontWeight: '600', marginBottom: 8 },
  label: { marginBottom: 4, fontWeight: '500' },
  alert: { padding: 12, borderRadius: 6, marginBottom: 16 },
  badge: { paddingHorizontal: 8, paddingVertical: 3, borderRadius: 4, alignSelf: 'flex-start' },
  btn: { paddingHorizontal: 16, paddingVertical: 10, borderRadius: 6 },
})
