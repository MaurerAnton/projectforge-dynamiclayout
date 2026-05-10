<!--
  DynamicLayout HTMX renderer — server-rendered PFDL with HTMX interactivity.
  Single-file. Zero JS framework. Works with any backend (just serve HTML).
  HTMX handles form submissions, partial updates, and navigation without React.

  Approach: The server renders PFDL JSON into HTML on the backend.
  This file is a PYTHON/Go/etc. HELPER that converts PFDL JSON → HTML string.
  Pair with your backend template engine.

  Python example:
    from dynamiclayout_htmx import render
    html = render({"title":"Edit","layout":[...],"actions":[...]}, data={"name":"John"})
    return html
-->

<!-- Conceptual: this is a pattern, not a standalone file.
     Server converts PFDL JSON → HTMX-enhanced HTML. -->

# DynamicLayout HTMX pattern
# --------------------------
# Instead of sending JSON to a JS client, the server renders
# the PFDL layout directly to HTML with HTMX attributes.
#
# Python reference implementation:

import html
import json

def render(spec: dict, data: dict = None) -> str:
    """Convert PFDL JSON spec to HTMX-enhanced HTML."""
    data = data or {}
    parts = ['<div class="dl-root">']

    if spec.get('title'):
        parts.append(f'<h4>{html.escape(spec["title"])}</h4>')

    parts.append('<div class="dl-layout">')
    for el in spec.get('layout', []):
        parts.append(_render_el(el, data))
    parts.append('</div>')

    if spec.get('actions'):
        parts.append('<div class="dl-actions">')
        for el in spec['actions']:
            parts.append(_render_el(el, data))
        parts.append('</div>')

    parts.append('</div>')
    return '\n'.join(parts)

def _render_el(el: dict, data: dict) -> str:
    t = el.get('type')
    if t == 'ROW':     return f'<div class="dl-row">{_children(el, data)}</div>'
    if t == 'COL':     return f'<div class="dl-col">{_children(el, data)}</div>'
    if t == 'FIELDSET':
        title = f'<legend>{html.escape(el.get("title",""))}</legend>' if el.get('title') else ''
        return f'<fieldset>{title}{_children(el, data)}</fieldset>'
    if t == 'GROUP':   return f'<div class="dl-group">{_children(el, data)}</div>'
    if t == 'INLINE_GROUP': return f'<div style="display:inline-flex;gap:0.5rem;align-items:center">{_children(el, data)}</div>'
    if t == 'FRAGMENT': return _children(el, data) or ''

    if t == 'LABEL':   return f'<label>{html.escape(el.get("label",""))}</label>'
    if t == 'ALERT':
        return f'<div class="dl-alert dl-alert-{el.get("color","info")}">{html.escape(el.get("message",""))}</div>'
    if t == 'BADGE':   return f'<span class="dl-badge dl-badge-{el.get("color","primary")}">{html.escape(el.get("title",""))}</span>'
    if t == 'SPACER':  return f'<div style="height:{el.get("width",20)}px"></div>'
    if t == 'PROGRESS':
        label = f'<div>{html.escape(el.get("label",""))}</div>' if el.get('label') else ''
        pct = el.get('progress', 0)
        return f'<div>{label}<div class="dl-progress"><div class="dl-progress-fill" style="width:{pct}%"></div></div></div>'

    if t == 'INPUT':
        req = 'required' if el.get('required') else ''
        val = html.escape(str(data.get(el.get('id', ''), '')))
        tp = 'password' if el.get('dataType') == 'PASSWORD' else 'number' if el.get('dataType') in ('INTEGER','LONG') else 'date' if el.get('dataType') == 'DATE' else 'text'
        label = f'<label>{html.escape(el.get("label",""))}{" *" if el.get("required") else ""}</label>' if el.get('label') else ''
        return f'<div class="dl-field">{label}<input type="{tp}" name="{el.get("id","")}" value="{val}" maxlength="{el.get("maxLength","")}" {req} /></div>'
    if t == 'CHECKBOX':
        ck = 'checked' if data.get(el.get('id', '')) else ''
        label = f'<label>{html.escape(el.get("label",""))}</label>' if el.get('label') else ''
        return f'<div class="dl-checkbox"><input type="checkbox" name="{el.get("id","")}" {ck} />{label}</div>'
    if t == 'TEXTAREA':
        val = html.escape(str(data.get(el.get('id', ''), '')))
        label = f'<label>{html.escape(el.get("label",""))}</label>' if el.get('label') else ''
        return f'<div class="dl-field">{label}<textarea name="{el.get("id","")}" rows="{el.get("rows",3)}">{val}</textarea></div>'
    if t == 'SELECT':
        label = f'<label>{html.escape(el.get("label",""))}</label>' if el.get('label') else ''
        opts = ''.join(f'<option value="{html.escape(str(v.get("id","")))}" {"selected" if str(data.get(el.get("id",""))) == str(v.get("id","")) else ""}>{html.escape(v.get("displayName",""))}</option>' for v in el.get('values', []))
        return f'<div class="dl-field">{label}<select name="{el.get("id","")}"><option value=""></option>{opts}</select></div>'
    if t == 'RATING':
        r = int(data.get(el.get('id', ''), 0))
        label = f'<label>{html.escape(el.get("label",""))}</label>' if el.get('label') else ''
        stars = ''.join(f'<span class="dl-star{" active" if n<=r else ""}" data-rating="{n}">★</span>' for n in range(1,6))
        return f'<div class="dl-field">{label}<div class="dl-rating">{stars}</div></div>'
    if t == 'READONLY_FIELD':
        val = html.escape(str(data.get(el.get('id', ''), '—')))
        label = f'<label>{html.escape(el.get("label",""))}</label>' if el.get('label') else ''
        return f'<div class="dl-field">{label}<div class="dl-readonly">{val}</div></div>'

    if t == 'BUTTON':
        action_url = el.get('responseAction', {}).get('url', '')
        attrs = f' hx-post="{action_url}" hx-target=".dl-root" hx-swap="outerHTML"' if action_url else ''
        return f'<button class="dl-btn dl-btn-{el.get("color","primary")}"{attrs}>{html.escape(el.get("title","") or el.get("id",""))}</button>'

    return f'<span style="color:red">Unknown: {t}</span>'

def _children(el: dict, data: dict) -> str:
    return '\n'.join(_render_el(c, data) for c in el.get('content', []))
