# Firestore Rules Setup - CMS Gereja V6

## ⚠️ PENTING: Update Firestore Rules

Untuk mengaktifkan koneksi database, Anda perlu mengupdate Firestore Rules di Firebase Console.

## Langkah-langkah:

### 1. Buka Firebase Console
- Kunjungi: https://console.firebase.google.com/
- Pilih project: `churchmanagementsystem-a77a3`

### 2. Navigasi ke Firestore Database
- Klik **Firestore Database** di menu kiri
- Pilih tab **Rules**

### 3. Update Rules
Ganti rules yang ada dengan kode berikut:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

### 4. Publish Rules
- Klik tombol **Publish** atau **Deploy**
- Tunggu beberapa detik sampai rules terdeploy

### 5. Verifikasi Koneksi
- Buka aplikasi CMS V6
- Login dengan kredensial default
- Periksa console browser untuk pesan "[FIREBASE] Terhubung ke database"

---

## 🔒 Rules Aman untuk Production (Opsional)

Jika ingin membatasi akses, gunakan rules berikut:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users collection - hanya admin yang bisa mengelola users
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && 
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'superadmin';
    }
    
    // Data lainnya - semua user yang login bisa baca, admin bisa tulis
    match /{collection}/{docId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && 
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role in ['admin', 'superadmin'];
    }
  }
}
```

**Catatan:** Untuk menggunakan rules aman, Anda perlu mengimplementasikan Firebase Authentication.

---

## 🔧 Troubleshooting

### Error: "Permission denied"
- Pastikan rules sudah di-publish
- Cek console browser untuk error detail
- Refresh halaman setelah update rules

### Error: "Failed to get document"
- Pastikan collections sudah dibuat di Firestore
- Cek koneksi internet
- Verifikasi firebaseConfig di `js/firebase.js`

### Data tidak tersimpan
- Cek console untuk pesan error
- Pastikan Firebase SDK berhasil di-load
- Cek browser DevTools > Network untuk request yang gagal

---

## 📊 Collections yang Diperlukan

Pastikan collections berikut tersedia di Firestore:

1. `users` - Data pengguna
2. `members` - Data jemaat
3. `families` - Data keluarga
4. `groups` - Data grup/ministry
5. `events` - Data event
6. `attendance` - Data kehadiran
7. `donations` - Data donasi
8. `donors` - Data donor
9. `volunteers` - Data relawan
10. `assignments` - Data penugasan
11. `announcements` - Data pengumuman
12. `pemasukan` - Data pemasukan keuangan
13. `pengeluaran` - Data pengeluaran keuangan
14. `financeCategories` - Kategori keuangan
15. `financeConfig` - Konfigurasi keuangan
16. `approvalHistory` - Riwayat approval
17. `notifications` - Data notifikasi

Collections akan dibuat otomatis saat pertama kali data disimpan.

---

## 📞 Support

Jika mengalami masalah:
1. Cek browser console untuk error detail
2. Verifikasi konfigurasi Firebase
3. Pastikan Firestore Rules sudah benar
4. Cek koneksi internet
