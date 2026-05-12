package dynamiclayout.playground

import android.content.ContentUris
import android.graphics.BitmapFactory
import android.provider.ContactsContract

data class Contact(
    val id: String, val name: String, val phones: List<String> = emptyList(), val emails: List<String> = emptyList(),
    val org: String = "", val addr: String = "", val note: String = "", val photo: ByteArray? = null,
    val phoneTypes: Map<String, String> = emptyMap()
)

object Contacts {
    fun loadBasic(ctx: android.content.Context): List<Contact> {
        val list = mutableListOf<Contact>()
        val c = ctx.contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null) ?: return list
        c.use {
            val ii = it.getColumnIndex(ContactsContract.Contacts._ID); val ni = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            if (ii < 0 || ni < 0) return list
            while (it.moveToNext() && list.size < 100) list.add(Contact(it.getString(ii) ?: "", it.getString(ni) ?: "?"))
        }
        return list
    }

    fun loadWithPhones(ctx: android.content.Context): List<Contact> {
        val list = loadBasic(ctx).toMutableList(); val idx = list.associateBy { it.id }.toMutableMap()
        val c = ctx.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null) ?: return list
        c.use { cur ->
            val ii = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val pi = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val ti = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
            val li = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL)
            if (ii < 0 || pi < 0) return@use
            val tm = mapOf(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE to "📱", ContactsContract.CommonDataKinds.Phone.TYPE_HOME to "🏠", ContactsContract.CommonDataKinds.Phone.TYPE_WORK to "💼", ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK to "📠")
            while (cur.moveToNext()) {
                val cid = cur.getString(ii) ?: continue; val c = idx[cid] ?: continue
                val ph = cur.getString(pi) ?: ""; if (ph.isBlank()) continue
                val tp = if (ti >= 0) cur.getInt(ti) else 0; val lb = if (li >= 0) cur.getString(li) ?: "" else ""
                val icon = when { lb.contains("WhatsApp", true) || lb.contains("Whats App", true) -> "💬"; lb.contains("Telegram", true) -> "✈"; lb.contains("Signal", true) -> "🔒"; lb.contains("Viber", true) -> "📲"; else -> tm[tp] ?: "📞" }
                idx[cid] = c.copy(phones = c.phones + ph, phoneTypes = c.phoneTypes + (ph to icon))
            }
        }
        return idx.values.toList()
    }

    fun loadWithEmails(ctx: android.content.Context): List<Contact> {
        val list = loadWithPhones(ctx).toMutableList(); val idx = list.associateBy { it.id }.toMutableMap()
        val c = ctx.contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, null, null, null) ?: return list
        c.use { cur ->
            val ii = cur.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID); val ei = cur.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
            if (ii < 0 || ei < 0) return@use
            while (cur.moveToNext()) { val cid = cur.getString(ii) ?: continue; val c = idx[cid] ?: continue; val em = cur.getString(ei) ?: ""; if (em.isNotBlank()) idx[cid] = c.copy(emails = c.emails + em) }
        }
        return idx.values.toList()
    }

    fun loadFull(ctx: android.content.Context): List<Contact> {
        val list = loadWithEmails(ctx).toMutableList(); val idx = list.associateBy { it.id }.toMutableMap()
        val orgS = "${ContactsContract.Data.MIMETYPE} = ?"
        listOf(ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE to "org",
            ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE to "addr",
            ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE to "note").forEach { (mime, field) ->
            ctx.contentResolver.query(ContactsContract.Data.CONTENT_URI, null, orgS, arrayOf(mime), null)?.use { cur ->
                val ii = cur.getColumnIndex(ContactsContract.Data.CONTACT_ID); val di = cur.getColumnIndex(ContactsContract.Data.DATA1)
                if (ii >= 0 && di >= 0) while (cur.moveToNext()) { val cid = cur.getString(ii) ?: continue; val c = idx[cid] ?: continue; val v = cur.getString(di) ?: ""
                    if (v.isNotBlank()) idx[cid] = when (field) { "org" -> c.copy(org = if (c.org.isBlank()) v else c.org); "addr" -> c.copy(addr = if (c.addr.isBlank()) v else c.addr); "note" -> c.copy(note = if (c.note.isBlank()) v else "${c.note}\n$v"); else -> c }
                }
            }
        }
        for (c in idx.values.toList()) { try { val cid = c.id.toLongOrNull() ?: continue; val uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, cid); val s = ContactsContract.Contacts.openContactPhotoInputStream(ctx.contentResolver, uri, true); if (s != null) { val b = s.readBytes(); s.close(); if (b.isNotEmpty()) idx[c.id] = c.copy(photo = b) } } catch (_: Exception) {} }
        return idx.values.toList()
    }
}
